package com.example.recipe_android_project.features.meal_detail.data.repository;

import android.content.Context;

import com.example.recipe_android_project.core.helper.UserSessionManager;
import com.example.recipe_android_project.features.home.data.entities.FavoriteMealEntity;
import com.example.recipe_android_project.features.home.data.mapper.MealMapper;
import com.example.recipe_android_project.features.home.model.Meal;
import com.example.recipe_android_project.features.meal_detail.data.datasource.local.MealDetailLocalDatasource;
import com.example.recipe_android_project.features.meal_detail.data.datasource.remote.MealDetailRemoteDatasource;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MealDetailRepository {

    private final MealDetailRemoteDatasource remote;
    private final MealDetailLocalDatasource local;
    private final UserSessionManager sessionManager;

    public MealDetailRepository(Context context) {
        this.remote = new MealDetailRemoteDatasource(context);
        this.local = new MealDetailLocalDatasource(context);
        this.sessionManager = UserSessionManager.getInstance(context);
    }

    // Constructor for testing/dependency injection
    public MealDetailRepository(MealDetailRemoteDatasource remote,
                                MealDetailLocalDatasource local,
                                UserSessionManager sessionManager) {
        this.remote = remote;
        this.local = local;
        this.sessionManager = sessionManager;
    }

    // ==================== HELPER METHODS ====================

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

    public boolean isUserAuthenticated() {
        return isUserLoggedIn();
    }

    // ==================== MEAL OPERATIONS ====================

    public Single<Meal> getMealById(String id) {
        return remote.getMealById(id)
                .flatMap(response -> {
                    if (response == null || response.getMeals() == null || response.getMeals().isEmpty()) {
                        return Single.error(new Exception("Meal not found"));
                    }
                    return Single.just(MealMapper.toDomain(response.getMeals().get(0)));
                });
    }

    /**
     * Get meal by ID with favorite status
     */
    public Single<Meal> getMealByIdWithFavoriteStatus(String id) {
        return getMealById(id)
                .flatMap(meal -> {
                    String localUserId = getLocalUserId();
                    if (localUserId == null) {
                        meal.setFavorite(false);
                        return Single.just(meal);
                    }
                    return local.isFavorite(id, localUserId)
                            .map(isFav -> {
                                meal.setFavorite(isFav);
                                return meal;
                            });
                });
    }

    // ==================== FAVORITE OPERATIONS ====================

    /**
     * Add meal to favorites
     */
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
        Completable localSave = local.addToFavorites(entity);

        // Then sync to Firestore using Firebase UID
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

    /**
     * Remove meal from favorites by meal ID
     */
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
        Completable localRemove = local.removeFromFavorites(mealId, localUserId);

        // Then sync to Firestore
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

    /**
     * Remove meal from favorites
     */
    public Completable removeFromFavorites(Meal meal) {
        if (meal == null || meal.getId() == null) {
            return Completable.error(new IllegalArgumentException("Meal is required"));
        }
        return removeFromFavorites(meal.getId());
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

    /**
     * Check if meal is favorite
     */
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
}
