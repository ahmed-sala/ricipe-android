package com.example.recipe_android_project.features.profile.data.repository;


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

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
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

    // ==================== GET PROFILE ====================

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

    // ==================== UPDATE PROFILE ====================

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

                        Log.d(TAG, "Offline - marking for pending sync");
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

    // ==================== CHANGE PASSWORD ====================

    public Single<PasswordChangeResult> changePassword(String userId,
                                                       String oldPassword,
                                                       String newPassword) {
        return Single.defer(() ->
                localDatasource.verifyPassword(userId, oldPassword)
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
                        })
        ).subscribeOn(Schedulers.io());
    }

    // ==================== EMAIL CHECK ====================

    public Single<Boolean> isEmailExists(String email) {
        return localDatasource.isEmailExists(email)
                .subscribeOn(Schedulers.io());
    }

    // ==================== SYNC ALL PENDING ====================

    public Completable syncAllPendingUpdates() {
        return Completable.mergeArray(
                syncAllPendingUserUpdates(),
                syncPendingPasswordChanges()
        ).subscribeOn(Schedulers.io());
    }

    // ==================== SYNC PENDING USER UPDATES ====================

    public Completable syncAllPendingUserUpdates() {
        return Completable.defer(() -> {
            if (!remoteDatasource.isNetworkAvailable()) {
                Log.d(TAG, "Skipping user sync - no network");
                return Completable.complete();
            }

            return localDatasource.getAllPendingSyncUsers()
                    .flatMapCompletable(users -> {
                        if (users == null || users.isEmpty()) {
                            Log.d(TAG, "No pending user syncs");
                            return Completable.complete();
                        }

                        Log.d(TAG, "Syncing " + users.size() + " pending users");

                        return Flowable.fromIterable(users)
                                .flatMapCompletable(
                                        this::syncSingleUser, false, 1
                                );
                    })
                    .onErrorComplete();
        }).subscribeOn(Schedulers.io());
    }

    private Completable syncSingleUser(UserEntity userEntity) {
        if (!remoteDatasource.isNetworkAvailable()) {
            return Completable.complete();
        }

        return remoteDatasource.saveProfileToFirestore(userEntity)
                .timeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .andThen(localDatasource.clearPendingSyncFlag(userEntity.getId())
                        .flatMapCompletable(success -> Completable.complete())
                )
                .doOnComplete(() ->
                        Log.d(TAG, "User synced: " + userEntity.getId()))
                .onErrorComplete(error -> {
                    Log.e(TAG, "Failed to sync user " + userEntity.getId()
                            + ": " + error.getMessage());
                    return true; // Continue with other users
                });
    }

    // ==================== SYNC PENDING PASSWORD CHANGES ====================

    public Completable syncPendingPasswordChanges() {
        return Completable.defer(() -> {
            if (!remoteDatasource.isNetworkAvailable()) {
                Log.d(TAG, "Skipping password sync - no network");
                return Completable.complete();
            }

            return localDatasource.getPendingPasswordSyncUser()
                    .flatMapCompletable(userEntity -> {
                        if (!userEntity.isPendingPasswordSync()) {
                            Log.d(TAG, "No pending password sync");
                            return Completable.complete();
                        }

                        String oldPassword = userEntity.getPendingOldPassword();
                        String newPassword = userEntity.getPendingNewPassword();

                        // Validate stored passwords
                        if (!isValidPendingPassword(oldPassword)
                                || !isValidPendingPassword(newPassword)) {
                            Log.w(TAG, "Invalid pending passwords, clearing flag");
                            return clearPasswordSyncFlagAsCompletable(userEntity.getId());
                        }

                        if (!remoteDatasource.isNetworkAvailable()) {
                            return Completable.complete();
                        }

                        return remoteDatasource.updatePasswordWithStoredCredentials(
                                        userEntity.getEmail(),
                                        oldPassword,
                                        newPassword
                                )
                                .timeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                                .andThen(clearPasswordSyncFlagAsCompletable(userEntity.getId()))
                                .doOnComplete(() ->
                                        Log.d(TAG, "Password synced successfully"))
                                .onErrorResumeNext(error -> {
                                    Log.e(TAG, "Password sync error: "
                                            + error.getMessage());
                                    return Completable.complete();
                                });
                    })
                    .onErrorComplete();
        }).subscribeOn(Schedulers.io());
    }

    // ==================== PRIVATE - PROFILE SYNC ====================

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
                                    Log.w(TAG, "Firebase sync failed: "
                                            + error.getMessage());
                                    return markProfileForPendingSync(
                                            userId, fullName, email);
                                })
                );
    }

    private Single<Boolean> markProfileForPendingSync(String userId,
                                                      String fullName,
                                                      String email) {
        return localDatasource.updateProfileWithPendingSync(userId, fullName, email)
                .doOnSuccess(result -> {
                    if (result) {
                        Log.d(TAG, "Profile marked for pending sync");
                    }
                })
                .onErrorReturnItem(true);
    }

    // ==================== PRIVATE - PASSWORD ====================

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
                                    Log.d(TAG, "Password changed online");
                                    return PasswordChangeResult.success();
                                }
                                return PasswordChangeResult.failure(
                                        "Failed to update local password");
                            });
                }))
                .onErrorResumeNext(error -> {
                    Log.w(TAG, "Online password change failed: "
                            + error.getMessage());

                    if (isNetworkError(error) || isReauthError(error)) {
                        return changePasswordOffline(userId, oldPassword, newPassword);
                    }

                    return Single.error(error);
                });
    }

    private Single<PasswordChangeResult> changePasswordOffline(String userId,
                                                               String oldPassword,
                                                               String newPassword) {
        String hashed = PasswordHasher.hashPassword(newPassword);

        return localDatasource.updatePasswordWithPendingSync(
                userId, hashed, oldPassword, newPassword
        ).map(success -> {
            if (success) {
                Log.d(TAG, "Password changed offline with pending sync");
                return PasswordChangeResult.successWithPendingSync();
            }
            return PasswordChangeResult.failure("Failed to update password");
        });
    }

    // ==================== PRIVATE - HELPERS ====================

    @SuppressLint("CheckResult")
    private void clearSyncFlagSilently(String userId) {
        localDatasource.clearPendingSyncFlag(userId)
                .subscribeOn(Schedulers.io())
                .subscribe(
                        result -> Log.d(TAG, "Sync flag cleared"),
                        error -> Log.e(TAG, "Failed to clear sync flag: "
                                + error.getMessage())
                );
    }

    private Completable clearPasswordSyncFlagAsCompletable(String userId) {
        return localDatasource.clearPasswordSyncFlag(userId)
                .flatMapCompletable(success -> Completable.complete());
    }

    private boolean isValidPendingPassword(String password) {
        return password != null && !password.isEmpty();
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
