package com.example.recipe_android_project.features.auth.data.datasource.remote;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.example.recipe_android_project.features.auth.data.entities.UserEntity;
import com.example.recipe_android_project.features.home.data.entities.FavoriteMealEntity;
import com.example.recipe_android_project.features.plan.data.entity.MealPlanEntity;
import com.example.recipe_android_project.features.plan.data.mapper.MealPlanMapper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

public class AuthRemoteDatasource {
    private static final String USERS_COLLECTION = "users";
    private static final String FAVORITES_COLLECTION = "favorites";
    private static final String MEAL_PLANS_COLLECTION = "meal_plans";

    private final FirebaseAuth firebaseAuth;
    private final FirebaseFirestore firestore;
    private final Context context;

    public AuthRemoteDatasource(Context context) {
        this.context = context;
        this.firebaseAuth = FirebaseAuth.getInstance();
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


    @SuppressLint("CheckResult")
    public Single<UserEntity> registerUser(String fullName, String email, String password) {
        return Single.create(emitter -> {
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        FirebaseUser firebaseUser = authResult.getUser();
                        if (firebaseUser == null) {
                            emitter.onError(new Exception("Firebase registration failed"));
                            return;
                        }

                        UserEntity user = new UserEntity();
                        user.setId(firebaseUser.getUid());
                        user.setFullName(fullName);
                        user.setEmail(email);
                        user.setLoggedIn(true);
                        user.setCreatedAt(System.currentTimeMillis());
                        user.setUpdatedAt(System.currentTimeMillis());

                        saveUserToFirestore(user)
                                .subscribe(
                                        () -> emitter.onSuccess(user),
                                        emitter::onError
                                );
                    })
                    .addOnFailureListener(e ->
                            emitter.onError(new Exception("Firebase registration failed: " + e.getMessage()))
                    );
        });
    }

    @SuppressLint("CheckResult")
    public Single<UserEntity> login(String email, String password) {
        return Single.create(emitter -> {
            firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        FirebaseUser firebaseUser = authResult.getUser();
                        if (firebaseUser == null) {
                            emitter.onError(new Exception("Firebase login failed"));
                            return;
                        }

                        getUserFromFirestore(firebaseUser.getUid())
                                .subscribe(
                                        user -> {
                                            user.setLoggedIn(true);
                                            emitter.onSuccess(user);
                                        },
                                        emitter::onError,
                                        () -> {
                                            UserEntity user = new UserEntity();
                                            user.setId(firebaseUser.getUid());
                                            user.setEmail(email);
                                            user.setLoggedIn(true);
                                            user.setCreatedAt(System.currentTimeMillis());
                                            user.setUpdatedAt(System.currentTimeMillis());
                                            emitter.onSuccess(user);
                                        }
                                );
                    })
                    .addOnFailureListener(e ->
                            emitter.onError(new Exception("Firebase login failed: " + e.getMessage()))
                    );
        });
    }

    @SuppressLint("CheckResult")
    public Single<UserEntity> createUserInFirebase(UserEntity localUser, String password) {
        return Single.create(emitter -> {
            firebaseAuth.createUserWithEmailAndPassword(localUser.getEmail(), password)
                    .addOnSuccessListener(authResult -> {
                        FirebaseUser firebaseUser = authResult.getUser();
                        if (firebaseUser == null) {
                            emitter.onError(new Exception("Failed to create user in Firebase"));
                            return;
                        }

                        localUser.setId(firebaseUser.getUid());
                        localUser.setUpdatedAt(System.currentTimeMillis());

                        saveUserToFirestore(localUser)
                                .subscribe(
                                        () -> emitter.onSuccess(localUser),
                                        emitter::onError
                                );
                    })
                    .addOnFailureListener(e ->
                            emitter.onError(new Exception("Failed to create user in Firebase: " + e.getMessage()))
                    );
        });
    }

    public Completable saveUserToFirestore(UserEntity user) {
        return Completable.create(emitter -> {
            Map<String, Object> userData = new HashMap<>();
            userData.put("id", user.getId());
            userData.put("fullName", user.getFullName());
            userData.put("email", user.getEmail());
            userData.put("isLoggedIn", user.isLoggedIn());
            userData.put("createdAt", user.getCreatedAt());
            userData.put("updatedAt", user.getUpdatedAt());

            firestore.collection(USERS_COLLECTION)
                    .document(user.getId())
                    .set(userData)
                    .addOnSuccessListener(aVoid -> emitter.onComplete())
                    .addOnFailureListener(e ->
                            emitter.onError(new Exception("Failed to save to Firestore: " + e.getMessage()))
                    );
        });
    }

    public Maybe<UserEntity> getUserFromFirestore(String userId) {
        return Maybe.create(emitter -> {
            firestore.collection(USERS_COLLECTION)
                    .document(userId)
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            UserEntity user = documentToUserEntity(doc);
                            emitter.onSuccess(user);
                        } else {
                            emitter.onComplete();
                        }
                    })
                    .addOnFailureListener(emitter::onError);
        });
    }

    private UserEntity documentToUserEntity(DocumentSnapshot doc) {
        UserEntity user = new UserEntity();
        user.setId(doc.getString("id") != null ? doc.getString("id") : doc.getId());
        user.setFullName(doc.getString("fullName"));
        user.setEmail(doc.getString("email"));
        user.setLoggedIn(Boolean.TRUE.equals(doc.getBoolean("isLoggedIn")));
        user.setCreatedAt(doc.getLong("createdAt") != null ? doc.getLong("createdAt") : System.currentTimeMillis());
        user.setUpdatedAt(doc.getLong("updatedAt") != null ? doc.getLong("updatedAt") : System.currentTimeMillis());
        return user;
    }

    public Completable logout() {
        return Completable.fromAction(() -> firebaseAuth.signOut());
    }

    public Single<Boolean> isLoggedIn() {
        return Single.fromCallable(() -> firebaseAuth.getCurrentUser() != null);
    }
    public Single<List<FavoriteMealEntity>> getFavoritesFromFirestore(String userId) {
        return Single.create(emitter -> {
            if (userId == null || userId.isEmpty()) {
                emitter.onSuccess(new ArrayList<>());
                return;
            }

            firestore.collection(USERS_COLLECTION)
                    .document(userId)
                    .collection(FAVORITES_COLLECTION)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        List<FavoriteMealEntity> favorites = new ArrayList<>();
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            FavoriteMealEntity entity = documentToFavoriteEntity(doc, userId);
                            if (entity != null) {
                                favorites.add(entity);
                            }
                        }
                        emitter.onSuccess(favorites);
                    })
                    .addOnFailureListener(e -> emitter.onSuccess(new ArrayList<>()));
        });
    }

    private FavoriteMealEntity documentToFavoriteEntity(DocumentSnapshot doc, String userId) {
        if (doc == null || !doc.exists()) {
            return null;
        }

        FavoriteMealEntity entity = new FavoriteMealEntity();

        String mealId = doc.getString("mealId");
        if (mealId == null || mealId.isEmpty()) {
            mealId = doc.getId();
        }
        entity.setMealId(mealId);
        entity.setUserId(userId);
        entity.setName(doc.getString("name"));
        entity.setAlternateName(doc.getString("alternateName"));
        entity.setCategory(doc.getString("category"));
        entity.setArea(doc.getString("area"));
        entity.setInstructions(doc.getString("instructions"));
        entity.setThumbnailUrl(doc.getString("thumbnailUrl"));
        entity.setTags(doc.getString("tags"));
        entity.setYoutubeUrl(doc.getString("youtubeUrl"));
        entity.setSourceUrl(doc.getString("sourceUrl"));
        entity.setImageSource(doc.getString("imageSource"));
        entity.setCreativeCommonsConfirmed(doc.getString("creativeCommonsConfirmed"));
        entity.setDateModified(doc.getString("dateModified"));
        entity.setIngredientsJson(doc.getString("ingredientsJson"));

        Long createdAt = doc.getLong("createdAt");
        entity.setCreatedAt(createdAt != null ? createdAt : System.currentTimeMillis());

        return entity;
    }
    public Single<List<MealPlanEntity>> getMealPlansFromFirestore(String userId) {
        return Single.create(emitter -> {
            if (userId == null || userId.isEmpty()) {
                emitter.onSuccess(new ArrayList<>());
                return;
            }

            firestore.collection(USERS_COLLECTION)
                    .document(userId)
                    .collection(MEAL_PLANS_COLLECTION)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (emitter.isDisposed()) return;

                        List<MealPlanEntity> mealPlans = new ArrayList<>();
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            MealPlanEntity entity = documentToMealPlanEntity(doc, userId);
                            if (entity != null) {
                                mealPlans.add(entity);
                            }
                        }
                        emitter.onSuccess(mealPlans);
                    })
                    .addOnFailureListener(e -> {
                        if (!emitter.isDisposed()) {
                            emitter.onSuccess(new ArrayList<>());
                        }
                    });
        });
    }

    public Single<List<MealPlanEntity>> getMealPlansByDateFromFirestore(String userId, String date) {
        return Single.create(emitter -> {
            if (userId == null || userId.isEmpty() || date == null || date.isEmpty()) {
                emitter.onSuccess(new ArrayList<>());
                return;
            }

            firestore.collection(USERS_COLLECTION)
                    .document(userId)
                    .collection(MEAL_PLANS_COLLECTION)
                    .whereEqualTo("date", date)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (emitter.isDisposed()) return;

                        List<MealPlanEntity> mealPlans = new ArrayList<>();
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            MealPlanEntity entity = documentToMealPlanEntity(doc, userId);
                            if (entity != null) {
                                mealPlans.add(entity);
                            }
                        }
                        emitter.onSuccess(mealPlans);
                    })
                    .addOnFailureListener(e -> {
                        if (!emitter.isDisposed()) {
                            emitter.onSuccess(new ArrayList<>());
                        }
                    });
        });
    }
    public Single<List<MealPlanEntity>> getMealPlansByDateRangeFromFirestore(String userId, String startDate, String endDate) {
        return Single.create(emitter -> {
            if (userId == null || userId.isEmpty() ||
                    startDate == null || startDate.isEmpty() ||
                    endDate == null || endDate.isEmpty()) {
                emitter.onSuccess(new ArrayList<>());
                return;
            }

            firestore.collection(USERS_COLLECTION)
                    .document(userId)
                    .collection(MEAL_PLANS_COLLECTION)
                    .whereGreaterThanOrEqualTo("date", startDate)
                    .whereLessThanOrEqualTo("date", endDate)
                    .orderBy("date")
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (emitter.isDisposed()) return;

                        List<MealPlanEntity> mealPlans = new ArrayList<>();
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            MealPlanEntity entity = documentToMealPlanEntity(doc, userId);
                            if (entity != null) {
                                mealPlans.add(entity);
                            }
                        }
                        emitter.onSuccess(mealPlans);
                    })
                    .addOnFailureListener(e -> {
                        if (!emitter.isDisposed()) {
                            emitter.onSuccess(new ArrayList<>());
                        }
                    });
        });
    }
    public Completable addAllMealPlansToFirestore(List<MealPlanEntity> entities) {
        return Completable.create(emitter -> {
            if (entities == null || entities.isEmpty()) {
                emitter.onComplete();
                return;
            }

            WriteBatch batch = firestore.batch();

            for (MealPlanEntity entity : entities) {
                if (!MealPlanMapper.isValidMealPlanEntity(entity)) {
                    continue;
                }

                String documentId = MealPlanMapper.generateDocumentId(entity);
                Map<String, Object> mealPlanData = MealPlanMapper.entityToMap(entity);

                if (documentId != null && mealPlanData != null) {
                    batch.set(
                            firestore.collection(USERS_COLLECTION)
                                    .document(entity.getUserId())
                                    .collection(MEAL_PLANS_COLLECTION)
                                    .document(documentId),
                            mealPlanData
                    );
                }
            }

            batch.commit()
                    .addOnSuccessListener(aVoid -> {
                        if (!emitter.isDisposed()) {
                            emitter.onComplete();
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (!emitter.isDisposed()) {
                            emitter.onError(new Exception("Failed to add meal plans to Firestore: " + e.getMessage()));
                        }
                    });
        });
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
