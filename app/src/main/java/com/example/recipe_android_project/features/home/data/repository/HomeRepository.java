package com.example.recipe_android_project.features.home.data.repository;

import android.content.Context;

import com.example.recipe_android_project.core.helper.UserSessionManager;
import com.example.recipe_android_project.features.home.data.datasource.local.HomeLocalDatasource;
import com.example.recipe_android_project.features.home.data.datasource.remote.HomeRemoteDatasource;
import com.example.recipe_android_project.features.home.data.entities.FavoriteMealEntity;
import com.example.recipe_android_project.features.home.data.mapper.CategoryMapper;
import com.example.recipe_android_project.features.home.data.mapper.MealMapper;
import com.example.recipe_android_project.features.home.model.Category;
import com.example.recipe_android_project.features.home.model.Meal;
import com.example.recipe_android_project.features.search.data.mapper.FilterResultMapper;
import com.example.recipe_android_project.features.search.domain.model.FilterResult;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class HomeRepository {

    private final HomeRemoteDatasource remote;
    private final HomeLocalDatasource local;
    private final UserSessionManager sessionManager;

    public HomeRepository(Context context) {
        this.remote = new HomeRemoteDatasource(context);
        this.local = new HomeLocalDatasource(context);
        this.sessionManager = UserSessionManager.getInstance(context);
    }

    private String getLocalUserId() {
        return sessionManager.getCurrentUserIdOrNull();
    }

    private String getFirebaseUserId() {
        return sessionManager.getFirebaseUidOrNull();
    }

    private boolean isUserLoggedIn() {
        return sessionManager.hasValidSession();
    }

    private boolean isNetworkAvailable() {
        return remote.isNetworkAvailable();
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

    public Completable addToFavorites(Meal meal) {
        if (meal == null) {
            return Completable.error(new IllegalArgumentException("Meal is required"));
        }

        String localUserId = getLocalUserId();
        String firebaseUserId = getFirebaseUserId();

        if (localUserId == null) {
            return Completable.error(new IllegalStateException("User not logged in"));
        }

        FavoriteMealEntity entity = MealMapper.toEntity(meal, localUserId);
        if (entity == null) {
            return Completable.error(new IllegalArgumentException("Failed to create favorite entity"));
        }

        Completable localSave = local.addToFavorites(entity);

        Completable firestoreSync = Completable.defer(() -> {
            if (firebaseUserId != null && isNetworkAvailable()) {
                FavoriteMealEntity firestoreEntity = MealMapper.toEntity(meal, firebaseUserId);
                return remote.addFavoriteToFirestore(firestoreEntity)
                        .onErrorComplete();
            }
            return Completable.complete();
        });

        return localSave
                .andThen(firestoreSync)
                .subscribeOn(Schedulers.io());
    }

    public Completable removeFromFavorites(String mealId) {
        if (mealId == null || mealId.isEmpty()) {
            return Completable.error(new IllegalArgumentException("MealId is required"));
        }

        String localUserId = getLocalUserId();
        String firebaseUserId = getFirebaseUserId();

        if (localUserId == null) {
            return Completable.error(new IllegalStateException("User not logged in"));
        }

        Completable localRemove = local.removeFromFavorites(mealId, localUserId);

        Completable firestoreSync = Completable.defer(() -> {
            if (firebaseUserId != null && isNetworkAvailable()) {
                return remote.removeFavoriteFromFirestore(firebaseUserId, mealId)
                        .onErrorComplete();
            }
            return Completable.complete();
        });

        return localRemove
                .andThen(firestoreSync)
                .subscribeOn(Schedulers.io());
    }

    public Completable removeFromFavorites(Meal meal) {
        if (meal == null || meal.getId() == null) {
            return Completable.error(new IllegalArgumentException("Meal is required"));
        }
        return removeFromFavorites(meal.getId());
    }

    public Flowable<List<Meal>> getFavorites() {
        String localUserId = getLocalUserId();
        if (localUserId == null) {
            return Flowable.error(new IllegalStateException("User not logged in"));
        }
        return local.getFavorites(localUserId);
    }

    public Single<Boolean> isFavorite(String mealId) {
        if (mealId == null || mealId.isEmpty()) {
            return Single.just(false);
        }

        String localUserId = getLocalUserId();
        if (localUserId == null) {
            return Single.just(false);
        }

        return local.isFavorite(mealId, localUserId);
    }

    public Single<List<Meal>> getMealsByCategoryWithFavoriteStatus(String categoryName) {
        return getMealsByCategory(categoryName)
                .flatMap(meals -> {
                    String localUserId = getLocalUserId();
                    if (localUserId == null || meals.isEmpty()) {
                        return Single.just(meals);
                    }
                    return local.getFavoriteEntitiesSingle(localUserId)
                            .map(favorites -> MealMapper.markFavorites(meals, favorites));
                });
    }

    public Single<Meal> getMealOfTheDayWithFavoriteStatus() {
        return getMealOfTheDay()
                .flatMap(meal -> {
                    String localUserId = getLocalUserId();
                    if (localUserId == null) {
                        meal.setFavorite(false);
                        return Single.just(meal);
                    }
                    return local.isFavorite(meal.getId(), localUserId)
                            .map(isFav -> {
                                meal.setFavorite(isFav);
                                return meal;
                            });
                });
    }

    public Single<List<Meal>> getMealsByFirstLetterWithFavoriteStatus(String firstLetter) {
        return getMealsByFirstLetter(firstLetter)
                .flatMap(meals -> {
                    String localUserId = getLocalUserId();
                    if (localUserId == null || meals.isEmpty()) {
                        return Single.just(meals);
                    }
                    return local.getFavoriteEntitiesSingle(localUserId)
                            .map(favorites -> MealMapper.markFavorites(meals, favorites));
                });
    }

    public boolean isUserAuthenticated() {
        return isUserLoggedIn();
    }

}
