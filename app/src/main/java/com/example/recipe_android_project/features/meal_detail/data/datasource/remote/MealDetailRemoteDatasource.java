package com.example.recipe_android_project.features.meal_detail.data.datasource.remote;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.example.recipe_android_project.core.config.RetrofitClient;
import com.example.recipe_android_project.features.home.data.dto.meal.MealResponseDto;
import com.example.recipe_android_project.features.home.data.entities.FavoriteMealEntity;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public class MealDetailRemoteDatasource {
    private final MealDetailApiService mealDetailApiService;
    private static final String USERS_COLLECTION = "users";
    private static final String FAVORITES_COLLECTION = "favorites";
    private final FirebaseFirestore firestore;
    private final Context context;
    public MealDetailRemoteDatasource(Context context) {
        this.context=context;
        this.firestore = FirebaseFirestore.getInstance();
        this.mealDetailApiService = RetrofitClient.getMealDetailApiService();
    }
    public boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnected();
        }
        return false;
    }
    public Completable addFavoriteToFirestore(FavoriteMealEntity entity) {
        return Completable.create(emitter -> {
            if (entity == null || entity.getUserId().isEmpty() || entity.getMealId().isEmpty()) {
                emitter.onError(new IllegalArgumentException("Invalid favorite entity"));
                return;
            }

            Map<String, Object> favoriteData = entityToMap(entity);

            firestore.collection(USERS_COLLECTION)
                    .document(entity.getUserId())
                    .collection(FAVORITES_COLLECTION)
                    .document(entity.getMealId())
                    .set(favoriteData)
                    .addOnSuccessListener(aVoid -> emitter.onComplete())
                    .addOnFailureListener(e ->
                            emitter.onError(new Exception("Failed to add favorite to Firestore: " + e.getMessage()))
                    );
        });
    }
    public Completable removeFavoriteFromFirestore(String userId, String mealId) {
        return Completable.create(emitter -> {
            if (userId == null || userId.isEmpty() || mealId == null || mealId.isEmpty()) {
                emitter.onError(new IllegalArgumentException("UserId and MealId are required"));
                return;
            }

            firestore.collection(USERS_COLLECTION)
                    .document(userId)
                    .collection(FAVORITES_COLLECTION)
                    .document(mealId)
                    .delete()
                    .addOnSuccessListener(aVoid -> emitter.onComplete())
                    .addOnFailureListener(e ->
                            emitter.onError(new Exception("Failed to remove favorite from Firestore: " + e.getMessage()))
                    );
        });
    }
    public Single<MealResponseDto> getMealById(String id) {
        return mealDetailApiService.getMealById(id);
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
