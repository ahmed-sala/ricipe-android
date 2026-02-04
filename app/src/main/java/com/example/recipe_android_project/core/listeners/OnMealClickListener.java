package com.example.recipe_android_project.core.listeners;

import com.example.recipe_android_project.features.home.model.Meal;

public interface OnMealClickListener {
    void onMealClick(Meal meal, int position);
    void onFavoriteClick(Meal meal, int position, boolean isFavorite);
}
