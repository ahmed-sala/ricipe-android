package com.example.recipe_android_project.features.auth.data.repository;

import android.annotation.SuppressLint;
import android.content.Context;

import com.example.recipe_android_project.core.helper.UserSessionManager;
import com.example.recipe_android_project.features.auth.data.datasource.local.AuthLocalDatasource;
import com.example.recipe_android_project.features.auth.data.datasource.remote.AuthRemoteDatasource;
import com.example.recipe_android_project.features.auth.data.entities.UserEntity;
import com.example.recipe_android_project.features.auth.data.mapper.UserMapper;
import com.example.recipe_android_project.features.auth.domain.model.User;
import com.example.recipe_android_project.features.home.data.entities.FavoriteMealEntity;
import com.example.recipe_android_project.features.plan.data.entity.MealPlanEntity;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class AuthRepository {
    private final AuthLocalDatasource localDatasource;
    private final AuthRemoteDatasource firebaseDatasource;
    private final UserSessionManager sessionManager;

    public AuthRepository(Context context) {
        this.localDatasource = new AuthLocalDatasource(context);
        this.firebaseDatasource = new AuthRemoteDatasource(context);
        this.sessionManager = UserSessionManager.getInstance(context);
    }


    public Single<User> register(String fullName, String email, String password) {
        if (firebaseDatasource.isNetworkAvailable()) {
            return registerWithFirebaseSync(fullName, email, password)
                    .map(UserMapper::toDomain)
                    .subscribeOn(Schedulers.io());
        } else {
            return localDatasource.registerUser(fullName, email, password)
                    .doOnSuccess(user -> {
                        sessionManager.createSession(
                                user.getId(),
                                user.getId(),
                                user.getEmail(),
                                user.getFullName()
                        );
                    })
                    .map(UserMapper::toDomain)
                    .subscribeOn(Schedulers.io());
        }
    }

    @SuppressLint("CheckResult")
    private Single<UserEntity> registerWithFirebaseSync(String fullName, String email, String password) {
        return firebaseDatasource.registerUser(fullName, email, password)
                .flatMap(firebaseUser -> {
                    String firebaseUid = firebaseUser.getId();

                    return localDatasource.isEmailExists(email)
                            .flatMap(exists -> {
                                if (exists) {
                                    return Single.error(new Exception("Email already registered locally"));
                                }

                                return localDatasource.registerUserWithId(
                                        firebaseUid,
                                        fullName,
                                        email,
                                        password
                                ).doOnSuccess(localUser -> {
                                    sessionManager.createSession(
                                            localUser.getId(),
                                            firebaseUid,
                                            email,
                                            fullName
                                    );
                                });
                            })
                            .onErrorResumeNext(e -> {
                                sessionManager.createSessionWithFirebaseUid(
                                        firebaseUid,
                                        email,
                                        fullName
                                );
                                firebaseUser.setPassword(null);
                                return Single.just(firebaseUser);
                            });
                });
    }


    @SuppressLint("CheckResult")
    public Single<User> login(String email, String password) {
        if (firebaseDatasource.isNetworkAvailable()) {
            return loginWithFirebaseSync(email, password)
                    .map(UserMapper::toDomain)
                    .subscribeOn(Schedulers.io());
        } else {
            return localDatasource.login(email, password)
                    .doOnSuccess(user -> {
                        sessionManager.createSession(
                                user.getId(),
                                user.getId(),
                                user.getEmail(),
                                user.getFullName()
                        );
                    })
                    .map(UserMapper::toDomain)
                    .subscribeOn(Schedulers.io());
        }
    }

    @SuppressLint("CheckResult")
    private Single<UserEntity> loginWithFirebaseSync(String email, String password) {
        return localDatasource.isEmailExists(email)
                .flatMap(existsInLocal ->
                        firebaseDatasource.login(email, password)
                                .flatMap(firebaseUser -> {
                                    String firebaseUid = firebaseUser.getId();

                                    if (existsInLocal) {
                                        return localDatasource.login(email, password)
                                                .doOnSuccess(localUser -> {
                                                    sessionManager.createSession(
                                                            localUser.getId(),
                                                            firebaseUid,
                                                            email,
                                                            localUser.getFullName()
                                                    );

                                                    syncFirestoreToLocal(firebaseUser, localUser);

                                                    syncFavoritesOnLogin(firebaseUid, localUser.getId());

                                                    syncMealPlansOnLogin(firebaseUid, localUser.getId());
                                                });
                                    } else {
                                        return createLocalUserFromFirebase(firebaseUser, password, firebaseUid)
                                                .doOnSuccess(newLocalUser -> {
                                                    sessionManager.createSession(
                                                            newLocalUser.getId(),
                                                            firebaseUid,
                                                            email,
                                                            newLocalUser.getFullName()
                                                    );

                                                    syncFavoritesOnLogin(firebaseUid, newLocalUser.getId());

                                                    syncMealPlansOnLogin(firebaseUid, newLocalUser.getId());
                                                });
                                    }
                                })
                                .onErrorResumeNext(firebaseError -> {
                                    if (existsInLocal) {
                                        return localDatasource.login(email, password)
                                                .doOnSuccess(localUser -> {
                                                    sessionManager.createSession(
                                                            localUser.getId(),
                                                            localUser.getId(),
                                                            email,
                                                            localUser.getFullName()
                                                    );

                                                    createFirebaseUserFromLocal(localUser, password)
                                                            .subscribeOn(Schedulers.io())
                                                            .subscribe(
                                                                    () -> {},
                                                                    e -> {}
                                                            );
                                                });
                                    } else {
                                        return Single.error(new Exception("Invalid email or password"));
                                    }
                                })
                );
    }
    @SuppressLint("CheckResult")
    private void syncFavoritesOnLogin(String firebaseUid, String localUserId) {
        if (firebaseUid == null || firebaseUid.isEmpty()) return;
        if (!firebaseDatasource.isNetworkAvailable()) return;

        syncFavoritesFromFirestore(firebaseUid, localUserId)
                .subscribeOn(Schedulers.io())
                .subscribe(
                        () -> { },
                        e -> { }
                );
    }

    public Completable syncFavoritesFromFirestore(String firebaseUid, String localUserId) {
        if (firebaseUid == null || firebaseUid.isEmpty()) {
            return Completable.complete();
        }

        if (!firebaseDatasource.isNetworkAvailable()) {
            return Completable.complete();
        }

        final String finalLocalUserId = (localUserId != null) ? localUserId : firebaseUid;

        return firebaseDatasource.getFavoritesFromFirestore(firebaseUid)
                .flatMapCompletable(firestoreFavorites -> {
                    if (firestoreFavorites.isEmpty()) {
                        return Completable.complete();
                    }

                    for (FavoriteMealEntity entity : firestoreFavorites) {
                        entity.setUserId(finalLocalUserId);
                    }

                    return localDatasource.mergeFavoritesFromFirestore(firestoreFavorites);
                })
                .onErrorComplete()
                .subscribeOn(Schedulers.io());
    }
    @SuppressLint("CheckResult")
    private void syncMealPlansOnLogin(String firebaseUid, String localUserId) {
        if (firebaseUid == null || firebaseUid.isEmpty()) return;
        if (!firebaseDatasource.isNetworkAvailable()) return;

        syncMealPlansFromFirestore(firebaseUid, localUserId)
                .subscribeOn(Schedulers.io())
                .subscribe(
                        () -> { },
                        e -> { }
                );
    }
    public Completable syncMealPlansFromFirestore(String firebaseUid, String localUserId) {
        if (firebaseUid == null || firebaseUid.isEmpty()) {
            return Completable.complete();
        }

        if (!firebaseDatasource.isNetworkAvailable()) {
            return Completable.complete();
        }

        final String finalLocalUserId = (localUserId != null) ? localUserId : firebaseUid;

        return firebaseDatasource.getMealPlansFromFirestore(firebaseUid)
                .flatMapCompletable(firestoreMealPlans -> {
                    if (firestoreMealPlans.isEmpty()) {
                        return Completable.complete();
                    }

                    for (MealPlanEntity entity : firestoreMealPlans) {
                        entity.setUserId(finalLocalUserId);
                        entity.setSynced(true);
                    }

                    return localDatasource.mergeMealPlansFromFirestore(firestoreMealPlans);
                })
                .onErrorComplete()
                .subscribeOn(Schedulers.io());
    }

    private Completable syncFirestoreToLocal(UserEntity firebaseUser, UserEntity localUser) {
        return Completable.defer(() -> {
            if (firebaseUser.getFullName() != null &&
                    !firebaseUser.getFullName().equals(localUser.getFullName())) {
                return localDatasource.updateFullName(localUser.getId(), firebaseUser.getFullName())
                        .flatMapCompletable(result ->
                                firebaseDatasource.saveUserToFirestore(localUser)
                                        .onErrorComplete()
                        );
            }
            return firebaseDatasource.saveUserToFirestore(localUser)
                    .onErrorComplete();
        }).onErrorComplete();
    }

    private Single<UserEntity> createLocalUserFromFirebase(UserEntity firebaseUser, String password, String firebaseUid) {
        return localDatasource.registerUserWithId(
                firebaseUid,
                firebaseUser.getFullName(),
                firebaseUser.getEmail(),
                password
        );
    }

    private Completable createFirebaseUserFromLocal(UserEntity localUser, String password) {
        return firebaseDatasource.createUserInFirebase(localUser, password)
                .doOnSuccess(updatedUser -> {
                    sessionManager.updateFirebaseUid(updatedUser.getId());
                })
                .ignoreElement()
                .onErrorComplete();
    }


    public Completable logout() {
        Completable localLogout = localDatasource.logout()
                .doOnComplete(() -> sessionManager.clearSession());

        if (firebaseDatasource.isNetworkAvailable()) {
            return localLogout
                    .andThen(firebaseDatasource.logout().onErrorComplete())
                    .subscribeOn(Schedulers.io());
        }

        return localLogout.subscribeOn(Schedulers.io());
    }


    public boolean isSessionLoggedIn() {
        return sessionManager.hasValidSession();
    }

    public Single<User> getCurrentUser() {
        return localDatasource.getCurrentUser()
                .map(UserMapper::toDomain)
                .subscribeOn(Schedulers.io());
    }

    public Single<Boolean> isEmailExists(String email) {
        return localDatasource.isEmailExists(email)
                .subscribeOn(Schedulers.io());
    }

    public Single<Boolean> updateUser(User user) {
        UserEntity entity = UserMapper.toEntity(user);

        return localDatasource.updateUser(entity)
                .doOnSuccess(result -> {
                    if (result) {
                        sessionManager.updateUserInfo(user.getFullName(), user.getEmail());
                    }
                })
                .flatMap(result -> {
                    if (result && firebaseDatasource.isNetworkAvailable()) {
                        return firebaseDatasource.saveUserToFirestore(entity)
                                .toSingleDefault(true)
                                .onErrorReturnItem(true);
                    }
                    return Single.just(result);
                })
                .subscribeOn(Schedulers.io());
    }

    public Single<Boolean> updatePassword(String userId, String oldPassword, String newPassword) {
        return localDatasource.updatePassword(userId, oldPassword, newPassword)
                .subscribeOn(Schedulers.io());
    }


    public boolean isNetworkAvailable() {
        return firebaseDatasource.isNetworkAvailable();
    }
}
