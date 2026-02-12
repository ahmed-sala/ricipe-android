package com.example.recipe_android_project.features.home.data.datasource.remote;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;

import com.example.recipe_android_project.core.config.RetrofitClient;
import com.example.recipe_android_project.features.home.data.dto.category.CategoryResponseDto;
import com.example.recipe_android_project.features.home.data.dto.meal.MealResponseDto;
import com.example.recipe_android_project.features.home.data.entities.FavoriteMealEntity;
import com.example.recipe_android_project.features.search.data.dto.filter_result.FilterResultResponseDto;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public class HomeRemoteDatasource {

    private final MealApiService mealApiService;
    private static final String USERS_COLLECTION = "users";
    private static final String FAVORITES_COLLECTION = "favorites";

    private final FirebaseFirestore firestore;
    private final Context context;
    private ConnectivityManager connectivityManager;
    public HomeRemoteDatasource(Context context) {
        this.context = context;
        this.firestore = FirebaseFirestore.getInstance();
        this.mealApiService = RetrofitClient.getMealApiService();
    }


    public Single<MealResponseDto> getMealOfTheDay() {
        return mealApiService.getRandomMeal();
    }

    public Single<CategoryResponseDto> getCategories() {
        return mealApiService.getCategories();
    }

    public Single<MealResponseDto> getMealsByFirstLetter(String firstLetter) {
        return mealApiService.getMealsByFirstLetter(firstLetter);
    }

    public Single<FilterResultResponseDto> getMealsByCategory(String categoryName) {
        return mealApiService.filterByMealCategory(categoryName);
    }

    public boolean isNetworkAvailable() {
        if (connectivityManager == null) {
            connectivityManager = (ConnectivityManager)
                    context.getSystemService(Context.CONNECTIVITY_SERVICE);
        }

        if (connectivityManager == null) return false;

        Network network = connectivityManager.getActiveNetwork();
        if (network == null) return false;

        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
        if (capabilities == null) return false;

        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
    }
    public Completable addFavoriteToFirestore(FavoriteMealEntity entity) {
        return Completable.create(emitter -> {
            if (entity == null || entity.getUserId() == null || entity.getUserId().isEmpty()
                    || entity.getMealId() == null || entity.getMealId().isEmpty()) {
                emitter.onComplete();
                return;
            }

            Map<String, Object> favoriteData = entityToMap(entity);

            firestore.collection(USERS_COLLECTION)
                    .document(entity.getUserId())
                    .collection(FAVORITES_COLLECTION)
                    .document(entity.getMealId())
                    .set(favoriteData)
                    .addOnSuccessListener(aVoid -> emitter.onComplete())
                    .addOnFailureListener(e -> emitter.onError(
                            new Exception("Failed to add favorite to Firestore: " + e.getMessage())
                    ));
        });
    }
    public Completable removeFavoriteFromFirestore(String userId, String mealId) {
        return Completable.create(emitter -> {
            if (userId == null || userId.isEmpty() || mealId == null || mealId.isEmpty()) {
                emitter.onComplete();
                return;
            }

            firestore.collection(USERS_COLLECTION)
                    .document(userId)
                    .collection(FAVORITES_COLLECTION)
                    .document(mealId)
                    .delete()
                    .addOnSuccessListener(aVoid -> emitter.onComplete())
                    .addOnFailureListener(e -> emitter.onError(
                            new Exception("Failed to remove favorite from Firestore: " + e.getMessage())
                    ));
        });
    }
    private Map<String, Object> entityToMap(FavoriteMealEntity entity) {
        Map<String, Object> map = new HashMap<>();
        map.put("mealId", entity.getMealId());
        map.put("userId", entity.getUserId());
        map.put("name", entity.getName());
        map.put("alternateName", entity.getAlternateName());
        map.put("category", entity.getCategory());
        map.put("area", entity.getArea());
        map.put("instructions", entity.getInstructions());
        map.put("thumbnailUrl", entity.getThumbnailUrl());
        map.put("tags", entity.getTags());
        map.put("youtubeUrl", entity.getYoutubeUrl());
        map.put("sourceUrl", entity.getSourceUrl());
        map.put("imageSource", entity.getImageSource());
        map.put("creativeCommonsConfirmed", entity.getCreativeCommonsConfirmed());
        map.put("dateModified", entity.getDateModified());
        map.put("ingredientsJson", entity.getIngredientsJson());
        map.put("createdAt", entity.getCreatedAt());
        return map;
    }
}
