package com.example.recipe_android_project.features.auth.data.datasource.local;

import android.content.Context;

import com.example.recipe_android_project.core.config.DbManager;
import com.example.recipe_android_project.core.helper.SharedPreferencesManager;
import com.example.recipe_android_project.core.utils.PasswordHasher;
import com.example.recipe_android_project.core.utils.PasswordHasher.PasswordValidationResult;
import com.example.recipe_android_project.features.auth.data.entities.UserEntity;

import java.util.List;
import java.util.UUID;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

public class AuthLocalDatasource {

    private static final String KEY_USER_ID = "logged_in_user_id";
    private static final String KEY_USER_EMAIL = "logged_in_user_email";
    private static final String KEY_USER_NAME = "logged_in_user_name";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_LOGIN_TIME = "login_time";

    private final UserDao userDao;
    private final SharedPreferencesManager prefsManager;

    public AuthLocalDatasource(Context context) {
        DbManager dbManager = DbManager.getInstance(context);
        userDao = dbManager.userDao();
        prefsManager = SharedPreferencesManager.getInstance(context);
    }

    public Single<UserEntity> registerUser(String fullName, String email, String password) {
        return Single.defer(() -> {
            PasswordValidationResult validationResult = PasswordHasher.validatePasswordStrength(password);
            if (!validationResult.isValid()) {
                return Single.error(new Exception(validationResult.getMessage()));
            }

            return userDao.isEmailExists(email)
                    .flatMap(exists -> {
                        if (exists) {
                            return Single.error(new Exception("Email already registered"));
                        }

                        String hashedPassword = PasswordHasher.hashPassword(password);

                        UserEntity user = new UserEntity();
                        user.setId(UUID.randomUUID().toString());
                        user.setFullName(fullName);
                        user.setEmail(email);
                        user.setPassword(hashedPassword);
                        user.setLoggedIn(true);
                        user.setCreatedAt(System.currentTimeMillis());
                        user.setUpdatedAt(System.currentTimeMillis());

                        return userDao.insertUser(user)
                                .flatMap(result -> {
                                    if (result > 0) {
                                        saveUserSession(user);
                                        user.setPassword(null);
                                        return Single.just(user);
                                    } else {
                                        return Single.error(new Exception("Failed to register user"));
                                    }
                                });
                    });
        });
    }
    public Single<UserEntity> login(String email, String password) {
        return userDao.getUserByEmail(email)
                .switchIfEmpty(Single.error(new Exception("Invalid email or password")))
                .flatMap(user -> {
                    boolean isPasswordValid = PasswordHasher.verifyPassword(password, user.getPassword());

                    if (!isPasswordValid) {
                        return Single.error(new Exception("Invalid email or password"));
                    }

                    Single<Integer> rehashSingle;
                    if (PasswordHasher.needsRehash(user.getPassword())) {
                        String newHash = PasswordHasher.hashPassword(password);
                        rehashSingle = userDao.updatePassword(user.getId(), newHash, System.currentTimeMillis());
                    } else {
                        rehashSingle = Single.just(0);
                    }

                    return rehashSingle
                            .flatMap(ignored -> userDao.logoutAllUsers())
                            .flatMap(ignored -> userDao.updateLoginStatus(user.getId(), true, System.currentTimeMillis()))
                            .map(ignored -> {
                                user.setLoggedIn(true);
                                saveUserSession(user);
                                user.setPassword(null);
                                return user;
                            });
                });
    }
    public Completable logout() {
        return userDao.logoutAllUsers()
                .doOnSuccess(result -> clearUserSession())
                .ignoreElement();
    }
    public Completable logoutUser(String userId) {
        return userDao.updateLoginStatus(userId, false, System.currentTimeMillis())
                .doOnSuccess(result -> {
                    String currentUserId = prefsManager.getString(KEY_USER_ID, null);
                    if (userId.equals(currentUserId)) {
                        clearUserSession();
                    }
                })
                .ignoreElement();
    }
    private void saveUserSession(UserEntity user) {
        prefsManager.putString(KEY_USER_ID, user.getId());
        prefsManager.putString(KEY_USER_EMAIL, user.getEmail());
        prefsManager.putString(KEY_USER_NAME, user.getFullName());
        prefsManager.putBoolean(KEY_IS_LOGGED_IN, true);
        prefsManager.putLong(KEY_LOGIN_TIME, System.currentTimeMillis());
    }
    private void clearUserSession() {
        prefsManager.remove(KEY_USER_ID);
        prefsManager.remove(KEY_USER_EMAIL);
        prefsManager.remove(KEY_USER_NAME);
        prefsManager.putBoolean(KEY_IS_LOGGED_IN, false);
        prefsManager.remove(KEY_LOGIN_TIME);
    }
    public boolean isSessionLoggedIn() {
        return prefsManager.getBoolean(KEY_IS_LOGGED_IN, false);
    }
    public Single<UserEntity> getCurrentUser() {
        String userId = prefsManager.getString(KEY_USER_ID, null);

        Maybe<UserEntity> userMaybe;
        if (userId != null) {
            userMaybe = userDao.getUserById(userId);
        } else {
            userMaybe = userDao.getLoggedInUser();
        }

        return userMaybe
                .switchIfEmpty(Single.defer(() -> {
                    clearUserSession();
                    return Single.error(new Exception("No user logged in"));
                }))
                .map(user -> {
                    user.setPassword(null);
                    return user;
                });
    }
    public Single<Boolean> isEmailExists(String email) {
        return userDao.isEmailExists(email);
    }
    public Single<Boolean> updateUser(UserEntity user) {
        user.setUpdatedAt(System.currentTimeMillis());
        return userDao.updateUser(user)
                .map(result -> {
                    if (result > 0) {
                        String currentUserId = prefsManager.getString(KEY_USER_ID, null);
                        if (user.getId().equals(currentUserId)) {
                            prefsManager.putString(KEY_USER_NAME, user.getFullName());
                            prefsManager.putString(KEY_USER_EMAIL, user.getEmail());
                        }
                        return true;
                    }
                    return false;
                });
    }

