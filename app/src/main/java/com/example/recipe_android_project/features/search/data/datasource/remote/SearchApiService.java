package com.example.recipe_android_project.features.search.data.datasource.remote;

import com.example.recipe_android_project.features.home.data.dto.area.AreaResponseDto;
import com.example.recipe_android_project.features.home.data.dto.meal.MealResponseDto;
import com.example.recipe_android_project.features.search.data.dto.filter_result.FilterResultResponseDto;
import com.example.recipe_android_project.features.search.data.dto.ingradient.IngredientsResponseDto;

import io.reactivex.rxjava3.core.Single;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface SearchApiService {
    @GET("search.php")
    Single<MealResponseDto> getMealsByName(@Query("s") String name);
    @GET("filter.php")
    Single<FilterResultResponseDto> filterByIngredient(@Query("i") String ingredient);
    @GET("filter.php")
    Call<FilterResultResponseDto> FilterByMealCategory(@Query("c") String category);
    @GET("filter.php")
    Single<FilterResultResponseDto> filterByArea(@Query("a") String area);
    @GET("list.php?a=list")
    Single<AreaResponseDto> getAllAreas();
    @GET("list.php?i=list")
    Single<IngredientsResponseDto> getAllIngredients();
    @GET("lookup.php")
    Single<MealResponseDto> getMealById(@Query("i") String id);
}
