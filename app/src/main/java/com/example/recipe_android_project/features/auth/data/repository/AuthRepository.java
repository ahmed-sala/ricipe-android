package com.example.recipe_android_project.features.auth.data.repository;

import android.content.Context;

import com.example.recipe_android_project.core.utils.PasswordHasher;
import com.example.recipe_android_project.core.utils.PasswordHasher.PasswordValidationResult;
import com.example.recipe_android_project.features.auth.data.datasource.local.AuthLocalDatasource;
import com.example.recipe_android_project.features.auth.data.datasource.remote.AuthRemoteDatasource;
import com.example.recipe_android_project.features.auth.data.entities.UserEntity;
import com.example.recipe_android_project.features.auth.data.mapper.UserMapper;
import com.example.recipe_android_project.features.auth.domain.model.User;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class AuthRepository {

    private static final String TAG = "AuthRepository";

    private final AuthLocalDatasource localDatasource;
    private final AuthRemoteDatasource firebaseDatasource;

    public AuthRepository(Context context) {
        this.localDatasource = new AuthLocalDatasource(context);
        this.firebaseDatasource = new AuthRemoteDatasource(context);
    }

    public Single<User> register(String fullName, String email, String password) {
        if (firebaseDatasource.isNetworkAvailable()) {
            return registerWithFirebaseSync(fullName, email, password)
                    .map(UserMapper::toDomain)
                    .subscribeOn(Schedulers.io());
        } else {
            return localDatasource.registerUser(fullName, email, password)
                    .map(UserMapper::toDomain)
                    .subscribeOn(Schedulers.io());
        }
    }

    private Single<UserEntity> registerWithFirebaseSync(String fullName, String email, String password) {
        return firebaseDatasource.registerUser(fullName, email, password)
                .flatMap(firebaseUser ->
                        localDatasource.isEmailExists(email)
                                .flatMap(exists -> {
                                    if (exists) {
                                        return Single.error(new Exception("Email already registered locally"));
                                    }
                                    return localDatasource.registerUser(fullName, email, password)
                                            .onErrorResumeNext(e -> {
                                                firebaseUser.setPassword(null);
                                                return Single.just(firebaseUser);
                                            });
                                })
                );
    }

    public Single<User> login(String email, String password) {
        if (firebaseDatasource.isNetworkAvailable()) {
            return loginWithFirebaseSync(email, password)
                    .map(UserMapper::toDomain)
                    .subscribeOn(Schedulers.io());
        } else {
            return localDatasource.login(email, password)
                    .map(UserMapper::toDomain)
                    .subscribeOn(Schedulers.io());
        }
    }

    private Single<UserEntity> loginWithFirebaseSync(String email, String password) {
        return localDatasource.isEmailExists(email)
                .flatMap(existsInLocal ->
                        firebaseDatasource.login(email, password)
                                .flatMap(firebaseUser -> {
                                    if (existsInLocal) {
                                        return localDatasource.login(email, password)
                                                .doOnSuccess(localUser -> syncFirestoreToLocal(firebaseUser, localUser));
                                    } else {
                                        return createLocalUserFromFirebase(firebaseUser, password);
                                    }
                                })
                                .onErrorResumeNext(firebaseError -> {
                                    if (existsInLocal) {
                                        return localDatasource.login(email, password)
                                                .doOnSuccess(localUser ->
                                                        createFirebaseUserFromLocal(localUser, password)
                                                                .subscribeOn(Schedulers.io())
                                                                .subscribe(() -> {}, e -> {})
                                                );
                                    } else {
                                        return Single.error(new Exception("Invalid email or password"));
                                    }
                                })
                );
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

    private Single<UserEntity> createLocalUserFromFirebase(UserEntity firebaseUser, String password) {
        return localDatasource.registerUser(
                firebaseUser.getFullName(),
                firebaseUser.getEmail(),
                password
        );
    }

    private Completable createFirebaseUserFromLocal(UserEntity localUser, String password) {
        return firebaseDatasource.createUserInFirebase(localUser, password)
                .ignoreElement()
                .onErrorComplete();
    }

    public Completable logout() {
        Completable localLogout = localDatasource.logout();

        if (firebaseDatasource.isNetworkAvailable()) {
            return localLogout
                    .andThen(firebaseDatasource.logout().onErrorComplete())
                    .subscribeOn(Schedulers.io());
        }

        return localLogout.subscribeOn(Schedulers.io());
    }

    public Completable logoutUser(String userId) {
        Completable localLogout = localDatasource.logoutUser(userId);

        if (firebaseDatasource.isNetworkAvailable()) {
            return localLogout
                    .andThen(firebaseDatasource.logout().onErrorComplete())
                    .subscribeOn(Schedulers.io());
        }

        return localLogout.subscribeOn(Schedulers.io());
    }







    public boolean isSessionLoggedIn() {
        return localDatasource.isSessionLoggedIn();
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

    public Single<Boolean> deleteUser(User user) {
        UserEntity entity = UserMapper.toEntity(user);
        return localDatasource.deleteUser(entity)
                .subscribeOn(Schedulers.io());
    }


}
