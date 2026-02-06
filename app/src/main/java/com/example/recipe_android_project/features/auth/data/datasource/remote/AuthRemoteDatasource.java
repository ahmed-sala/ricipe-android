package com.example.recipe_android_project.features.auth.data.datasource.remote;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.example.recipe_android_project.features.auth.data.entities.UserEntity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

public class AuthRemoteDatasource {
    private static final String USERS_COLLECTION = "users";

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
}
