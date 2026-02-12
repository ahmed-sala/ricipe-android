package com.example.recipe_android_project.features.profile.data.datasource.local;

import android.content.Context;
import android.util.Log;

import com.example.recipe_android_project.core.config.DbManager;
import com.example.recipe_android_project.core.helper.UserSessionManager;
import com.example.recipe_android_project.core.utils.PasswordHasher;
import com.example.recipe_android_project.features.auth.data.entities.UserEntity;

import java.util.List;

import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

public class ProfileLocalDatasource {

    private static final String TAG = "ProfileLocalDatasource";

    private final ProfileDao profileDao;
    private final UserSessionManager sessionManager;

    public ProfileLocalDatasource(Context context) {
        DbManager dbManager = DbManager.getInstance(context);
        this.profileDao = dbManager.profileDao();
        this.sessionManager = UserSessionManager.getInstance(context);
    }


    public Single<UserEntity> getCurrentUser() {
        String userId = sessionManager.getCurrentUserId();

        if (userId != null) {
            return profileDao.getUserById(userId)
                    .switchIfEmpty(Single.error(new Exception("User not found")))
                    .map(user -> {
                        user.setPassword(null);
                        return user;
                    });
        }

        return profileDao.getLoggedInUser()
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
        return profileDao.getUserById(userId)
                .switchIfEmpty(Single.error(new Exception("User not found")))
                .map(user -> {
                    user.setPassword(null);
                    return user;
                });
    }


    public Single<Boolean> updateProfile(String userId, String fullName, String email) {
        return checkEmailAvailability(userId, email)
                .flatMap(isAvailable -> {
                    if (!isAvailable) {
                        return Single.error(new Exception("Email already in use"));
                    }
                    return profileDao.updateProfile(
                            userId, fullName, email, System.currentTimeMillis()
                    ).map(rows -> {
                        if (rows > 0) {
                            sessionManager.updateUserInfo(fullName, email);
                            Log.d(TAG, "Profile updated locally");
                            return true;
                        }
                        return false;
                    });
                });
    }

    public Single<Boolean> updateProfileWithPendingSync(String userId, String fullName, String email) {
        return checkEmailAvailability(userId, email)
                .flatMap(isAvailable -> {
                    if (!isAvailable) {
                        return Single.error(new Exception("Email already in use"));
                    }
                    return profileDao.updateProfileWithPendingSync(
                            userId, fullName, email,
                            UserEntity.SYNC_ACTION_UPDATE,
                            System.currentTimeMillis()
                    ).map(rows -> {
                        if (rows > 0) {
                            sessionManager.updateUserInfo(fullName, email);
                            Log.d(TAG, "Profile updated with pending sync");
                            return true;
                        }
                        return false;
                    });
                });
    }


    public Single<Boolean> verifyPassword(String userId, String password) {
        return profileDao.getUserById(userId)
                .switchIfEmpty(Single.error(new Exception("User not found")))
                .map(user -> PasswordHasher.verifyPassword(password, user.getPassword()));
    }

    public Single<Boolean> updatePassword(String userId, String hashedPassword) {
        return profileDao.updatePassword(userId, hashedPassword, System.currentTimeMillis())
                .map(rows -> rows > 0);
    }

    public Single<Boolean> updatePasswordWithPendingSync(String userId, String hashedPassword,
                                                         String oldPlainPassword,
                                                         String newPlainPassword) {
        return profileDao.updatePasswordWithPendingSync(
                userId, hashedPassword, true,
                oldPlainPassword, newPlainPassword,
                System.currentTimeMillis()
        ).map(rows -> rows > 0);
    }


    public Single<List<UserEntity>> getAllPendingSyncUsers() {
        return profileDao.getAllPendingSyncUsers();
    }

    public Single<Boolean> clearPendingSyncFlag(String userId) {
        long now = System.currentTimeMillis();
        return profileDao.clearPendingSyncFlag(userId, now, now)
                .map(rows -> rows > 0);
    }


    public Maybe<UserEntity> getPendingPasswordSyncUser() {
        return profileDao.getPendingPasswordSyncUser();
    }

    public Single<Boolean> clearPasswordSyncFlag(String userId) {
        long now = System.currentTimeMillis();
        return profileDao.clearPasswordSyncFlag(userId, now, now)
                .map(rows -> rows > 0);
    }


    public Single<Boolean> isEmailExists(String email) {
        return profileDao.isEmailExists(email);
    }


    private Single<Boolean> checkEmailAvailability(String userId, String email) {
        return profileDao.getUserById(userId)
                .switchIfEmpty(Single.error(new Exception("User not found")))
                .flatMap(user -> {
                    if (user.getEmail() != null && user.getEmail().equals(email)) {
                        return Single.just(true);
                    }
                    return profileDao.isEmailTakenByOther(email, userId)
                            .map(taken -> !taken);
                });
    }
}