    public Single<Boolean> updateFullName(String userId, String fullName) {
        return userDao.updateFullName(userId, fullName, System.currentTimeMillis())
                .map(result -> {
                    if (result > 0) {
                        String currentUserId = prefsManager.getString(KEY_USER_ID, null);
                        if (userId.equals(currentUserId)) {
                            prefsManager.putString(KEY_USER_NAME, fullName);
                        }
                        return true;
                    }
                    return false;
                });
    }

    public Single<Boolean> updatePassword(String userId, String oldPassword, String newPassword) {
        return Single.defer(() -> {
            PasswordValidationResult validationResult = PasswordHasher.validatePasswordStrength(newPassword);
            if (!validationResult.isValid()) {
                return Single.error(new Exception(validationResult.getMessage()));
            }

            return userDao.getUserById(userId)
                    .switchIfEmpty(Single.error(new Exception("User not found")))
                    .flatMap(user -> {
                        if (!PasswordHasher.verifyPassword(oldPassword, user.getPassword())) {
                            return Single.error(new Exception("Current password is incorrect"));
                        }

                        String hashedNewPassword = PasswordHasher.hashPassword(newPassword);
                        return userDao.updatePassword(userId, hashedNewPassword, System.currentTimeMillis())
                                .map(result -> result > 0);
                    });
        });
    }
    public Single<Boolean> deleteUser(UserEntity user) {
        return userDao.deleteUser(user)
                .map(result -> {
                    if (result > 0) {
                        String currentUserId = prefsManager.getString(KEY_USER_ID, null);
                        if (user.getId().equals(currentUserId)) {
                            clearUserSession();
                        }
                        return true;
                    }
                    return false;
                });
    }
}
