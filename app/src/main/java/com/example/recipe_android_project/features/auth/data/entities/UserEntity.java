package com.example.recipe_android_project.features.auth.data.entities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(
        tableName = "users",
        indices = {
                @Index(value = "email", unique = true)
        }
)
public class UserEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String SYNC_ACTION_UPDATE = "UPDATE";


    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    private String id;
    @ColumnInfo(name = "pending_old_password")
    private String pendingOldPassword;
    @Nullable
    @ColumnInfo(name = "full_name")
    private String fullName;

    @Nullable
    @ColumnInfo(name = "email")
    private String email;

    @Nullable
    @ColumnInfo(name = "password")
    private String password;

    @ColumnInfo(name = "is_logged_in", defaultValue = "0")
    private boolean isLoggedIn;

    @ColumnInfo(name = "pending_sync", defaultValue = "0")
    private boolean pendingSync;

    @Nullable
    @ColumnInfo(name = "pending_sync_action")
    private String pendingSyncAction;

    @ColumnInfo(name = "last_synced_at", defaultValue = "0")
    private long lastSyncedAt;


    @ColumnInfo(name = "pending_password_sync", defaultValue = "0")
    private boolean pendingPasswordSync;

    @Nullable
    @ColumnInfo(name = "pending_new_password")
    private String pendingNewPassword;


    @ColumnInfo(name = "pending_registration_sync", defaultValue = "0")
    private boolean pendingRegistrationSync;

    @Nullable
    @ColumnInfo(name = "pending_plain_password")
    private String pendingPlainPassword;


    @ColumnInfo(name = "created_at")
    private long createdAt;

    @ColumnInfo(name = "updated_at")
    private long updatedAt;


    public UserEntity() {
        this.id = "";
        this.pendingSync = false;
        this.pendingPasswordSync = false;
        this.pendingRegistrationSync = false;
        this.lastSyncedAt = 0;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }


    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    @Nullable
    public String getFullName() {
        return fullName;
    }

    public void setFullName(@Nullable String fullName) {
        this.fullName = fullName;
    }

    @Nullable
    public String getEmail() {
        return email;
    }

    public void setEmail(@Nullable String email) {
        this.email = email;
    }

    @Nullable
    public String getPassword() {
        return password;
    }

    public void setPassword(@Nullable String password) {
        this.password = password;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        isLoggedIn = loggedIn;
    }

    public boolean isPendingSync() {
        return pendingSync;
    }

    public void setPendingSync(boolean pendingSync) {
        this.pendingSync = pendingSync;
    }

    @Nullable
    public String getPendingSyncAction() {
        return pendingSyncAction;
    }

    public void setPendingSyncAction(@Nullable String pendingSyncAction) {
        this.pendingSyncAction = pendingSyncAction;
    }

    public long getLastSyncedAt() {
        return lastSyncedAt;
    }

    public void setLastSyncedAt(long lastSyncedAt) {
        this.lastSyncedAt = lastSyncedAt;
    }

    public boolean isPendingPasswordSync() {
        return pendingPasswordSync;
    }

    public void setPendingPasswordSync(boolean pendingPasswordSync) {
        this.pendingPasswordSync = pendingPasswordSync;
    }

    @Nullable
    public String getPendingNewPassword() {
        return pendingNewPassword;
    }

    public void setPendingNewPassword(@Nullable String pendingNewPassword) {
        this.pendingNewPassword = pendingNewPassword;
    }

    public boolean isPendingRegistrationSync() {
        return pendingRegistrationSync;
    }

    public void setPendingRegistrationSync(boolean pendingRegistrationSync) {
        this.pendingRegistrationSync = pendingRegistrationSync;
    }

    @Nullable
    public String getPendingPlainPassword() {
        return pendingPlainPassword;
    }

    public void setPendingPlainPassword(@Nullable String pendingPlainPassword) {
        this.pendingPlainPassword = pendingPlainPassword;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserEntity that = (UserEntity) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
    public String getPendingOldPassword() {
        return pendingOldPassword;
    }

    public void setPendingOldPassword(String pendingOldPassword) {
        this.pendingOldPassword = pendingOldPassword;
    }
    @Override
    public String toString() {
        return "UserEntity{" +
                "id='" + id + '\'' +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", isLoggedIn=" + isLoggedIn +
                ", pendingSync=" + pendingSync +
                ", pendingSyncAction='" + pendingSyncAction + '\'' +
                ", pendingPasswordSync=" + pendingPasswordSync +
                ", pendingRegistrationSync=" + pendingRegistrationSync +
                '}';
    }
}
