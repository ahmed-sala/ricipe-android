package com.example.recipe_android_project.features.auth.data.datasource.remote;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.example.recipe_android_project.features.auth.data.entities.UserEntity;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

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


    public UserEntity registerUser(String fullName, String email, String password) throws Exception {
        try {
            Task<AuthResult> task = firebaseAuth.createUserWithEmailAndPassword(email, password);
            AuthResult result = Tasks.await(task);

            FirebaseUser firebaseUser = result.getUser();
            if (firebaseUser == null) {
                throw new Exception("Firebase registration failed");
            }

            UserEntity user = new UserEntity();
            user.setId(firebaseUser.getUid());
            user.setFullName(fullName);
            user.setEmail(email);
            user.setLoggedIn(true);
            user.setCreatedAt(System.currentTimeMillis());
            user.setUpdatedAt(System.currentTimeMillis());

            saveUserToFirestore(user);

            return user;
        } catch (Exception e) {
            throw new Exception("Firebase registration failed: " + e.getMessage());
        }
    }


    public UserEntity login(String email, String password) throws Exception {
        try {
            Task<AuthResult> task = firebaseAuth.signInWithEmailAndPassword(email, password);
            AuthResult result = Tasks.await(task);

            FirebaseUser firebaseUser = result.getUser();
            if (firebaseUser == null) {
                throw new Exception("Firebase login failed");
            }

            UserEntity user = getUserFromFirestore(firebaseUser.getUid());

            if (user == null) {
                user = new UserEntity();
                user.setId(firebaseUser.getUid());
                user.setEmail(email);
                user.setCreatedAt(System.currentTimeMillis());
                user.setUpdatedAt(System.currentTimeMillis());
            }

            user.setLoggedIn(true);
            return user;
        } catch (Exception e) {
            throw new Exception("Firebase login failed: " + e.getMessage());
        }
    }


    public boolean isUserExistsInFirebase(String email) {
        try {
            Task<AuthResult> task = firebaseAuth.signInWithEmailAndPassword(email, "dummy_password_check");
            Tasks.await(task);
            return true;
        } catch (Exception e) {
            String message = e.getMessage();
            if (message != null && message.contains("password is invalid")) {
                return true;
            }
            return false;
        }
    }


    public UserEntity createUserInFirebase(UserEntity localUser, String password) throws Exception {
        try {
            Task<AuthResult> task = firebaseAuth.createUserWithEmailAndPassword(localUser.getEmail(), password);
            AuthResult result = Tasks.await(task);

            FirebaseUser firebaseUser = result.getUser();
            if (firebaseUser == null) {
                throw new Exception("Failed to create user in Firebase");
            }

            localUser.setId(firebaseUser.getUid());
            localUser.setUpdatedAt(System.currentTimeMillis());

            saveUserToFirestore(localUser);

            return localUser;
        } catch (Exception e) {
            throw new Exception("Failed to create user in Firebase: " + e.getMessage());
        }
    }


    public void saveUserToFirestore(UserEntity user) throws Exception {
        try {
            Map<String, Object> userData = new HashMap<>();
            userData.put("id", user.getId());
            userData.put("fullName", user.getFullName());
            userData.put("email", user.getEmail());
            userData.put("isLoggedIn", user.isLoggedIn());
            userData.put("createdAt", user.getCreatedAt());
            userData.put("updatedAt", user.getUpdatedAt());

            Task<Void> task = firestore.collection(USERS_COLLECTION)
                    .document(user.getId())
                    .set(userData);
            Tasks.await(task);
        } catch (Exception e) {
            throw new Exception("Failed to save to Firestore: " + e.getMessage());
        }
    }

    public UserEntity getUserFromFirestore(String userId) {
        try {
            Task<DocumentSnapshot> task = firestore.collection(USERS_COLLECTION)
                    .document(userId)
                    .get();
            DocumentSnapshot doc = Tasks.await(task);

            if (doc.exists()) {
                UserEntity user = new UserEntity();
                user.setId(doc.getString("id") != null ? doc.getString("id") : userId);
                user.setFullName(doc.getString("fullName"));
                user.setEmail(doc.getString("email"));
                user.setLoggedIn(Boolean.TRUE.equals(doc.getBoolean("isLoggedIn")));
                user.setCreatedAt(doc.getLong("createdAt") != null ? doc.getLong("createdAt") : System.currentTimeMillis());
                user.setUpdatedAt(doc.getLong("updatedAt") != null ? doc.getLong("updatedAt") : System.currentTimeMillis());
                return user;
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public UserEntity getUserFromFirestoreByEmail(String email) {
        try {
            Task<QuerySnapshot> task = firestore.collection(USERS_COLLECTION)
                    .whereEqualTo("email", email)
                    .limit(1)
                    .get();
            QuerySnapshot snapshot = Tasks.await(task);

            if (!snapshot.isEmpty()) {
                DocumentSnapshot doc = snapshot.getDocuments().get(0);
                UserEntity user = new UserEntity();
                user.setId(doc.getString("id"));
                user.setFullName(doc.getString("fullName"));
                user.setEmail(doc.getString("email"));
                user.setLoggedIn(Boolean.TRUE.equals(doc.getBoolean("isLoggedIn")));
                user.setCreatedAt(doc.getLong("createdAt") != null ? doc.getLong("createdAt") : System.currentTimeMillis());
                user.setUpdatedAt(doc.getLong("updatedAt") != null ? doc.getLong("updatedAt") : System.currentTimeMillis());
                return user;
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }


    public void logout() {
        firebaseAuth.signOut();
    }

    public FirebaseUser getCurrentFirebaseUser() {
        return firebaseAuth.getCurrentUser();
    }
}
