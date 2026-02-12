package com.example.recipe_android_project.features.profile.data.datasource.local;

import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Update;

import com.example.recipe_android_project.features.auth.data.entities.UserEntity;

import java.util.List;

import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

@Dao
public interface ProfileDao {


    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    Maybe<UserEntity> getUserById(String userId);

    @Query("SELECT * FROM users WHERE is_logged_in = 1 LIMIT 1")
    Maybe<UserEntity> getLoggedInUser();

    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE email = :email)")
    Single<Boolean> isEmailExists(String email);

    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE email = :email AND id != :userId)")
    Single<Boolean> isEmailTakenByOther(String email, String userId);


    @Query("UPDATE users SET full_name = :fullName, email = :email, updated_at = :updatedAt WHERE id = :userId")
    Single<Integer> updateProfile(String userId, String fullName, String email, long updatedAt);

    @Query("UPDATE users SET full_name = :fullName, email = :email, " +
            "pending_sync = 1, pending_sync_action = :syncAction, " +
            "updated_at = :updatedAt WHERE id = :userId")
    Single<Integer> updateProfileWithPendingSync(String userId, String fullName,
                                                 String email, String syncAction,
                                                 long updatedAt);
    @Query("UPDATE users SET password = :hashedPassword, updated_at = :updatedAt WHERE id = :userId")
    Single<Integer> updatePassword(String userId, String hashedPassword, long updatedAt);

    @Query("UPDATE users SET password = :hashedPassword, " +
            "pending_password_sync = :pendingSync, " +
            "pending_old_password = :oldPassword, " +
            "pending_new_password = :newPassword, " +
            "updated_at = :updatedAt WHERE id = :userId")
    Single<Integer> updatePasswordWithPendingSync(String userId, String hashedPassword,
                                                  boolean pendingSync, String oldPassword,
                                                  String newPassword, long updatedAt);


    @Query("SELECT * FROM users WHERE pending_sync = 1")
    Single<List<UserEntity>> getAllPendingSyncUsers();

    @Query("UPDATE users SET pending_sync = 0, pending_sync_action = NULL, " +
            "last_synced_at = :syncedAt, updated_at = :updatedAt WHERE id = :userId")
    Single<Integer> clearPendingSyncFlag(String userId, long syncedAt, long updatedAt);


    @Query("SELECT * FROM users WHERE pending_password_sync = 1 LIMIT 1")
    Maybe<UserEntity> getPendingPasswordSyncUser();

    @Query("UPDATE users SET pending_password_sync = 0, " +
            "pending_old_password = NULL, pending_new_password = NULL, " +
            "last_synced_at = :syncedAt, updated_at = :updatedAt WHERE id = :userId")
    Single<Integer> clearPasswordSyncFlag(String userId, long syncedAt, long updatedAt);
}
