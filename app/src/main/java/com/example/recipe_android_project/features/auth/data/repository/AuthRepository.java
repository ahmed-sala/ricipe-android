package com.example.recipe_android_project.features.auth.data.repository;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import com.example.recipe_android_project.core.helper.UserSessionManager;
import com.example.recipe_android_project.features.auth.data.datasource.local.AuthLocalDatasource;
import com.example.recipe_android_project.features.auth.data.datasource.remote.AuthRemoteDatasource;
import com.example.recipe_android_project.features.auth.data.entities.UserEntity;
import com.example.recipe_android_project.features.auth.data.mapper.UserMapper;
import com.example.recipe_android_project.features.auth.domain.model.User;
import com.example.recipe_android_project.features.home.data.entities.FavoriteMealEntity;
import com.example.recipe_android_project.features.plan.data.entity.MealPlanEntity;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class AuthRepository {

    private static final String TAG = "AuthRepository";
    private static final int FIREBASE_TIMEOUT_SECONDS = 10;

    private final AuthLocalDatasource localDatasource;
    private final AuthRemoteDatasource remoteDatasource;
    private final UserSessionManager sessionManager;

    public AuthRepository(Context context) {
        this.localDatasource = new AuthLocalDatasource(context);
        this.remoteDatasource = new AuthRemoteDatasource(context);
        this.sessionManager = UserSessionManager.getInstance(context);
    }


    public boolean isNetworkAvailable() {
        return remoteDatasource.isNetworkAvailable();
    }


    public boolean isSessionLoggedIn() {
        return sessionManager.hasValidSession();
    }
    public Single<User> register(String fullName, String email, String password) {
        return Single.defer(() -> {
            if (isNetworkAvailable()) {
                return registerWithFirebase(fullName, email, password)
                        .map(UserMapper::toDomain);
            } else {
                return registerOffline(fullName, email, password)
                        .map(UserMapper::toDomain);
            }
        }).subscribeOn(Schedulers.io());
    }

    private Single<UserEntity> registerWithFirebase(String fullName, String email, String password) {
        if (!isNetworkAvailable()) {
            return registerOffline(fullName, email, password);
        }

        return remoteDatasource.registerUser(fullName, email, password)
                .timeout(FIREBASE_TIMEOUT_SECONDS * 2, TimeUnit.SECONDS)
                .flatMap(firebaseUser -> {
                    String firebaseUid = firebaseUser.getId();

                    return localDatasource.isEmailExists(email)
                            .flatMap(exists -> {
                                if (exists) {
                                    return Single.error(
                                            new Exception("Email already registered locally"));
                                }
                                return localDatasource.registerUserWithId(
                                        firebaseUid, fullName, email, password
                                ).doOnSuccess(localUser -> {
                                    sessionManager.createSession(
                                            localUser.getId(),
                                            firebaseUid,
                                            email,
                                            fullName
                                    );
                                });
                            })
                            .onErrorResumeNext(localError -> {
                                sessionManager.createSessionWithFirebaseUid(
                                        firebaseUid, email, fullName);
                                firebaseUser.setPassword(null);
                                return Single.just(firebaseUser);
                            });
                })
                .onErrorResumeNext(firebaseError -> {

                    if (isNetworkError(firebaseError)) {
                        return registerOffline(fullName, email, password);
                    }
                    return Single.error(firebaseError);
                });
    }

    private Single<UserEntity> registerOffline(String fullName, String email, String password) {
        return localDatasource.registerUserOffline(fullName, email, password)
                .doOnSuccess(user -> {
                    sessionManager.createSession(
                            user.getId(),
                            user.getId(),
                            user.getEmail(),
                            user.getFullName()
                    );
                });
    }
    public Single<User> login(String email, String password) {
        return Single.defer(() -> {
            if (isNetworkAvailable()) {
                return loginWithFirebase(email, password)
                        .map(UserMapper::toDomain);
            } else {
                return loginOffline(email, password)
                        .map(UserMapper::toDomain);
            }
        }).subscribeOn(Schedulers.io());
    }

    private Single<UserEntity> loginWithFirebase(String email, String password) {
        return localDatasource.isEmailExists(email)
                .flatMap(existsLocally -> {
                    if (!isNetworkAvailable()) {
                        if (existsLocally) {
                            return loginOffline(email, password);
                        }
                        return Single.error(
                                new Exception("No network and user not found locally"));
                    }

                    return remoteDatasource.login(email, password)
                            .timeout(FIREBASE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                            .flatMap(firebaseUser -> {
                                String firebaseUid = firebaseUser.getId();

                                if (existsLocally) {
                                    return loginExistingLocalUser(
                                            email, password, firebaseUid);
                                } else {
                                    return loginNewLocalUser(
                                            email, password, firebaseUser, firebaseUid);
                                }
                            })
                            .onErrorResumeNext(error ->
                                    handleLoginError(error, existsLocally, email, password));
                });
    }

    private Single<UserEntity> loginExistingLocalUser(String email, String password,
                                                      String firebaseUid) {
        return localDatasource.login(email, password)
                .doOnSuccess(localUser -> {
                    sessionManager.createSession(
                            localUser.getId(),
                            firebaseUid,
                            email,
                            localUser.getFullName()
                    );

                    syncDataOnLogin(firebaseUid, localUser.getId());
                });
    }

    private Single<UserEntity> loginNewLocalUser(String email, String password,
                                                 UserEntity firebaseUser,
                                                 String firebaseUid) {
        return localDatasource.registerUserWithId(
                firebaseUid,
                firebaseUser.getFullName(),
                firebaseUser.getEmail(),
                password
        ).doOnSuccess(newUser -> {
            sessionManager.createSession(
                    newUser.getId(),
                    firebaseUid,
                    email,
                    newUser.getFullName()
            );

            syncDataOnLogin(firebaseUid, newUser.getId());
        });
    }

    private Single<UserEntity> handleLoginError(Throwable error,
                                                boolean existsLocally,
                                                String email, String password) {

        if (existsLocally) {
            return localDatasource.login(email, password)
                    .doOnSuccess(localUser -> {
                        sessionManager.createSession(
                                localUser.getId(),
                                localUser.getId(),
                                email,
                                localUser.getFullName()
                        );
                    });
        }

        if (isNetworkError(error)) {
            return Single.error(
                    new Exception("No network and user not found locally"));
        }
        return Single.error(new Exception("Invalid email or password"));
    }

    private Single<UserEntity> loginOffline(String email, String password) {
        return localDatasource.login(email, password)
                .doOnSuccess(user -> {
                    sessionManager.createSession(
                            user.getId(),
                            user.getId(),
                            user.getEmail(),
                            user.getFullName()
                    );
                });
    }


    public Completable logout() {
        return Completable.defer(() -> {
            Completable localLogout = localDatasource.logout()
                    .doOnComplete(() -> {
                        sessionManager.clearSession();
                    });

            if (isNetworkAvailable()) {
                return localLogout
                        .andThen(remoteDatasource.logout()
                                .timeout(FIREBASE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                                .doOnComplete(() -> Log.d(TAG, "Firebase logout done"))
                                .onErrorComplete());
            }

            return localLogout;
        }).subscribeOn(Schedulers.io());
    }


    public Single<Boolean> isEmailExists(String email) {
        return localDatasource.isEmailExists(email)
                .subscribeOn(Schedulers.io());
    }

    public Completable syncPendingRegistrations() {
        return Completable.defer(() -> {
            if (!isNetworkAvailable()) {
                return Completable.complete();
            }

            return localDatasource.getPendingRegistrationSyncUsers()
                    .flatMapCompletable(users -> {
                        if (users == null || users.isEmpty()) {
                            return Completable.complete();
                        }

                        return Flowable.fromIterable(users)
                                .flatMapCompletable(
                                        this::syncSingleRegistration, false, 1);
                    })
                    .onErrorComplete();
        }).subscribeOn(Schedulers.io());
    }

    private Completable syncSingleRegistration(UserEntity userEntity) {
        if (!isNetworkAvailable()) {
            return Completable.complete();
        }

        if (userEntity.getPendingPlainPassword() == null
                || userEntity.getPendingPlainPassword().isEmpty()) {
            return localDatasource.clearRegistrationSyncFlag(userEntity.getId())
                    .flatMapCompletable(success -> Completable.complete());
        }

        return remoteDatasource.createUserInFirebaseForSync(userEntity)
                .timeout(FIREBASE_TIMEOUT_SECONDS * 2, TimeUnit.SECONDS)
                .flatMapCompletable(firebaseUid -> {
                    sessionManager.updateFirebaseUid(firebaseUid);

                    return localDatasource.clearRegistrationSyncFlag(userEntity.getId())
                            .flatMapCompletable(success -> Completable.complete());
                })
                .onErrorComplete(error -> {
                    Log.e(TAG, "Registration sync failed: " + error.getMessage());
                    return true;
                });
    }


    public Completable syncFavoritesFromFirestore(String firebaseUid, String localUserId) {
        return Completable.defer(() -> {
            if (firebaseUid == null || firebaseUid.isEmpty() || !isNetworkAvailable()) {
                return Completable.complete();
            }

            final String userId = (localUserId != null) ? localUserId : firebaseUid;

            return remoteDatasource.getFavoritesFromFirestore(firebaseUid)
                    .timeout(FIREBASE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .flatMapCompletable(favorites -> {
                        if (favorites.isEmpty()) {
                            return Completable.complete();
                        }

                        for (FavoriteMealEntity entity : favorites) {
                            entity.setUserId(userId);
                        }

                        return localDatasource.mergeFavoritesFromFirestore(favorites);
                    })
                    .onErrorComplete();
        }).subscribeOn(Schedulers.io());
    }


    public Completable syncMealPlansFromFirestore(String firebaseUid, String localUserId) {
        return Completable.defer(() -> {
            if (firebaseUid == null || firebaseUid.isEmpty() || !isNetworkAvailable()) {
                return Completable.complete();
            }

            final String userId = (localUserId != null) ? localUserId : firebaseUid;

            return remoteDatasource.getMealPlansFromFirestore(firebaseUid)
                    .timeout(FIREBASE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .flatMapCompletable(mealPlans -> {
                        if (mealPlans.isEmpty()) {
                            return Completable.complete();
                        }

                        for (MealPlanEntity entity : mealPlans) {
                            entity.setUserId(userId);
                            entity.setSynced(true);
                        }

                        return localDatasource.mergeMealPlansFromFirestore(mealPlans);
                    })
                    .onErrorComplete();
        }).subscribeOn(Schedulers.io());
    }


    @SuppressLint("CheckResult")
    private void syncDataOnLogin(String firebaseUid, String localUserId) {
        if (!isNetworkAvailable()) {
            Log.d(TAG, "Skipping login data sync - no network");
            return;
        }

        syncFavoritesFromFirestore(firebaseUid, localUserId)
                .subscribeOn(Schedulers.io())
                .subscribe(
                        () -> Log.d(TAG, "Favorites sync done"),
                        e -> Log.e(TAG, "Favorites sync error: " + e.getMessage())
                );

        syncMealPlansFromFirestore(firebaseUid, localUserId)
                .subscribeOn(Schedulers.io())
                .subscribe(
                        () -> Log.d(TAG, "Meal plans sync done"),
                        e -> Log.e(TAG, "Meal plans sync error: " + e.getMessage())
                );
    }

    public Single<User> signInWithGoogle(String idToken) {
        return Single.defer(() -> {
            if (!isNetworkAvailable()) {
                return Single.error(
                        new Exception("Google sign-in requires an internet connection"));
            }

            return remoteDatasource.signInWithGoogle(idToken)
                    .timeout(FIREBASE_TIMEOUT_SECONDS * 2, TimeUnit.SECONDS)
                    .flatMap(firebaseUser -> {
                        String firebaseUid = firebaseUser.getId();
                        String email = firebaseUser.getEmail();
                        String fullName = firebaseUser.getFullName();

                        return localDatasource.registerOrLoginGoogleUser(
                                        firebaseUid, fullName, email)
                                .doOnSuccess(localUser -> {
                                    sessionManager.createSession(
                                            localUser.getId(),
                                            firebaseUid,
                                            email,
                                            fullName
                                    );
                                    syncDataOnLogin(firebaseUid, localUser.getId());
                                })
                                .onErrorResumeNext(localError -> {
                                    Log.e(TAG, "Local save failed for Google user: "
                                            + localError.getMessage());
                                    sessionManager.createSessionWithFirebaseUid(
                                            firebaseUid, email, fullName);
                                    firebaseUser.setPassword(null);
                                    return Single.just(firebaseUser);
                                });
                    })
                    .map(UserMapper::toDomain);
        }).subscribeOn(Schedulers.io());
    }
    private boolean isNetworkError(Throwable error) {
        if (error instanceof TimeoutException) return true;

        String message = error.getMessage();
        if (message == null) return false;

        String lower = message.toLowerCase();
        return lower.contains("network")
                || lower.contains("connection")
                || lower.contains("timeout")
                || lower.contains("unreachable")
                || lower.contains("failed to connect")
                || lower.contains("unable to resolve host")
                || lower.contains("no address associated")
                || lower.contains("socket");
    }
}
