package com.example.recipe_android_project.features.plan.data.entity;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;

import java.io.Serializable;

@Entity(
        tableName = "meal_plans",
        primaryKeys = {"user_id", "date", "meal_type"},
        indices = {
                @Index(value = {"user_id", "date"}),
                @Index(value = {"user_id", "date", "meal_type"}, unique = true)
        }
)
public class MealPlanEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @NonNull
    @ColumnInfo(name = "user_id")
    private String userId;

    @NonNull
    @ColumnInfo(name = "date")
    private String date;

    @NonNull
    @ColumnInfo(name = "meal_type")
    private String mealType;

    @NonNull
    @ColumnInfo(name = "meal_id")
    private String mealId;

    @Nullable
    @ColumnInfo(name = "meal_name")
    private String mealName;

    @Nullable
    @ColumnInfo(name = "meal_thumbnail")
    private String mealThumbnail;

    @Nullable
    @ColumnInfo(name = "meal_category")
    private String mealCategory;

    @Nullable
    @ColumnInfo(name = "meal_area")
    private String mealArea;

    @ColumnInfo(name = "created_at")
    private long createdAt;

    @ColumnInfo(name = "updated_at")
    private long updatedAt;

    @ColumnInfo(name = "is_synced", defaultValue = "0")
    private boolean isSynced;

    public MealPlanEntity() {
        this.userId = "";
        this.date = "";
        this.mealType = "";
        this.mealId = "";
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.isSynced = false;
    }

    public MealPlanEntity(@NonNull String userId, @NonNull String date,
                          @NonNull String mealType, @NonNull String mealId) {
        this.userId = userId;
        this.date = date;
        this.mealType = mealType;
        this.mealId = mealId;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.isSynced = false;
    }


    @NonNull
    public String getUserId() {
        return userId;
    }

    public void setUserId(@NonNull String userId) {
        this.userId = userId;
    }

    @NonNull
    public String getDate() {
        return date;
    }

    public void setDate(@NonNull String date) {
        this.date = date;
    }

    @NonNull
    public String getMealType() {
        return mealType;
    }

    public void setMealType(@NonNull String mealType) {
        this.mealType = mealType;
    }

    @NonNull
    public String getMealId() {
        return mealId;
    }

    public void setMealId(@NonNull String mealId) {
        this.mealId = mealId;
    }

    @Nullable
    public String getMealName() {
        return mealName;
    }

    public void setMealName(@Nullable String mealName) {
        this.mealName = mealName;
    }

    @Nullable
    public String getMealThumbnail() {
        return mealThumbnail;
    }

    public void setMealThumbnail(@Nullable String mealThumbnail) {
        this.mealThumbnail = mealThumbnail;
    }

    @Nullable
    public String getMealCategory() {
        return mealCategory;
    }

    public void setMealCategory(@Nullable String mealCategory) {
        this.mealCategory = mealCategory;
    }

    @Nullable
    public String getMealArea() {
        return mealArea;
    }

    public void setMealArea(@Nullable String mealArea) {
        this.mealArea = mealArea;
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

    public boolean isSynced() {
        return isSynced;
    }

    public void setSynced(boolean synced) {
        isSynced = synced;
    }
}
