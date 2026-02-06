package com.example.recipe_android_project.features.home.data.datasource.remote;

import com.example.recipe_android_project.core.config.RetrofitClient;
import com.example.recipe_android_project.features.home.data.dto.category.CategoryResponseDto;
import com.example.recipe_android_project.features.home.data.dto.meal.MealResponseDto;
import com.example.recipe_android_project.features.search.data.dto.filter_result.FilterResultResponseDto;

import io.reactivex.rxjava3.core.Single;

public class HomeRemoteDatasource {

    private final MealApiService mealApiService;

    public HomeRemoteDatasource() {
        this.mealApiService = RetrofitClient.getMealApiService();
    }

    public HomeRemoteDatasource(MealApiService mealApiService) {
        this.mealApiService = mealApiService;
    }

    public Single<MealResponseDto> getMealOfTheDay() {
        return mealApiService.getRandomMeal();
    }

    public Single<CategoryResponseDto> getCategories() {
        return mealApiService.getCategories();
    }

    public Single<MealResponseDto> getMealsByFirstLetter(String firstLetter) {
        return mealApiService.getMealsByFirstLetter(firstLetter);
    }

    public Single<FilterResultResponseDto> getMealsByCategory(String categoryName) {
        return mealApiService.filterByMealCategory(categoryName);
    }

    public Single<MealResponseDto> getMealById(String id) {
        return mealApiService.getMealById(id);
    }
}
