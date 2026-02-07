package com.example.recipe_android_project.features.meal_detail.data.datasource.remote;

import com.example.recipe_android_project.features.home.data.dto.meal.MealResponseDto;

import io.reactivex.rxjava3.core.Single;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface MealDetailApiService {
    @GET("lookup.php")
    Single<MealResponseDto> getMealById(@Query("i") String id);
}
