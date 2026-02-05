package com.example.recipe_android_project.features.search.data.repository;

import com.example.recipe_android_project.core.config.ResultCallback;
import com.example.recipe_android_project.features.home.data.mapper.AreaMapper;
import com.example.recipe_android_project.features.home.data.mapper.MealMapper;
import com.example.recipe_android_project.features.home.model.Area;
import com.example.recipe_android_project.features.home.model.AreaList;
import com.example.recipe_android_project.features.home.model.Meal;
import com.example.recipe_android_project.features.search.data.datasource.remote.SearchRemoteDataSource;
import com.example.recipe_android_project.features.search.data.mapper.FilterResultMapper;
import com.example.recipe_android_project.features.search.data.mapper.IngredientMapper;
import com.example.recipe_android_project.features.search.domain.model.FilterResultList;
import com.example.recipe_android_project.features.search.domain.model.Ingredient;
import com.example.recipe_android_project.features.search.domain.model.IngredientList;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SearchRepository {

    private final SearchRemoteDataSource remoteDataSource;
    private IngredientList cachedIngredients;
    private AreaList cachedAreas;

    public SearchRepository() {
        this.remoteDataSource = new SearchRemoteDataSource();
    }

    public Single<List<Meal>> searchMealsByName(String name) {
        return remoteDataSource.searchMealsByName(name)
                .map(response -> {
                    if (response == null || response.getMeals() == null) {
                        return new ArrayList<Meal>();
                    }
                    return MealMapper.toDomainList(response.getMeals());
                });
    }

    public Single<Meal> getMealById(String id) {
        return remoteDataSource.getMealById(id)
                .flatMap(response -> {
                    if (response == null || response.getMeals() == null || response.getMeals().isEmpty()) {
                        return Single.error(new Exception("Meal not found"));
                    }
                    return Single.just(MealMapper.toDomain(response.getMeals().get(0)));
                });
    }

    public Single<FilterResultList> filterMealsByIngredient(String ingredient) {
        return remoteDataSource.filterMealsByIngredient(ingredient)
                .map(FilterResultMapper::toDomain);
    }

    public Single<FilterResultList> filterMealsByArea(String area) {
        return remoteDataSource.filterMealsByArea(area)
                .map(FilterResultMapper::toDomain);
    }

    public Single<List<Area>> getAllAreas() {
        return remoteDataSource.getAllAreas()
                .map(AreaMapper::toDomain)
                .doOnSuccess(areaList -> cachedAreas = areaList)
                .map(AreaList::getAreas);
    }

    public Single<List<Ingredient>> getAllIngredients() {
        return remoteDataSource.getAllIngredients()
                .map(IngredientMapper::toDomain)
                .doOnSuccess(ingredientList -> cachedIngredients = ingredientList)
                .map(IngredientList::getIngredients);
    }

    public Single<List<Ingredient>> searchIngredientsByName(String query) {
        String searchQuery = query.toLowerCase().trim();

        return getIngredientsSource()
                .flattenAsObservable(IngredientList::getIngredients)
                .filter(ingredient ->
                        ingredient != null &&
                                ingredient.getName() != null &&
                                ingredient.getName().toLowerCase().contains(searchQuery))
                .toList();
    }

    public Single<List<Area>> searchAreasByName(String query) {
        String searchQuery = query.toLowerCase().trim();

        return getAreasSource()
                .flattenAsObservable(AreaList::getAreas)
                .filter(area ->
                        area != null &&
                                area.getName() != null &&
                                area.getName().toLowerCase().contains(searchQuery))
                .toList();
    }

    private Single<IngredientList> getIngredientsSource() {
        if (cachedIngredients != null && !cachedIngredients.isEmpty()) {
            return Single.just(cachedIngredients);
        }
        return remoteDataSource.getAllIngredients()
                .map(IngredientMapper::toDomain)
                .doOnSuccess(ingredientList -> cachedIngredients = ingredientList);
    }

    private Single<AreaList> getAreasSource() {
        if (cachedAreas != null && !cachedAreas.isEmpty()) {
            return Single.just(cachedAreas);
        }
        return remoteDataSource.getAllAreas()
                .map(AreaMapper::toDomain)
                .doOnSuccess(areaList -> cachedAreas = areaList);
    }

    public void clearCache() {
        cachedIngredients = null;
        cachedAreas = null;
    }
}
