package com.example.recipe_android_project.features.home.data.datasource.remote;

import com.example.recipe_android_project.features.home.data.dto.area.AreaResponseDto;
import com.example.recipe_android_project.features.home.data.dto.category.CategoryResponseDto;
import com.example.recipe_android_project.features.home.data.dto.meal.MealResponseDto;
import com.example.recipe_android_project.features.search.data.dto.filter_result.FilterResultResponseDto;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface MealApiService {
    @GET("random.php")
    Call<MealResponseDto> getRandomMeal();
    @GET("lookup.php")
    Call<MealResponseDto> getMealById(@Query("i") String id);
    @GET("categories.php")
    Call<CategoryResponseDto> getCategories();
    @GET("list.php")
    Call<AreaResponseDto> getAreas(@Query("a") String list);
    @GET("list.php")
    Call<CategoryResponseDto> getCategoryList(@Query("l") String list);
    @GET("search.php")
    Call<MealResponseDto> getMealsByFirstLetter(@Query("f") String firstLetter);

    @GET("filter.php")
    Call<FilterResultResponseDto> filterByMealCategory(@Query("c") String category);
}
