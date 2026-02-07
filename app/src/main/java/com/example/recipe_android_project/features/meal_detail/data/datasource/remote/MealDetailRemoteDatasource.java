package com.example.recipe_android_project.features.meal_detail.data.datasource.remote;

import com.example.recipe_android_project.core.config.RetrofitClient;
import com.example.recipe_android_project.features.home.data.dto.meal.MealResponseDto;

import io.reactivex.rxjava3.core.Single;

public class MealDetailRemoteDatasource {
    private final MealDetailApiService mealDetailApiService;

    public MealDetailRemoteDatasource() {
        this.mealDetailApiService = RetrofitClient.getMealDetailApiService();
    }
    public Single<MealResponseDto> getMealById(String id) {
        return mealDetailApiService.getMealById(id);
    }
}
