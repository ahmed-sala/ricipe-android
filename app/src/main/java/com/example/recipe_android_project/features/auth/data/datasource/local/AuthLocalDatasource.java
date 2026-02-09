package com.example.recipe_android_project.features.auth.data.datasource.local;

import android.content.Context;

import com.example.recipe_android_project.core.config.DbManager;
import com.example.recipe_android_project.core.helper.UserSessionManager;
import com.example.recipe_android_project.core.utils.PasswordHasher;
import com.example.recipe_android_project.core.utils.PasswordHasher.PasswordValidationResult;
import com.example.recipe_android_project.features.auth.data.entities.UserEntity;
import com.example.recipe_android_project.features.home.data.datasource.local.MealDao;
import com.example.recipe_android_project.features.home.data.entities.FavoriteMealEntity;
import com.example.recipe_android_project.features.plan.data.datasource.local.MealPlanDao;
import com.example.recipe_android_project.features.plan.data.entity.MealPlanEntity;

import java.util.List;
import java.util.UUID;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

public class AuthLocalDatasource {

    private final UserDao userDao;
    private final MealDao mealDao;
    private final MealPlanDao mealPlanDao;
    private final UserSessionManager sessionManager;

    public AuthLocalDatasource(Context context) {
        DbManager dbManager = DbManager.getInstance(context);
        userDao = dbManager.userDao();
        mealDao = dbManager.favoriteMealDao();
        mealPlanDao = dbManager.mealPlanDao();
        sessionManager = UserSessionManager.getInstance(context);
    }


    public UserSessionManager getSessionManager() {
        return sessionManager;
    }

    public String getCurrentUserId() {
        return sessionManager.requireCurrentUserId();
    }

    public String getCurrentUserIdOrNull() {
        return sessionManager.getCurrentUserIdOrNull();
    }

    public boolean isLoggedIn() {
        return sessionManager.isLoggedIn();
    }

    public boolean hasValidSession() {
        return sessionManager.hasValidSession();
    }


    public Single<UserEntity> registerUser(String fullName, String email, String password) {
        return registerUserWithId(UUID.randomUUID().toString(), fullName, email, password);
    }

