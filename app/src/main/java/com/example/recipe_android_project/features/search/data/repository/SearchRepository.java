package com.example.recipe_android_project.features.search.data.repository;

import android.content.Context;

import com.example.recipe_android_project.core.config.ResultCallback;
import com.example.recipe_android_project.core.helper.UserSessionManager;
import com.example.recipe_android_project.features.home.data.entities.FavoriteMealEntity;
import com.example.recipe_android_project.features.home.data.mapper.AreaMapper;
import com.example.recipe_android_project.features.home.data.mapper.MealMapper;
import com.example.recipe_android_project.features.home.model.Area;
import com.example.recipe_android_project.features.home.model.AreaList;
import com.example.recipe_android_project.features.home.model.Meal;
import com.example.recipe_android_project.features.search.data.datasource.local.SearchLocalDatasource;
import com.example.recipe_android_project.features.search.data.datasource.remote.SearchRemoteDataSource;
import com.example.recipe_android_project.features.search.data.mapper.FilterResultMapper;
import com.example.recipe_android_project.features.search.data.mapper.IngredientMapper;
import com.example.recipe_android_project.features.search.domain.model.FilterResultList;
import com.example.recipe_android_project.features.search.domain.model.Ingredient;
import com.example.recipe_android_project.features.search.domain.model.IngredientList;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SearchRepository {
    private final UserSessionManager sessionManager;

    private final SearchRemoteDataSource remoteDataSource;
    private final SearchLocalDatasource localDatasource;
    private IngredientList cachedIngredients;
    private AreaList cachedAreas;

    public SearchRepository(Context context) {
        this.sessionManager =  UserSessionManager.getInstance(context);
            this.localDatasource = new SearchLocalDatasource(context);
        this.remoteDataSource = new SearchRemoteDataSource(
                context
        );
    }
    private String getLocalUserId() {
        return sessionManager.getCurrentUserIdOrNull();
    }

    /**
     * Get Firebase UID (for Firestore operations)
     * This must match the Firebase Auth UID for security rules to work
     */
    private String getFirebaseUserId() {
        return sessionManager.getFirebaseUidOrNull();
    }

    private boolean isUserLoggedIn() {
        return sessionManager.hasValidSession();
    }

    private boolean isNetworkAvailable() {
        return remoteDataSource.isNetworkAvailable();
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

        // Create entity with local user ID for Room
        FavoriteMealEntity entity = MealMapper.toEntity(meal, localUserId);
        if (entity == null) {
            return Completable.error(new IllegalArgumentException("Failed to create favorite entity"));
        }

        // Save to local DB first
        Completable localSave = localDatasource.addToFavorites(entity);

        // Then sync to Firestore using Firebase UID
        Completable firestoreSync = Completable.defer(() -> {
            if (firebaseUserId != null && isNetworkAvailable()) {
                // Create entity with Firebase UID for Firestore
                FavoriteMealEntity firestoreEntity = MealMapper.toEntity(meal, firebaseUserId);
                return remoteDataSource.addFavoriteToFirestore(firestoreEntity)
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

        // Remove from local DB first
        Completable localRemove = localDatasource.removeFromFavorites(mealId, localUserId);

        // Then sync to Firestore
        Completable firestoreSync = Completable.defer(() -> {
            if (firebaseUserId != null && isNetworkAvailable()) {
                return remoteDataSource.removeFavoriteFromFirestore(firebaseUserId, mealId)
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
    public Single<List<Meal>> searchMealsByName(String name) {
        return remoteDataSource.searchMealsByName(name)
                .map(response -> {
                    if (response == null || response.getMeals() == null) {
                        return new ArrayList<Meal>();
                    }
                    return MealMapper.toDomainList(response.getMeals());
                });
    }
    public boolean isUserAuthenticated() {
        return isUserLoggedIn();
    }
    public Single<List<Meal>> getSearchedMealsWithFavoriteStatus(String name) {
        return searchMealsByName(name)
                .flatMap(meals -> {
                    String localUserId = getLocalUserId();
                    if (localUserId == null || meals.isEmpty()) {
                        return Single.just(meals);
                    }
                    return localDatasource.getFavoriteEntitiesSingle(localUserId)
                            .map(favorites -> MealMapper.markFavorites(meals, favorites));
                });
    }
    public Single<Meal> getMealByIdWithFavoriteStatus(String id) {
        return getMealById(id)
                .flatMap(meal -> {
                    String localUserId = getLocalUserId();
                    if (localUserId == null) {
                        meal.setFavorite(false);
                        return Single.just(meal);
                    }
                    return localDatasource.isFavorite(id, localUserId)
                            .map(isFav -> {
                                meal.setFavorite(isFav);
                                return meal;
                            });
                });
    }

    /**
     * Toggle favorite status
     */
    public Completable toggleFavorite(Meal meal) {
        if (meal == null || meal.getId() == null) {
            return Completable.error(new IllegalArgumentException("Meal is required"));
        }

        if (!isUserLoggedIn()) {
            return Completable.error(new IllegalStateException("User not logged in"));
        }

        return isFavorite(meal.getId())
                .flatMapCompletable(isFav -> {
                    if (isFav) {
                        return removeFromFavorites(meal.getId());
                    } else {
                        return addToFavorites(meal);
                    }
                });
    }

    public Single<Boolean> isFavorite(String mealId) {
        if (mealId == null || mealId.isEmpty()) {
            return Single.just(false);
        }

        String localUserId = getLocalUserId();
        if (localUserId == null) {
            return Single.just(false);
        }

        return localDatasource.isFavorite(mealId, localUserId);
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
