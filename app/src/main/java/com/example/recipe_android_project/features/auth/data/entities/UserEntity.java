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

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    private String id;

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

    @ColumnInfo(name = "created_at")
    private long createdAt;

    @ColumnInfo(name = "updated_at")
    private long updatedAt;

    public UserEntity() {
        this.id = "";
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    // ==================== GETTERS & SETTERS ====================

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
}
