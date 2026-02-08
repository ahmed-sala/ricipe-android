package com.example.recipe_android_project.features.favourites.data.datasource.remote;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.google.firebase.firestore.FirebaseFirestore;

import io.reactivex.rxjava3.core.Completable;

public class FavouriteRemoteDatasource {
    private static final String USERS_COLLECTION = "users";
    private static final String FAVORITES_COLLECTION = "favorites";
    private final FirebaseFirestore firestore;
    private final Context context;

    public FavouriteRemoteDatasource(Context context) {
        this.firestore = FirebaseFirestore.getInstance();
        this.context = context;
    }
    public boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnected();
        }
        return false;
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

}
