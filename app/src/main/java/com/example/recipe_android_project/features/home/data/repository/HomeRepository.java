package com.example.recipe_android_project.features.home.data.repository;

import com.example.recipe_android_project.features.home.data.datasource.remote.HomeRemoteDatasource;
import com.example.recipe_android_project.features.home.data.mapper.CategoryMapper;
import com.example.recipe_android_project.features.home.data.mapper.MealMapper;
import com.example.recipe_android_project.features.home.model.Category;
import com.example.recipe_android_project.features.home.model.Meal;
import com.example.recipe_android_project.features.search.data.mapper.FilterResultMapper;
import com.example.recipe_android_project.features.search.domain.model.FilterResult;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.core.Single;

public class HomeRepository {

    private final HomeRemoteDatasource remote;

    public HomeRepository() {
        this.remote = new HomeRemoteDatasource();
    }

    public HomeRepository(HomeRemoteDatasource remote) {
        this.remote = remote;
    }

    public Single<Meal> getMealOfTheDay() {
        return remote.getMealOfTheDay()
                .flatMap(response -> {
                    if (response == null || response.getMeals() == null || response.getMeals().isEmpty()) {
                        return Single.error(new Exception("No meal found."));
                    }
                    Meal meal = MealMapper.toDomain(response.getMeals().get(0));
                    if (meal == null) {
                        return Single.error(new Exception("Meal mapping failed."));
                    }
                    return Single.just(meal);
                });
    }

    public Single<List<Category>> getCategories() {
        return remote.getCategories()
                .map(response -> {
                    if (response == null || response.getCategories() == null) {
                        return new ArrayList<Category>();
                    }
                    return CategoryMapper.toDomainList(response.getCategories());
                });
    }

    public Single<List<Meal>> getMealsByFirstLetter(String firstLetter) {
        if (firstLetter == null || firstLetter.trim().isEmpty()) {
            return Single.error(new IllegalArgumentException("firstLetter is required"));
        }

        String f = firstLetter.trim().substring(0, 1);

        return remote.getMealsByFirstLetter(f)
                .map(response -> {
                    if (response == null || response.getMeals() == null) {
                        return new ArrayList<Meal>();
                    }
                    return MealMapper.toDomainList(response.getMeals());
                });
    }

    public Single<List<Meal>> getMealsByCategory(String categoryName) {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            return Single.error(new IllegalArgumentException("categoryName is required"));
        }

        String trimmedCategory = categoryName.trim();

        return remote.getMealsByCategory(trimmedCategory)
                .flatMap(response -> {
                    List<FilterResult> results = FilterResultMapper.toDomainList(response);

                    if (results == null || results.isEmpty()) {
                        return Single.error(new Exception("No meals found for this category."));
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

                    return Single.just(meals);
                });
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
