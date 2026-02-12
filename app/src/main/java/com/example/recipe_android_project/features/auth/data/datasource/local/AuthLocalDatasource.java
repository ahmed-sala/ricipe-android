package com.example.recipe_android_project.features.auth.data.datasource.local;

import android.content.Context;
import android.util.Log;

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
import io.reactivex.rxjava3.core.Single;

public class AuthLocalDatasource {


    private final UserDao userDao;
    private final MealDao mealDao;
    private final MealPlanDao mealPlanDao;
    private final UserSessionManager sessionManager;

    public AuthLocalDatasource(Context context) {
        DbManager dbManager = DbManager.getInstance(context);
        this.userDao = dbManager.userDao();
        this.mealDao = dbManager.favoriteMealDao();
        this.mealPlanDao = dbManager.mealPlanDao();
        this.sessionManager = UserSessionManager.getInstance(context);
    }


    public boolean isLoggedIn() {
        return sessionManager.isLoggedIn();
    }


    public Single<UserEntity> registerUserWithId(String userId, String fullName,
                                                 String email, String password) {
        return Single.defer(() -> {
            PasswordValidationResult validation = PasswordHasher.validatePasswordStrength(password);
            if (!validation.isValid()) {
                return Single.error(new Exception(validation.getMessage()));
            }

            return userDao.isEmailExists(email)
                    .flatMap(exists -> {
                        if (exists) {
                            return Single.error(new Exception("Email already registered"));
                        }

                        String hashedPassword = PasswordHasher.hashPassword(password);

                        UserEntity user = createUserEntity(userId, fullName, email, hashedPassword);
                        user.setPendingRegistrationSync(false);

                        return insertAndCreateSession(user);
                    });
        });
    }

    public Single<UserEntity> registerUserOffline(String fullName, String email, String password) {
        return Single.defer(() -> {
            PasswordValidationResult validation = PasswordHasher.validatePasswordStrength(password);
            if (!validation.isValid()) {
                return Single.error(new Exception(validation.getMessage()));
            }

            return userDao.isEmailExists(email)
                    .flatMap(exists -> {
                        if (exists) {
                            return Single.error(new Exception("Email already registered"));
                        }

                        String hashedPassword = PasswordHasher.hashPassword(password);
                        String tempId = UUID.randomUUID().toString();

                        UserEntity user = createUserEntity(tempId, fullName, email, hashedPassword);
                        user.setPendingRegistrationSync(true);
                        user.setPendingPlainPassword(password);

                        return insertAndCreateSession(user);
                    });
        });
    }


    public Single<UserEntity> login(String email, String password) {
        return userDao.getUserByEmail(email)
                .switchIfEmpty(Single.error(new Exception("Invalid email or password")))
                .flatMap(user -> {
                    boolean isValid = PasswordHasher.verifyPassword(password, user.getPassword());
                    if (!isValid) {
                        return Single.error(new Exception("Invalid email or password"));
                    }

                    Single<Integer> rehashSingle;
                    if (PasswordHasher.needsRehash(user.getPassword())) {
                        String newHash = PasswordHasher.hashPassword(password);
                        rehashSingle = userDao.updatePassword(
                                user.getId(), newHash, System.currentTimeMillis());
                    } else {
                        rehashSingle = Single.just(0);
                    }

                    return rehashSingle
                            .flatMap(ignored -> userDao.logoutAllUsers())
                            .flatMap(ignored -> userDao.updateLoginStatus(
                                    user.getId(), true, System.currentTimeMillis()))
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


    public Single<Boolean> isEmailExists(String email) {
        return userDao.isEmailExists(email);
    }


    public Single<List<UserEntity>> getPendingRegistrationSyncUsers() {
        return userDao.getAllPendingRegistrationSyncUsers();
    }

    public Single<Boolean> clearRegistrationSyncFlag(String userId) {
        long now = System.currentTimeMillis();
        return userDao.clearRegistrationSyncFlag(userId, now, now)
                .map(result -> result > 0);
    }


    public Completable mergeFavoritesFromFirestore(List<FavoriteMealEntity> favorites) {
        if (favorites == null || favorites.isEmpty()) {
            return Completable.complete();
        }
        return mealDao.insertAllFavorites(favorites);
    }

    public Completable mergeMealPlansFromFirestore(List<MealPlanEntity> mealPlans) {
        if (mealPlans == null || mealPlans.isEmpty()) {
            return Completable.complete();
        }
        return mealPlanDao.insertAllMealPlans(mealPlans);
    }


    private UserEntity createUserEntity(String id, String fullName,
                                        String email, String hashedPassword) {
        UserEntity user = new UserEntity();
        user.setId(id);
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPassword(hashedPassword);
        user.setLoggedIn(true);
        user.setPendingSync(false);
        user.setPendingPasswordSync(false);
        user.setPendingRegistrationSync(false);
        user.setCreatedAt(System.currentTimeMillis());
        user.setUpdatedAt(System.currentTimeMillis());
        return user;
    }

    private Single<UserEntity> insertAndCreateSession(UserEntity user) {
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
                        user.setPendingPlainPassword(null);
                        return Single.just(user);
                    }
                    return Single.error(new Exception("Failed to register user"));
                });
    }
    public Single<UserEntity> registerOrLoginGoogleUser(String firebaseUid,
                                                        String fullName,
                                                        String email) {
        return userDao.isEmailExists(email)
                .flatMap(exists -> {
                    if (exists) {
                        return userDao.getUserByEmail(email)
                                .switchIfEmpty(Single.error(
                                        new Exception("User not found")))
                                .flatMap(existingUser ->
                                        userDao.logoutAllUsers()
                                                .flatMap(ignored ->
                                                        userDao.updateLoginStatus(
                                                                existingUser.getId(),
                                                                true,
                                                                System.currentTimeMillis()))
                                                .map(ignored -> {
                                                    existingUser.setLoggedIn(true);
                                                    existingUser.setPassword(null);

                                                    sessionManager.createSession(
                                                            existingUser.getId(),
                                                            firebaseUid,
                                                            email,
                                                            existingUser.getFullName()
                                                    );

                                                    return existingUser;
                                                })
                                );
                    } else {
                        UserEntity user = new UserEntity();
                        user.setId(firebaseUid);
                        user.setFullName(fullName != null ? fullName : "");
                        user.setEmail(email);
                        user.setPassword(null);
                        user.setLoggedIn(true);
                        user.setPendingSync(false);
                        user.setPendingPasswordSync(false);
                        user.setPendingRegistrationSync(false);
                        user.setCreatedAt(System.currentTimeMillis());
                        user.setUpdatedAt(System.currentTimeMillis());

                        return userDao.logoutAllUsers()
                                .flatMap(ignored -> userDao.insertUser(user))
                                .flatMap(result -> {
                                    if (result > 0) {
                                        sessionManager.createSession(
                                                user.getId(),
                                                firebaseUid,
                                                email,
                                                fullName
                                        );
                                        return Single.just(user);
                                    }
                                    return Single.error(
                                            new Exception("Failed to save Google user locally"));
                                });
                    }
                });
    }
}
