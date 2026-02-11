package com.example.recipe_android_project.features.profile.data.datasource.repository;


import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import com.example.recipe_android_project.core.helper.UserSessionManager;
import com.example.recipe_android_project.core.utils.PasswordHasher;
import com.example.recipe_android_project.features.auth.data.entities.UserEntity;
import com.example.recipe_android_project.features.auth.data.mapper.UserMapper;
import com.example.recipe_android_project.features.auth.domain.model.PasswordChangeResult;
import com.example.recipe_android_project.features.auth.domain.model.User;
import com.example.recipe_android_project.features.profile.data.datasource.local.ProfileLocalDatasource;
import com.example.recipe_android_project.features.profile.data.datasource.remote.ProfileRemoteDatasource;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ProfileRepository {

    private static final String TAG = "ProfileRepository";
    private static final int TIMEOUT_SECONDS = 10;

    private final ProfileLocalDatasource localDatasource;
    private final ProfileRemoteDatasource remoteDatasource;
    private final UserSessionManager sessionManager;

    public ProfileRepository(Context context) {
        this.localDatasource = new ProfileLocalDatasource(context);
        this.remoteDatasource = new ProfileRemoteDatasource(context);
        this.sessionManager = UserSessionManager.getInstance(context);
    }


    public Single<User> getCurrentUser() {
        return localDatasource.getCurrentUser()
                .map(UserMapper::toDomain)
                .subscribeOn(Schedulers.io());
    }

    public String getCurrentUserId() {
        return sessionManager.getCurrentUserId();
    }

    public boolean isNetworkAvailable() {
        return remoteDatasource.isNetworkAvailable();
    }


    public Single<Boolean> updateProfile(String fullName, String email) {
        return Single.defer(() -> {
            String userId = sessionManager.getCurrentUserId();
            if (userId == null) {
                return Single.error(new Exception("User not found"));
            }

            return localDatasource.updateProfile(userId, fullName, email)
                    .flatMap(localSuccess -> {
                        if (!localSuccess) {
                            return Single.just(false);
                        }

                        if (remoteDatasource.isNetworkAvailable()) {
                            return syncProfileToFirebase(userId, fullName, email);
                        }

                        return markProfileForPendingSync(userId, fullName, email);
                    });
        }).subscribeOn(Schedulers.io());
    }

    public Single<Boolean> updateProfileOffline(String fullName, String email) {
        return Single.defer(() -> {
            String userId = sessionManager.getCurrentUserId();
            if (userId == null) {
                return Single.error(new Exception("User not found"));
            }

            return localDatasource.updateProfileWithPendingSync(userId, fullName, email);
        }).subscribeOn(Schedulers.io());
    }


    public Single<PasswordChangeResult> changePassword(String userId,
                                                       String oldPassword,
                                                       String newPassword) {
        return Single.defer(() -> {
            return localDatasource.verifyPassword(userId, oldPassword)
                    .flatMap(isValid -> {
                        if (!isValid) {
                            return Single.error(
                                    new Exception("Current password is incorrect"));
                        }

                        if (remoteDatasource.isNetworkAvailable()
                                && remoteDatasource.isFirebaseAuthenticated()) {
                            return changePasswordOnline(userId, oldPassword, newPassword);
                        }

                        return changePasswordOffline(userId, oldPassword, newPassword);
                    });
        }).subscribeOn(Schedulers.io());
    }


    public Single<Boolean> isEmailExists(String email) {
        return localDatasource.isEmailExists(email)
                .subscribeOn(Schedulers.io());
    }


    private Single<Boolean> syncProfileToFirebase(String userId, String fullName, String email) {
        return localDatasource.getUserById(userId)
                .flatMap(userEntity ->
                        remoteDatasource.saveProfileToFirestore(userEntity)
                                .timeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                                .toSingleDefault(true)
                                .doOnSuccess(result -> {
                                    Log.d(TAG, "Profile synced to Firebase");
                                    clearSyncFlagSilently(userId);
                                })
                                .onErrorResumeNext(error -> {
                                    Log.w(TAG, "Firebase sync failed: " + error.getMessage());
                                    return markProfileForPendingSync(userId, fullName, email);
                                })
                );
    }

    private Single<Boolean> markProfileForPendingSync(String userId, String fullName, String email) {
        return localDatasource.updateProfileWithPendingSync(userId, fullName, email)
                .doOnSuccess(result -> {
                    if (result) {
                        Log.d(TAG, "Profile marked for pending sync");
                    }
                })
                .onErrorReturnItem(true);
    }

    @SuppressLint("CheckResult")
    private void clearSyncFlagSilently(String userId) {
        localDatasource.clearPendingSyncFlag(userId)
                .subscribeOn(Schedulers.io())
                .subscribe(
                        result -> Log.d(TAG, "Sync flag cleared"),
                        error -> Log.e(TAG, "Failed to clear sync flag: " + error.getMessage())
                );
    }


    private Single<PasswordChangeResult> changePasswordOnline(String userId,
                                                              String oldPassword,
                                                              String newPassword) {
        return remoteDatasource.updatePassword(oldPassword, newPassword)
                .timeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .andThen(Single.defer(() -> {
                    String hashed = PasswordHasher.hashPassword(newPassword);
                    return localDatasource.updatePassword(userId, hashed)
                            .map(success -> {
                                if (success) {
                                    return PasswordChangeResult.success();
                                }
                                return PasswordChangeResult.failure("Failed to update local password");
                            });
                }))
                .onErrorResumeNext(error -> {

                    if (isNetworkError(error)) {
                        return changePasswordOffline(userId, oldPassword, newPassword);
                    }

                    if (isReauthError(error)) {
                        return changePasswordOffline(userId, oldPassword, newPassword);
                    }

                    return Single.error(error);
                });
    }

    private Single<PasswordChangeResult> changePasswordOffline(String userId,
                                                               String oldPassword,
                                                               String newPassword) {
        String hashed = PasswordHasher.hashPassword(newPassword);

        return localDatasource.updatePasswordWithPendingSync(userId, hashed, oldPassword, newPassword)
                .map(success -> {
                    if (success) {
                        return PasswordChangeResult.successWithPendingSync();
                    }
                    return PasswordChangeResult.failure("Failed to update password");
                });
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
                || lower.contains("failed to connect");
    }

    private boolean isReauthError(Throwable error) {
        String message = error.getMessage();
        return message != null && message.contains("Re-authentication failed");
    }
}
