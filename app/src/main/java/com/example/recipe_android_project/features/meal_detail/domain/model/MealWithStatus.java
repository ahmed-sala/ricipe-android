package com.example.recipe_android_project.features.meal_detail.domain.model;

import com.example.recipe_android_project.features.home.model.Meal;

public class MealWithStatus {
    private final Meal meal;
    private final boolean isFavorite;
    private final boolean isPlanned;
    private final String plannedMealType;

    public MealWithStatus(Meal meal, boolean isFavorite, boolean isPlanned, String plannedMealType) {
        this.meal = meal;
        this.isFavorite = isFavorite;
        this.isPlanned = isPlanned;
        this.plannedMealType = plannedMealType;
    }

    public Meal getMeal() {
        return meal;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public boolean isPlanned() {
        return isPlanned;
    }

    public String getPlannedMealType() {
        return plannedMealType;
    }

    public boolean hasPlannedMealType() {
        return plannedMealType != null && !plannedMealType.isEmpty();
    }
}
