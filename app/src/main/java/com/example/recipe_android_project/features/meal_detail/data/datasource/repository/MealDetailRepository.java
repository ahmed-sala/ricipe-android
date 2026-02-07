package com.example.recipe_android_project.features.meal_detail.data.datasource.repository;

import com.example.recipe_android_project.features.home.data.mapper.MealMapper;
import com.example.recipe_android_project.features.home.model.Meal;
import com.example.recipe_android_project.features.meal_detail.data.datasource.remote.MealDetailRemoteDatasource;

import io.reactivex.rxjava3.core.Single;

public class MealDetailRepository {
    private final MealDetailRemoteDatasource remote;

    public MealDetailRepository() {
        this.remote = new MealDetailRemoteDatasource();
    }

    public Single<Meal> getMealById(String id) {
        return remote.getMealById(id)
                .flatMap(response -> {
                    if (response == null || response.getMeals() == null || response.getMeals().isEmpty()) {
                        return Single.error(new Exception("Meal not found"));
                    }
                    return Single.just(MealMapper.toDomain(response.getMeals().get(0)));
                });
    }
}
