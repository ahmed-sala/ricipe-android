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
    private final CompositeDisposable disposables = new CompositeDisposable();

    private IngredientList cachedIngredients;
    private AreaList cachedAreas;

    public SearchRepository() {
        this.remoteDataSource = new SearchRemoteDataSource();
    }

    public void searchMealsByName(String name, ResultCallback<List<Meal>> callback) {
        Disposable disposable = remoteDataSource.searchMealsByName(name)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(response -> {
                    if (response == null || response.getMeals() == null) {
                        return new ArrayList<Meal>();
                    }
                    return MealMapper.toDomainList(response.getMeals());
                })
                .subscribe(
                        callback::onSuccess,
                        throwable -> callback.onError(new Exception(throwable.getMessage(), throwable))
                );
        disposables.add(disposable);
    }



    public void getMealById(String id, ResultCallback<Meal> callback) {
        Disposable disposable = remoteDataSource.getMealById(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> {
                            if (response == null || response.getMeals() == null || response.getMeals().isEmpty()) {
                                callback.onError(new Exception("Meal not found"));
                            } else {
                                Meal meal = MealMapper.toDomain(response.getMeals().get(0));
                                callback.onSuccess(meal);
                            }
                        },
                        throwable -> callback.onError(new Exception(throwable.getMessage(), throwable))
                );
        disposables.add(disposable);
    }


    public void filterMealsByIngredient(String ingredient, ResultCallback<FilterResultList> callback) {
        Disposable disposable = remoteDataSource.filterMealsByIngredient(ingredient)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(FilterResultMapper::toDomain)
                .subscribe(
                        callback::onSuccess,
                        throwable -> callback.onError(new Exception(throwable.getMessage(), throwable))
                );
        disposables.add(disposable);
    }
    public void filterMealsByArea(String area, ResultCallback<FilterResultList> callback) {
        Disposable disposable = remoteDataSource.filterMealsByArea(area)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(FilterResultMapper::toDomain)
                .subscribe(
                        callback::onSuccess,
                        throwable -> callback.onError(new Exception(throwable.getMessage(), throwable))
                );
        disposables.add(disposable);
    }




    public void getAllAreas(ResultCallback<List<Area>> callback) {
        Disposable disposable = remoteDataSource.getAllAreas()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(AreaMapper::toDomain)
                .doOnSuccess(areaList -> cachedAreas = areaList)
                .map(AreaList::getAreas)
                .subscribe(
                        callback::onSuccess,
                        throwable -> callback.onError(new Exception(throwable.getMessage(), throwable))
                );
        disposables.add(disposable);
    }



    public void getAllIngredients(ResultCallback<List<Ingredient>> callback) {
        Disposable disposable = remoteDataSource.getAllIngredients()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(IngredientMapper::toDomain)
                .doOnSuccess(ingredientList -> cachedIngredients = ingredientList)
                .map(IngredientList::getIngredients)
                .subscribe(
                        callback::onSuccess,
                        throwable -> callback.onError(new Exception(throwable.getMessage(), throwable))
                );
        disposables.add(disposable);
    }


    public void searchIngredientsByName(String query, ResultCallback<List<Ingredient>> callback) {
        String searchQuery = query.toLowerCase().trim();

        Single<IngredientList> source = getIngredientsSource();

        Disposable disposable = source
                .subscribeOn(Schedulers.io())
                .flattenAsObservable(IngredientList::getIngredients)
                .filter(ingredient ->
                        ingredient != null &&
                                ingredient.getName() != null &&
                                ingredient.getName().toLowerCase().contains(searchQuery)
                )
                .toList()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        callback::onSuccess,
                        throwable -> callback.onError(new Exception(throwable.getMessage(), throwable))
                );
        disposables.add(disposable);
    }

    public void searchAreasByName(String query, ResultCallback<List<Area>> callback) {
        String searchQuery = query.toLowerCase().trim();

        Single<AreaList> source = getAreasSource();

        Disposable disposable = source
                .subscribeOn(Schedulers.io())
                .flattenAsObservable(AreaList::getAreas)
                .filter(area ->
                        area != null &&
                                area.getName() != null &&
                                area.getName().toLowerCase().contains(searchQuery)
                )
                .toList()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        callback::onSuccess,
                        throwable -> callback.onError(new Exception(throwable.getMessage(), throwable))
                );
        disposables.add(disposable);
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

    public boolean hasIngredientsCache() {
        return cachedIngredients != null && !cachedIngredients.isEmpty();
    }

    public boolean hasAreasCache() {
        return cachedAreas != null && !cachedAreas.isEmpty();
    }

    public IngredientList getCachedIngredients() {
        return cachedIngredients;
    }

    public AreaList getCachedAreas() {
        return cachedAreas;
    }


    public void dispose() {
        disposables.clear();
    }
}
