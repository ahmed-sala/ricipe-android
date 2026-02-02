package com.example.recipe_android_project.features.home.data.repository;

import com.example.recipe_android_project.core.config.ResultCallback;
import com.example.recipe_android_project.features.home.data.datasource.remote.HomeRemoteDatasource;
import com.example.recipe_android_project.features.home.data.dto.category.CategoryDto;
import com.example.recipe_android_project.features.home.data.dto.category.CategoryResponseDto;
import com.example.recipe_android_project.features.home.data.dto.meal.MealDto;
import com.example.recipe_android_project.features.home.data.dto.meal.MealResponseDto;
import com.example.recipe_android_project.features.home.data.mapper.CategoryMapper;
import com.example.recipe_android_project.features.home.data.mapper.MealMapper;
import com.example.recipe_android_project.features.home.model.Category;
import com.example.recipe_android_project.features.home.model.Meal;
import com.example.recipe_android_project.features.search.data.dto.filter_result.FilterResultResponseDto;
import com.example.recipe_android_project.features.search.data.mapper.FilterResultMapper;
import com.example.recipe_android_project.features.search.domain.model.FilterResult;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeRepository {

    private final HomeRemoteDatasource remote;

    public HomeRepository() {
        this.remote = new HomeRemoteDatasource();
    }
    public void getMealOfTheDay(ResultCallback<Meal> callback) {
        remote.getMealOfTheDayCall().enqueue(new Callback<MealResponseDto>() {
            @Override
            public void onResponse(Call<MealResponseDto> call, Response<MealResponseDto> response) {
                if (response.isSuccessful() && response.body() != null) {

                    List<MealDto> mealsDto = response.body().getMeals();
                    if (mealsDto == null || mealsDto.isEmpty()) {
                        callback.onError(new Exception("No meal found."));
                        return;
                    }

                    Meal meal = MealMapper.toDomain(mealsDto.get(0));
                    if (meal == null) {
                        callback.onError(new Exception("Meal mapping failed."));
                        return;
                    }

                    callback.onSuccess(meal);

                } else {
                    callback.onError(new Exception("Request failed: " + response.code()));
                }
            }

            @Override
            public void onFailure(Call<MealResponseDto> call, Throwable t) {
                callback.onError(new Exception(t));
            }
        });
    }


    public void getCategories(ResultCallback<List<Category>> callback) {
        remote.getCategoriesCall().enqueue(new Callback<CategoryResponseDto>() {
            @Override
            public void onResponse(Call<CategoryResponseDto> call, Response<CategoryResponseDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<CategoryDto> dtos =
                            response.body().getCategories();
                    List<Category> categories = CategoryMapper.toDomainList(dtos);
                    callback.onSuccess(categories);

                } else {
                    callback.onError(new Exception("Request failed: " + response.code()));
                }
            }

            @Override
            public void onFailure(Call<CategoryResponseDto> call, Throwable t) {
                callback.onError(new Exception(t));
            }
        });
    }

    public void getMealsByFirstLetter(String firstLetter, ResultCallback<List<Meal>> callback) {
        if (firstLetter == null || firstLetter.trim().isEmpty()) {
            callback.onError(new IllegalArgumentException("firstLetter is required"));
            return;
        }

        String f = firstLetter.trim().substring(0, 1);

        remote.getMealsByFirstLetterCall(f).enqueue(new Callback<MealResponseDto>() {
            @Override
            public void onResponse(Call<MealResponseDto> call, Response<MealResponseDto> response) {
                if (response.isSuccessful() && response.body() != null) {

                    List<com.example.recipe_android_project.features.home.data.dto.meal.MealDto> dtos =
                            response.body().getMeals();

                    List<Meal> meals = MealMapper.toDomainList(dtos);

                    callback.onSuccess(meals);

                } else {
                    callback.onError(new Exception("Request failed: " + response.code()));
                }
            }

            @Override
            public void onFailure(Call<MealResponseDto> call, Throwable t) {
                callback.onError(new Exception(t));
            }
        });
    }
    public void getMealsByCategory(String categoryName, ResultCallback<List<Meal>> callback) {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            callback.onError(new IllegalArgumentException("categoryName is required"));
            return;
        }

        String trimmedCategory = categoryName.trim();

        remote.getMealsByCategoryCall(trimmedCategory).enqueue(new Callback<FilterResultResponseDto>() {
            @Override
            public void onResponse(Call<FilterResultResponseDto> call, Response<FilterResultResponseDto> response) {
                if (response.isSuccessful() && response.body() != null) {

                    List<FilterResult> results = FilterResultMapper.toDomainList(response.body());
                    if (results == null || results.isEmpty()) {
                        callback.onError(new Exception("No meals found for this category."));
                        return;
                    }

                    List<Meal> meals = new ArrayList<>();
                    for (FilterResult r : results) {
                        Meal m = new Meal();
                        m.setId(String.valueOf(r.getId()));
                        m.setName(r.getName());
                        m.setThumbnailUrl(r.getThumbnailUrl());
                        m.setCategory(trimmedCategory);
                        meals.add(m);
                    }

                    callback.onSuccess(meals);

                } else {
                    callback.onError(new Exception("Request failed: " + response.code()));
                }
            }

            @Override
            public void onFailure(Call<FilterResultResponseDto> call, Throwable t) {
                callback.onError(new Exception(t));
            }
        });
    }
}
