package com.example.recipe_android_project.features.search.data.datasource.remote;

import com.example.recipe_android_project.core.config.RetrofitClient;
import com.example.recipe_android_project.features.home.data.dto.area.AreaResponseDto;
import com.example.recipe_android_project.features.home.data.dto.meal.MealResponseDto;
import com.example.recipe_android_project.features.search.data.dto.filter_result.FilterResultResponseDto;
import com.example.recipe_android_project.features.search.data.dto.ingradient.IngredientsResponseDto;

import io.reactivex.rxjava3.core.Single;

public class SearchRemoteDataSource {

    private final SearchApiService apiService;

    public SearchRemoteDataSource() {
        this.apiService = RetrofitClient.getSearchApiService();
    }

    public SearchRemoteDataSource(SearchApiService apiService) {
        this.apiService = apiService;
    }


    public Single<MealResponseDto> searchMealsByName(String name) {
        return apiService.getMealsByName(name);
    }



    public Single<MealResponseDto> getMealById(String id) {
        return apiService.getMealById(id);
    }


    public Single<FilterResultResponseDto> filterMealsByIngredient(String ingredient) {
        return apiService.filterByIngredient(ingredient);
    }



    public Single<FilterResultResponseDto> filterMealsByArea(String area) {
        return apiService.filterByArea(area);
    }


    public Single<AreaResponseDto> getAllAreas() {
        return apiService.getAllAreas();
    }

    public Single<IngredientsResponseDto> getAllIngredients() {
        return apiService.getAllIngredients();
    }
}
