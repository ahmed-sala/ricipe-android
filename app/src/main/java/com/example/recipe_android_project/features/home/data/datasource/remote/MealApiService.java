package com.example.recipe_android_project.features.home.data.datasource.remote;

import com.example.recipe_android_project.features.home.data.dto.area.AreaResponseDto;
import com.example.recipe_android_project.features.home.data.dto.category.CategoryResponseDto;
import com.example.recipe_android_project.features.home.data.dto.meal.MealResponseDto;
import com.example.recipe_android_project.features.search.data.dto.filter_result.FilterResultResponseDto;

import io.reactivex.rxjava3.core.Single;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface MealApiService {

    @GET("random.php")
    Single<MealResponseDto> getRandomMeal();

    @GET("lookup.php")
    Single<MealResponseDto> getMealById(@Query("i") String id);

    @GET("categories.php")
    Single<CategoryResponseDto> getCategories();

    @GET("list.php")
    Single<CategoryResponseDto> getCategoryList(@Query("l") String list);

    @GET("search.php")
    Single<MealResponseDto> getMealsByFirstLetter(@Query("f") String firstLetter);

    @GET("filter.php")
    Single<FilterResultResponseDto> filterByMealCategory(@Query("c") String category);
}
