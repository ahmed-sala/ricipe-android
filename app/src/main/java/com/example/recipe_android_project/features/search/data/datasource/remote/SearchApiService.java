package com.example.recipe_android_project.features.search.data.datasource.remote;

import com.example.recipe_android_project.features.home.data.dto.meal.MealResponseDto;
import com.example.recipe_android_project.features.search.data.dto.filter_result.FilterResultResponseDto;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface SearchApiService {
    @GET("search.php")
    Call<MealResponseDto> getMealsByName(@Query("s") String name);
    @GET("filter.php")
    Call<FilterResultResponseDto> FilterByMealIngredient(@Query("i") String category);
    @GET("filter.php")
    Call<FilterResultResponseDto> FilterByMealCategory(@Query("c") String category);
    @GET("filter.php")
    Call<FilterResultResponseDto> FilterByMealArea(@Query("a") String area);
}
