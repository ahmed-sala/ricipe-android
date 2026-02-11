package com.example.recipe_android_project.features.auth.data.datasource.remote;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.util.Log;

import com.example.recipe_android_project.features.auth.data.entities.UserEntity;
import com.example.recipe_android_project.features.home.data.entities.FavoriteMealEntity;
import com.example.recipe_android_project.features.plan.data.entity.MealPlanEntity;
import com.example.recipe_android_project.features.plan.data.mapper.MealPlanMapper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

public class AuthRemoteDatasource {

    private static final String TAG = "AuthRemoteDatasource";
    private static final String USERS_COLLECTION = "users";
    private static final String FAVORITES_COLLECTION = "favorites";
    private static final String MEAL_PLANS_COLLECTION = "meal_plans";

    private final FirebaseAuth firebaseAuth;
    private final FirebaseFirestore firestore;
    private final Context context;
    private ConnectivityManager connectivityManager;

    public AuthRemoteDatasource(Context context) {
        this.context = context;
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.firestore = FirebaseFirestore.getInstance();
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
                            emitter.onError(new Exception(
                                    "Firebase registration failed: " + e.getMessage()))
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
                            emitter.onError(new Exception(
                                    "Firebase login failed: " + e.getMessage()))
                    );
        });
    }


    public Completable logout() {
        return Completable.fromAction(() -> firebaseAuth.signOut());
    }


    public boolean isFirebaseAuthenticated() {
        return firebaseAuth.getCurrentUser() != null;
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
                            emitter.onError(new Exception(
                                    "Failed to save to Firestore: " + e.getMessage()))
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
                            emitter.onSuccess(documentToUserEntity(doc));
                        } else {
                            emitter.onComplete();
                        }
                    })
                    .addOnFailureListener(emitter::onError);
        });
    }


    @SuppressLint("CheckResult")
    public Single<String> createUserInFirebaseForSync(UserEntity localUser) {
        return Single.create(emitter -> {
            String plainPassword = localUser.getPendingPlainPassword();

            if (plainPassword == null || plainPassword.isEmpty()) {
                emitter.onError(new Exception("No password available for Firebase registration"));
                return;
            }

            Log.d(TAG, "Creating Firebase user for: " + localUser.getEmail());

            firebaseAuth.createUserWithEmailAndPassword(localUser.getEmail(), plainPassword)
                    .addOnSuccessListener(authResult -> {
                        FirebaseUser firebaseUser = authResult.getUser();
                        if (firebaseUser == null) {
                            emitter.onError(new Exception("Failed to create Firebase user"));
                            return;
                        }

                        String firebaseUid = firebaseUser.getUid();
                        Log.d(TAG, "Firebase user created, UID: " + firebaseUid);

                        UserEntity firestoreUser = new UserEntity();
                        firestoreUser.setId(firebaseUid);
                        firestoreUser.setFullName(localUser.getFullName());
                        firestoreUser.setEmail(localUser.getEmail());
                        firestoreUser.setLoggedIn(true);
                        firestoreUser.setCreatedAt(localUser.getCreatedAt());
                        firestoreUser.setUpdatedAt(System.currentTimeMillis());

                        saveUserToFirestore(firestoreUser)
                                .subscribe(
                                        () -> emitter.onSuccess(firebaseUid),
                                        error -> emitter.onSuccess(firebaseUid)
                                );
                    })
                    .addOnFailureListener(e -> {
                        if (e.getMessage() != null
                                && e.getMessage().contains("email address is already in use")) {
                            signInAndGetUid(localUser.getEmail(), plainPassword)
                                    .subscribe(
                                            emitter::onSuccess,
                                            emitter::onError
                                    );
                        } else {
                            emitter.onError(new Exception(
                                    "Firebase registration failed: " + e.getMessage()));
                        }
                    });
        });
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


    private Single<String> signInAndGetUid(String email, String password) {
        return Single.create(emitter -> {
            firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        FirebaseUser user = authResult.getUser();
                        if (user != null) {
                            emitter.onSuccess(user.getUid());
                        } else {
                            emitter.onError(new Exception("Failed to sign in"));
                        }
                    })
                    .addOnFailureListener(e ->
                            emitter.onError(new Exception(
                                    "Sign in failed: " + e.getMessage()))
                    );
        });
    }

    private UserEntity documentToUserEntity(DocumentSnapshot doc) {
        UserEntity user = new UserEntity();
        user.setId(doc.getString("id") != null ? doc.getString("id") : doc.getId());
        user.setFullName(doc.getString("fullName"));
        user.setEmail(doc.getString("email"));
        user.setLoggedIn(Boolean.TRUE.equals(doc.getBoolean("isLoggedIn")));
        user.setCreatedAt(doc.getLong("createdAt") != null
                ? doc.getLong("createdAt") : System.currentTimeMillis());
        user.setUpdatedAt(doc.getLong("updatedAt") != null
                ? doc.getLong("updatedAt") : System.currentTimeMillis());
        return user;
    }

    private FavoriteMealEntity documentToFavoriteEntity(DocumentSnapshot doc, String userId) {
        if (doc == null || !doc.exists()) return null;

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

    private MealPlanEntity documentToMealPlanEntity(DocumentSnapshot doc, String userId) {
        if (doc == null || !doc.exists()) return null;

        Map<String, Object> data = doc.getData();
        if (data == null) return null;

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
