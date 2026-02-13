package com.example.recipe_android_project.core.helper;

import android.content.Context;

import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UserSessionManager {

    private static final String KEY_USER_ID = "logged_in_user_id";
    private static final String KEY_FIREBASE_UID = "firebase_uid";
    private static final String KEY_USER_EMAIL = "logged_in_user_email";
    private static final String KEY_USER_NAME = "logged_in_user_name";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_LOGIN_TIME = "login_time";

    private static volatile UserSessionManager instance;
    private final SharedPreferencesManager prefsManager;

    private UserSessionManager(Context context) {
        prefsManager = SharedPreferencesManager.getInstance(context);
    }

    public static UserSessionManager getInstance(Context context) {
        if (instance == null) {
            synchronized (UserSessionManager.class) {
                if (instance == null) {
                    instance = new UserSessionManager(context.getApplicationContext());
                }
            }
        }
        return instance;
    }
    public void createSession(String localUserId, String firebaseUid, String email, String fullName) {
        prefsManager.putString(KEY_USER_ID, localUserId);
        prefsManager.putString(KEY_FIREBASE_UID, firebaseUid);
        prefsManager.putString(KEY_USER_EMAIL, email);
        prefsManager.putString(KEY_USER_NAME, fullName);
        prefsManager.putBoolean(KEY_IS_LOGGED_IN, true);
        prefsManager.putLong(KEY_LOGIN_TIME, System.currentTimeMillis());
    }
    public void createSessionWithFirebaseUid(String firebaseUid, String email, String fullName) {
        prefsManager.putString(KEY_USER_ID, firebaseUid);
        prefsManager.putString(KEY_FIREBASE_UID, firebaseUid);
        prefsManager.putString(KEY_USER_EMAIL, email);
        prefsManager.putString(KEY_USER_NAME, fullName);
        prefsManager.putBoolean(KEY_IS_LOGGED_IN, true);
        prefsManager.putLong(KEY_LOGIN_TIME, System.currentTimeMillis());
    }
    public void clearSession() {
        prefsManager.remove(KEY_USER_ID);
        prefsManager.remove(KEY_FIREBASE_UID);
        prefsManager.remove(KEY_USER_EMAIL);
        prefsManager.remove(KEY_USER_NAME);
        prefsManager.putBoolean(KEY_IS_LOGGED_IN, false);
        prefsManager.remove(KEY_LOGIN_TIME);
    }
    public void updateUserInfo(String fullName, String email) {
        prefsManager.putString(KEY_USER_NAME, fullName);
        prefsManager.putString(KEY_USER_EMAIL, email);
    }

    public void updateFirebaseUid(String firebaseUid) {
        prefsManager.putString(KEY_FIREBASE_UID, firebaseUid);
    }
    @Nullable
    public String getCurrentUserId() {
        return prefsManager.getString(KEY_USER_ID, null);
    }
    @Nullable
    public String getFirebaseUid() {
        String storedUid = prefsManager.getString(KEY_FIREBASE_UID, null);
        if (storedUid != null && !storedUid.isEmpty()) {
            return storedUid;
        }

        String authUid = getFirebaseAuthUid();
        if (authUid != null) {
            prefsManager.putString(KEY_FIREBASE_UID, authUid);
            return authUid;
        }
        return getCurrentUserId();
    }
    @Nullable
    public String getFirebaseAuthUid() {
        try {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            return user != null ? user.getUid() : null;
        } catch (Exception e) {
            return null;
        }
    }
    public boolean isLoggedIn() {
        return prefsManager.getBoolean(KEY_IS_LOGGED_IN, false);
    }
    public boolean hasValidSession() {
        return isLoggedIn() && getCurrentUserId() != null && !getCurrentUserId().isEmpty();
    }
    @Nullable
    public String getCurrentUserIdOrNull() {
        if (!hasValidSession()) {
            return null;
        }
        return getCurrentUserId();
    }
    @Nullable
    public String getFirebaseUidOrNull() {
        if (!hasValidSession()) {
            return null;
        }
        return getFirebaseUid();
    }
}
