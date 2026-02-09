package com.example.recipe_android_project.features.meal_detail.domain.model;


import androidx.annotation.Nullable;

import java.io.Serializable;

public class MealPlan implements Serializable {

    private static final long serialVersionUID = 1L;

    private String userId;
    private String date;
    private String mealType;
    private String mealId;

    @Nullable
    private String mealName;

    @Nullable
    private String mealThumbnail;

    @Nullable
    private String mealCategory;

    @Nullable
    private String mealArea;

    private long createdAt;
    private long updatedAt;
    private boolean isSynced;

    public MealPlan() {
        this.userId = "";
        this.date = "";
        this.mealType = "";
        this.mealId = "";
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.isSynced = false;
    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMealType() {
        return mealType;
    }

    public void setMealType(String mealType) {
        this.mealType = mealType;
    }

    public String getMealId() {
        return mealId;
    }

    public void setMealId(String mealId) {
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

    @Override
    public String toString() {
        return "MealPlan{" +
                "userId='" + userId + '\'' +
                ", date='" + date + '\'' +
                ", mealType='" + mealType + '\'' +
                ", mealId='" + mealId + '\'' +
                ", mealName='" + mealName + '\'' +
                '}';
    }
}
