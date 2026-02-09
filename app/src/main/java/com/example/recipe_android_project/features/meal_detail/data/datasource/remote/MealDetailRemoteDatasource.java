package com.example.recipe_android_project.features.meal_detail.data.datasource.remote;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.example.recipe_android_project.core.config.RetrofitClient;
import com.example.recipe_android_project.features.home.data.dto.meal.MealResponseDto;
import com.example.recipe_android_project.features.home.data.entities.FavoriteMealEntity;
import com.example.recipe_android_project.features.plan.data.entity.MealPlanEntity;
import com.example.recipe_android_project.features.plan.data.mapper.MealPlanMapper;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public class MealDetailRemoteDatasource {

    private final MealDetailApiService mealDetailApiService;
    private static final String USERS_COLLECTION = "users";
    private static final String FAVORITES_COLLECTION = "favorites";
    private static final String MEAL_PLANS_COLLECTION = "meal_plans";

    private final FirebaseFirestore firestore;
    private final Context context;

    public MealDetailRemoteDatasource(Context context) {
        this.context = context;
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


    public Single<MealResponseDto> getMealById(String id) {
        return mealDetailApiService.getMealById(id);
    }

    public Completable addFavoriteToFirestore(FavoriteMealEntity entity) {
        return Completable.create(emitter -> {
            if (entity == null || entity.getUserId().isEmpty() || entity.getMealId().isEmpty()) {
                emitter.onError(new IllegalArgumentException("Invalid favorite entity"));
                return;
            }

            Map<String, Object> favoriteData = favoriteEntityToMap(entity);

            firestore.collection(USERS_COLLECTION)
                    .document(entity.getUserId())
                    .collection(FAVORITES_COLLECTION)
                    .document(entity.getMealId())
                    .set(favoriteData)
                    .addOnSuccessListener(aVoid -> {
                        if (!emitter.isDisposed()) {
                            emitter.onComplete();
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (!emitter.isDisposed()) {
                            emitter.onError(new Exception("Failed to add favorite to Firestore: " + e.getMessage()));
                        }
                    });
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
                    .addOnSuccessListener(aVoid -> {
                        if (!emitter.isDisposed()) {
                            emitter.onComplete();
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (!emitter.isDisposed()) {
                            emitter.onError(new Exception("Failed to remove favorite from Firestore: " + e.getMessage()));
                        }
                    });
        });
    }

    public Completable addMealPlanToFirestore(MealPlanEntity entity) {
        return Completable.create(emitter -> {
            if (!MealPlanMapper.isValidMealPlanEntity(entity)) {
                emitter.onError(new IllegalArgumentException("Invalid meal plan entity"));
                return;
            }

            String documentId = MealPlanMapper.generateDocumentId(entity);
            Map<String, Object> mealPlanData = MealPlanMapper.entityToMap(entity);

            if (documentId == null || mealPlanData == null) {
                emitter.onError(new IllegalArgumentException("Failed to generate document data"));
                return;
            }

            firestore.collection(USERS_COLLECTION)
                    .document(entity.getUserId())
                    .collection(MEAL_PLANS_COLLECTION)
                    .document(documentId)
                    .set(mealPlanData)
                    .addOnSuccessListener(aVoid -> {
                        if (!emitter.isDisposed()) {
                            emitter.onComplete();
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (!emitter.isDisposed()) {
                            emitter.onError(new Exception("Failed to add meal plan to Firestore: " + e.getMessage()));
                        }
                    });
        });
    }
    public Completable removeMealPlanFromFirestore(String userId, String date, String mealType) {
        return Completable.create(emitter -> {
            if (userId == null || userId.isEmpty() ||
                    date == null || date.isEmpty() ||
                    mealType == null || mealType.isEmpty()) {
                emitter.onError(new IllegalArgumentException("UserId, Date, and MealType are required"));
                return;
            }

            String documentId = MealPlanMapper.generateDocumentId(date, mealType);

            firestore.collection(USERS_COLLECTION)
                    .document(userId)
                    .collection(MEAL_PLANS_COLLECTION)
                    .document(documentId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        if (!emitter.isDisposed()) {
                            emitter.onComplete();
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (!emitter.isDisposed()) {
                            emitter.onError(new Exception("Failed to remove meal plan from Firestore: " + e.getMessage()));
                        }
                    });
        });
    }
    private Map<String, Object> favoriteEntityToMap(FavoriteMealEntity entity) {
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

    private MealPlanEntity documentToMealPlanEntity(DocumentSnapshot doc, String userId) {
        if (doc == null || !doc.exists()) {
            return null;
        }

        Map<String, Object> data = doc.getData();
        if (data == null) {
            return null;
        }

        MealPlanEntity entity = MealPlanMapper.mapToEntity(data);
        if (entity != null) {
            if (entity.getUserId() == null || entity.getUserId().isEmpty()) {
                entity.setUserId(userId);
            }
            entity.setSynced(true);
        }
        return entity;
    }
}
