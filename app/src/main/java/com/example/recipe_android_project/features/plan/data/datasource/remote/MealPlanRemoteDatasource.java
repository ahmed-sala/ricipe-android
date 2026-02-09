package com.example.recipe_android_project.features.plan.data.datasource.remote;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.example.recipe_android_project.features.plan.data.entity.MealPlanEntity;
import com.example.recipe_android_project.features.plan.data.mapper.MealPlanMapper;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.core.Completable;

public class MealPlanRemoteDatasource {

    private static final String USERS_COLLECTION = "users";
    private static final String MEAL_PLANS_COLLECTION = "meal_plans";

    private final FirebaseFirestore firestore;
    private final Context context;

    public MealPlanRemoteDatasource(Context context) {
        this.context = context;
        this.firestore = FirebaseFirestore.getInstance();
    }


    public boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnected();
        }
        return false;
    }
    public Completable removeMealPlanFromFirestore(String userId, String date, String mealType) {
        return Completable.create(emitter -> {
            if (userId == null || userId.isEmpty() || date == null || date.isEmpty() ||
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
    private MealPlanEntity mapDocumentToEntity(DocumentSnapshot doc) {
        if (doc == null || !doc.exists()) return null;

        Map<String, Object> data = doc.getData();
        if (data == null) return null;

        MealPlanEntity entity = MealPlanMapper.mapToEntity(data);
        if (entity != null) {
            entity.setSynced(true);
        }
        return entity;
    }

    private List<MealPlanEntity> mapQueryToEntityList(List<DocumentSnapshot> documents) {
        List<MealPlanEntity> entities = new ArrayList<>();
        if (documents != null) {
            for (DocumentSnapshot doc : documents) {
                MealPlanEntity entity = mapDocumentToEntity(doc);
                if (entity != null) {
                    entities.add(entity);
                }
            }
        }
        return entities;
    }
}
