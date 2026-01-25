package com.example.recipe_android_project.data.local.entities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "categories")
public class CategoryEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    private String id;

    @Nullable
    @ColumnInfo(name = "name")
    private String name;

    @Nullable
    @ColumnInfo(name = "thumbnail_url")
    private String thumbnailUrl;

    @Nullable
    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "created_at")
    private long createdAt;

    // ==================== CONSTRUCTOR ====================

    public CategoryEntity() {
        this.id = "";
        this.createdAt = System.currentTimeMillis();
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
    public String getName() {
        return name;
    }

    public void setName(@Nullable String name) {
        this.name = name;
    }

    @Nullable
    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(@Nullable String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    @Nullable
    public String getDescription() {
        return description;
    }

    public void setDescription(@Nullable String description) {
        this.description = description;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
