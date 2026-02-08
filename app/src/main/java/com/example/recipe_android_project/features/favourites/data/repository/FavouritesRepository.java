package com.example.recipe_android_project.features.favourites.data.repository;

import android.content.Context;

import com.airbnb.lottie.animation.content.Content;
import com.example.recipe_android_project.core.helper.UserSessionManager;
import com.example.recipe_android_project.features.favourites.data.datasource.local.FavouriteLocalDatasource;
import com.example.recipe_android_project.features.favourites.data.datasource.remote.FavouriteRemoteDatasource;
import com.example.recipe_android_project.features.home.model.Meal;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class FavouritesRepository {
    private final FavouriteLocalDatasource favouriteLocalDatasource;
    private final FavouriteRemoteDatasource favouriteRemoteDatasource;
    private final UserSessionManager sessionManager;

    public FavouritesRepository(Context context) {
        this.favouriteLocalDatasource = new FavouriteLocalDatasource(context);
        this.favouriteRemoteDatasource = new FavouriteRemoteDatasource(context);
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
        return favouriteRemoteDatasource.isNetworkAvailable();
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
        Completable localRemove = favouriteLocalDatasource.removeFromFavorites(mealId, localUserId);

        // Then sync to Firestore
        Completable firestoreSync = Completable.defer(() -> {
            if (firebaseUserId != null && isNetworkAvailable()) {
                return favouriteRemoteDatasource.removeFavoriteFromFirestore(firebaseUserId, mealId)
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
        return favouriteLocalDatasource.getFavorites(localUserId);
    }
}