    public Single<UserEntity> registerUserWithId(String userId, String fullName, String email, String password) {
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
                        user.setId(userId);
                        user.setFullName(fullName);
                        user.setEmail(email);
                        user.setPassword(hashedPassword);
                        user.setLoggedIn(true);
                        user.setCreatedAt(System.currentTimeMillis());
                        user.setUpdatedAt(System.currentTimeMillis());

                        return userDao.logoutAllUsers()
                                .flatMap(ignored -> userDao.insertUser(user))
                                .flatMap(result -> {
                                    if (result > 0) {
                                        sessionManager.createSession(
                                                user.getId(),
                                                user.getId(),
                                                user.getEmail(),
                                                user.getFullName()
                                        );

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

                                sessionManager.createSession(
                                        user.getId(),
                                        user.getId(),
                                        user.getEmail(),
                                        user.getFullName()
                                );

                                user.setPassword(null);
                                return user;
                            });
                });
    }


    public Completable logout() {
        return userDao.logoutAllUsers()
                .doOnSuccess(result -> sessionManager.clearSession())
                .ignoreElement();
    }



    public Single<UserEntity> getCurrentUser() {
        String userId = sessionManager.getCurrentUserId();

        Maybe<UserEntity> userMaybe;
        if (userId != null) {
            userMaybe = userDao.getUserById(userId);
        } else {
            userMaybe = userDao.getLoggedInUser();
        }

        return userMaybe
                .switchIfEmpty(Single.defer(() -> {
                    sessionManager.clearSession();
                    return Single.error(new Exception("No user logged in"));
                }))
                .map(user -> {
                    user.setPassword(null);
                    return user;
                });
    }

    public Single<UserEntity> getUserById(String userId) {
        return userDao.getUserById(userId)
                .switchIfEmpty(Single.error(new Exception("User not found")))
                .map(user -> {
                    user.setPassword(null);
                    return user;
                });
    }

    public Single<UserEntity> getUserByEmail(String email) {
        return userDao.getUserByEmail(email)
                .switchIfEmpty(Single.error(new Exception("User not found")))
                .map(user -> {
                    user.setPassword(null);
                    return user;
                });
    }

    public Single<Boolean> isEmailExists(String email) {
        return userDao.isEmailExists(email);
    }

    public Single<Boolean> isUserExists(String userId) {
        return userDao.isUserExists(userId);
    }


    public Single<Boolean> updateUser(UserEntity user) {
        user.setUpdatedAt(System.currentTimeMillis());
        return userDao.updateUser(user)
                .map(result -> {
                    if (result > 0) {
                        if (sessionManager.isCurrentUser(user.getId())) {
                            sessionManager.updateUserInfo(user.getFullName(), user.getEmail());
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
                        if (sessionManager.isCurrentUser(userId)) {
                            sessionManager.updateUserName(fullName);
                        }
                        return true;
                    }
                    return false;
                });
    }

    public Single<Boolean> updateEmail(String userId, String email) {
        return userDao.isEmailExists(email)
                .flatMap(exists -> {
                    if (exists) {
                        return Single.error(new Exception("Email already in use"));
                    }
                    return userDao.updateEmail(userId, email, System.currentTimeMillis())
                            .map(result -> {
                                if (result > 0) {
                                    if (sessionManager.isCurrentUser(userId)) {
                                        sessionManager.updateUserEmail(email);
                                    }
                                    return true;
                                }
                                return false;
                            });
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

    public Single<Boolean> updateUserInfo(String userId, String fullName, String email) {
        return userDao.getUserById(userId)
                .switchIfEmpty(Single.error(new Exception("User not found")))
                .flatMap(user -> {
                    if (!user.getEmail().equals(email)) {
                        return userDao.isEmailExists(email)
                                .flatMap(exists -> {
                                    if (exists) {
                                        return Single.error(new Exception("Email already in use"));
                                    }
                                    return performUserInfoUpdate(userId, fullName, email);
                                });
                    }
                    return performUserInfoUpdate(userId, fullName, email);
                });
    }

    private Single<Boolean> performUserInfoUpdate(String userId, String fullName, String email) {
        return userDao.updateUserInfo(userId, fullName, email, System.currentTimeMillis())
                .map(result -> {
                    if (result > 0) {
                        if (sessionManager.isCurrentUser(userId)) {
                            sessionManager.updateUserInfo(fullName, email);
                        }
                        return true;
                    }
                    return false;
                });
    }


    public Single<Boolean> deleteUser(UserEntity user) {
        return userDao.deleteUser(user)
                .map(result -> {
                    if (result > 0) {
                        if (sessionManager.isCurrentUser(user.getId())) {
                            sessionManager.clearSession();
                        }
                        return true;
                    }
                    return false;
                });
    }

    public Single<Boolean> deleteUserById(String userId) {
        return userDao.deleteUserById(userId)
                .map(result -> {
                    if (result > 0) {
                        if (sessionManager.isCurrentUser(userId)) {
                            sessionManager.clearSession();
                        }
                        return true;
                    }
                    return false;
                });
    }
    public Completable mergeFavoritesFromFirestore(List<FavoriteMealEntity> firestoreFavorites) {
        if (firestoreFavorites == null || firestoreFavorites.isEmpty()) {
            return Completable.complete();
        }
        return mealDao.insertAllFavorites(firestoreFavorites);
    }
    public Completable mergeMealPlansFromFirestore(List<MealPlanEntity> firestoreMealPlans) {
        if (firestoreMealPlans == null || firestoreMealPlans.isEmpty()) {
            return Completable.complete();
        }
        return mealPlanDao.insertAllMealPlans(firestoreMealPlans);
    }
    public Single<List<MealPlanEntity>> getUnsyncedMealPlans(String userId) {
        return mealPlanDao.getUnsyncedMealPlans(userId);
    }
    public Completable updateMealPlanSyncStatus(String userId, String date, String mealType, boolean isSynced) {
        return mealPlanDao.updateSyncStatus(userId, date, mealType, isSynced);
    }
}
