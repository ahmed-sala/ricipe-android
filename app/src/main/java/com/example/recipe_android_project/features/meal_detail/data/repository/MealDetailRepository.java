package com.example.recipe_android_project.features.meal_detail.data.repository;

import android.content.Context;

import com.example.recipe_android_project.core.helper.UserSessionManager;
import com.example.recipe_android_project.features.home.data.entities.FavoriteMealEntity;
import com.example.recipe_android_project.features.home.data.mapper.MealMapper;
import com.example.recipe_android_project.features.home.model.Meal;
import com.example.recipe_android_project.features.meal_detail.data.datasource.local.MealDetailLocalDatasource;
import com.example.recipe_android_project.features.meal_detail.data.datasource.remote.MealDetailRemoteDatasource;
import com.example.recipe_android_project.features.meal_detail.domain.model.MealPlan;
import com.example.recipe_android_project.features.plan.data.entity.MealPlanEntity;
import com.example.recipe_android_project.features.plan.data.mapper.MealPlanMapper;

import java.util.List;

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


    public Single<Meal> getMealById(String id) {
        return remote.getMealById(id)
                .flatMap(response -> {
                    if (response == null || response.getMeals() == null
                            || response.getMeals().isEmpty()) {
                        return Single.error(new Exception("Meal not found"));
                    }
                    return Single.just(
                            MealMapper.toDomain(response.getMeals().get(0)));
                })
                .subscribeOn(Schedulers.io());
    }


    public Single<Meal> getMealByIdWithFavoriteStatus(String id) {
        return getMealById(id)
                .flatMap(meal -> attachFavoriteStatus(meal, id))
                .onErrorResumeNext(remoteError ->
                        getMealFromLocalWithFallback(id, remoteError))
                .subscribeOn(Schedulers.io());
    }

    private Single<Meal> attachFavoriteStatus(Meal meal, String mealId) {
        String localUserId = getLocalUserId();
        if (localUserId == null) {
            meal.setFavorite(false);
            return Single.just(meal);
        }
        return local.isFavorite(mealId, localUserId)
                .map(isFav -> {
                    meal.setFavorite(isFav);
                    return meal;
                });
    }

    private Single<Meal> getMealFromLocalWithFallback(String mealId,
                                                      Throwable remoteError) {
        String localUserId = getLocalUserId();

        if (localUserId == null) {
            return Single.error(
                    new Exception("No internet connection. Please try again later."));
        }

        return getMealFromFavorites(mealId, localUserId)
                .onErrorResumeNext(favError ->
                        getMealFromMealPlan(mealId, localUserId)
                                .onErrorResumeNext(planError ->
                                        Single.error(buildOfflineError(remoteError))
                                )
                );
    }


    private Single<Meal> getMealFromFavorites(String mealId, String userId) {
        return local.getFavoriteMealById(mealId, userId)
                .map(entity -> {
                    Meal meal = MealMapper.fromFavoriteEntity(entity);
                    if (meal == null) {
                        throw new Exception("Failed to parse favorite meal");
                    }
                    meal.setFavorite(true);
                    meal.setOffline(true);
                    return meal;
                });
    }


    private Single<Meal> getMealFromMealPlan(String mealId, String userId) {
        return local.getMealPlanByMealId(mealId, userId)
                .map(entity -> {
                    Meal meal = MealPlanMapper.toMealDomain(entity);
                    if (meal == null) {
                        throw new Exception("Failed to parse meal plan");
                    }

                    meal.setFavorite(false);
                    meal.setOffline(true);
                    meal.setLimitedData(true);
                    return meal;
                });
    }


    private Exception buildOfflineError(Throwable remoteError) {
        if (remoteError instanceof java.net.UnknownHostException
                || remoteError instanceof java.net.SocketTimeoutException
                || remoteError instanceof java.io.IOException) {
            return new Exception(
                    "No internet connection and meal not saved locally.");
        }
        return new Exception(
                "Failed to load meal. " +
                        (remoteError.getMessage() != null
                                ? remoteError.getMessage()
                                : "Please try again."));
    }


    public Completable addToFavorites(Meal meal) {
        if (meal == null) {
            return Completable.error(
                    new IllegalArgumentException("Meal is required"));
        }

        String localUserId = getLocalUserId();
        String firebaseUserId = getFirebaseUserId();

        if (localUserId == null) {
            return Completable.error(
                    new IllegalStateException("User not logged in"));
        }

        FavoriteMealEntity entity = MealMapper.toEntity(meal, localUserId);
        if (entity == null) {
            return Completable.error(
                    new IllegalArgumentException("Failed to create favorite entity"));
        }

        Completable localSave = local.addToFavorites(entity);

        Completable firestoreSync = Completable.defer(() -> {
            if (firebaseUserId != null && isNetworkAvailable()) {
                FavoriteMealEntity firestoreEntity =
                        MealMapper.toEntity(meal, firebaseUserId);
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
            return Completable.error(
                    new IllegalArgumentException("MealId is required"));
        }

        String localUserId = getLocalUserId();
        String firebaseUserId = getFirebaseUserId();

        if (localUserId == null) {
            return Completable.error(
                    new IllegalStateException("User not logged in"));
        }

        Completable localRemove =
                local.removeFromFavorites(mealId, localUserId);

        Completable firestoreSync = Completable.defer(() -> {
            if (firebaseUserId != null && isNetworkAvailable()) {
                return remote.removeFavoriteFromFirestore(
                                firebaseUserId, mealId)
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
            return Completable.error(
                    new IllegalArgumentException("Meal is required"));
        }
        return removeFromFavorites(meal.getId());
    }

    public Single<Boolean> isFavorite(String mealId) {
        if (mealId == null || mealId.isEmpty()) {
            return Single.just(false);
        }

        String localUserId = getLocalUserId();
        if (localUserId == null) {
            return Single.just(false);
        }

        return local.isFavorite(mealId, localUserId)
                .subscribeOn(Schedulers.io());
    }


    public Completable addMealToPlan(Meal meal, String date, String mealType) {
        if (meal == null) {
            return Completable.error(
                    new IllegalArgumentException("Meal is required"));
        }
        if (date == null || date.isEmpty()) {
            return Completable.error(
                    new IllegalArgumentException("Date is required"));
        }
        if (mealType == null || mealType.isEmpty()) {
            return Completable.error(
                    new IllegalArgumentException("Meal type is required"));
        }

        String localUserId = getLocalUserId();
        String firebaseUserId = getFirebaseUserId();

        if (localUserId == null) {
            return Completable.error(
                    new IllegalStateException("User not logged in"));
        }

        MealPlanEntity localEntity = MealPlanMapper.createNewMealPlanEntity(
                localUserId, date, mealType,
                meal.getId(), meal.getName(),
                meal.getThumbnailUrl(),
                meal.getCategory(), meal.getArea()
        );

        Completable localSave = local.addMealPlan(localEntity);

        Completable firestoreSync = Completable.defer(() -> {
            if (firebaseUserId != null && isNetworkAvailable()) {
                MealPlanEntity firestoreEntity =
                        MealPlanMapper.createNewMealPlanEntity(
                                firebaseUserId, date, mealType,
                                meal.getId(), meal.getName(),
                                meal.getThumbnailUrl(),
                                meal.getCategory(), meal.getArea()
                        );
                firestoreEntity.setSynced(true);

                return remote.addMealPlanToFirestore(firestoreEntity)
                        .andThen(local.updateMealPlanSyncStatus(
                                localUserId, date, mealType, true))
                        .onErrorComplete();
            }
            return Completable.complete();
        });

        return localSave
                .andThen(firestoreSync)
                .subscribeOn(Schedulers.io());
    }

    public Completable removeMealPlan(String date, String mealType) {
        if (date == null || date.isEmpty()) {
            return Completable.error(
                    new IllegalArgumentException("Date is required"));
        }
        if (mealType == null || mealType.isEmpty()) {
            return Completable.error(
                    new IllegalArgumentException("Meal type is required"));
        }

        String localUserId = getLocalUserId();
        String firebaseUserId = getFirebaseUserId();

        if (localUserId == null) {
            return Completable.error(
                    new IllegalStateException("User not logged in"));
        }

        Completable localRemove =
                local.removeMealPlan(localUserId, date, mealType);

        Completable firestoreSync = Completable.defer(() -> {
            if (firebaseUserId != null && isNetworkAvailable()) {
                return remote.removeMealPlanFromFirestore(
                                firebaseUserId, date, mealType)
                        .onErrorComplete();
            }
            return Completable.complete();
        });

        return localRemove
                .andThen(firestoreSync)
                .subscribeOn(Schedulers.io());
    }

    public Single<MealPlan> getMealPlan(String date, String mealType) {
        if (date == null || date.isEmpty()
                || mealType == null || mealType.isEmpty()) {
            return Single.error(
                    new IllegalArgumentException(
                            "Date and MealType are required"));
        }

        String localUserId = getLocalUserId();
        if (localUserId == null) {
            return Single.error(
                    new IllegalStateException("User not logged in"));
        }

        return local.getMealPlan(localUserId, date, mealType)
                .map(MealPlanMapper::toDomain)
                .subscribeOn(Schedulers.io());
    }
}
