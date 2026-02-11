package com.example.recipe_android_project.features.profile.data.datasource.remote;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.util.Log;

import com.example.recipe_android_project.features.auth.data.entities.UserEntity;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.rxjava3.core.Completable;

public class ProfileRemoteDatasource {

    private static final String TAG = "ProfileRemoteDatasource";
    private static final String USERS_COLLECTION = "users";

    private final FirebaseAuth firebaseAuth;
    private final FirebaseFirestore firestore;
    private final Context context;
    private ConnectivityManager connectivityManager;

    public ProfileRemoteDatasource(Context context) {
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


    public Completable saveProfileToFirestore(UserEntity user) {
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
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Profile saved to Firestore");
                        emitter.onComplete();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Firestore save failed: " + e.getMessage());
                        emitter.onError(new Exception("Failed to save profile: " + e.getMessage()));
                    });
        });
    }


    public Completable updatePassword(String oldPassword, String newPassword) {
        return Completable.create(emitter -> {
            FirebaseUser currentUser = firebaseAuth.getCurrentUser();

            if (currentUser == null) {
                emitter.onError(new Exception("No user logged in to Firebase"));
                return;
            }

            String email = currentUser.getEmail();
            if (email == null) {
                emitter.onError(new Exception("User email not found"));
                return;
            }

            AuthCredential credential = EmailAuthProvider.getCredential(email, oldPassword);

            currentUser.reauthenticate(credential)
                    .addOnSuccessListener(aVoid -> {
                        if (emitter.isDisposed()) return;

                        currentUser.updatePassword(newPassword)
                                .addOnSuccessListener(aVoid2 -> {
                                    if (!emitter.isDisposed()) {
                                        Log.d(TAG, "Password updated on Firebase");
                                        emitter.onComplete();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    if (!emitter.isDisposed()) {
                                        emitter.onError(new Exception(
                                                "Failed to update password: " + e.getMessage()));
                                    }
                                });
                    })
                    .addOnFailureListener(e -> {
                        if (!emitter.isDisposed()) {
                            emitter.onError(new Exception(
                                    "Re-authentication failed: " + e.getMessage()));
                        }
                    });
        });
    }

    public Completable updatePasswordWithStoredCredentials(String email,
                                                           String oldPassword,
                                                           String newPassword) {
        return Completable.create(emitter -> {
            FirebaseUser currentUser = firebaseAuth.getCurrentUser();

            if (currentUser != null) {
                reauthAndUpdatePassword(currentUser, email, oldPassword, newPassword, emitter);
            } else {
                firebaseAuth.signInWithEmailAndPassword(email, oldPassword)
                        .addOnSuccessListener(authResult -> {
                            if (emitter.isDisposed()) return;

                            FirebaseUser user = authResult.getUser();
                            if (user == null) {
                                emitter.onError(new Exception("Failed to authenticate"));
                                return;
                            }
                            reauthAndUpdatePassword(user, email, oldPassword, newPassword, emitter);
                        })
                        .addOnFailureListener(e -> {
                            if (!emitter.isDisposed()) {
                                emitter.onError(new Exception(
                                        "Authentication failed: " + e.getMessage()));
                            }
                        });
            }
        });
    }


    public boolean isFirebaseAuthenticated() {
        return firebaseAuth.getCurrentUser() != null;
    }

    private void reauthAndUpdatePassword(FirebaseUser user, String email,
                                         String oldPassword, String newPassword,
                                         io.reactivex.rxjava3.core.CompletableEmitter emitter) {

        AuthCredential credential = EmailAuthProvider.getCredential(email, oldPassword);

        user.reauthenticate(credential)
                .addOnSuccessListener(aVoid -> {
                    if (emitter.isDisposed()) return;

                    user.updatePassword(newPassword)
                            .addOnSuccessListener(aVoid2 -> {
                                if (!emitter.isDisposed()) {
                                    Log.d(TAG, "Password updated successfully");
                                    emitter.onComplete();
                                }
                            })
                            .addOnFailureListener(e -> {
                                if (!emitter.isDisposed()) {
                                    emitter.onError(new Exception(
                                            "Failed to update password: " + e.getMessage()));
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    if (!emitter.isDisposed()) {
                        emitter.onError(new Exception(
                                "Re-authentication failed: " + e.getMessage()));
                    }
                });
    }
}
