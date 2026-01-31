package com.example.recipe_android_project.features.auth.data.datasource.local;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.recipe_android_project.core.config.DbManager;
import com.example.recipe_android_project.core.helper.SharedPreferencesManager;
import com.example.recipe_android_project.core.utils.PasswordHasher;
import com.example.recipe_android_project.core.utils.PasswordHasher.PasswordValidationResult;
import com.example.recipe_android_project.features.auth.data.entities.UserEntity;

import java.util.List;
import java.util.UUID;

public class AuthLocalDatasource {

    private static final String KEY_USER_ID = "logged_in_user_id";
    private static final String KEY_USER_EMAIL = "logged_in_user_email";
    private static final String KEY_USER_NAME = "logged_in_user_name";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_LOGIN_TIME = "login_time";

    private final UserDao userDao;
    private final SharedPreferencesManager prefsManager;

    public AuthLocalDatasource(Context context) {
        DbManager dbManager = DbManager.getInstance(context);
        userDao = dbManager.userDao();
        prefsManager = SharedPreferencesManager.getInstance(context);
    }


    public UserEntity registerUser(String fullName, String email, String password) throws Exception {
        PasswordValidationResult validationResult = PasswordHasher.validatePasswordStrength(password);
        if (!validationResult.isValid()) {
            throw new Exception(validationResult.getMessage());
        }

        if (userDao.isEmailExists(email)) {
            throw new Exception("Email already registered");
        }

        String hashedPassword = PasswordHasher.hashPassword(password);

        UserEntity user = new UserEntity();
        user.setId(UUID.randomUUID().toString());
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPassword(hashedPassword);
        user.setLoggedIn(true);
        user.setCreatedAt(System.currentTimeMillis());
        user.setUpdatedAt(System.currentTimeMillis());

        long result = userDao.insertUser(user);

        if (result > 0) {
            saveUserSession(user);
            user.setPassword(null);
            return user;
        } else {
            throw new Exception("Failed to register user");
        }
    }

    public UserEntity login(String email, String password) throws Exception {
        UserEntity user = userDao.getUserByEmail(email);

        if (user == null) {
            throw new Exception("Invalid email or password");
        }

        boolean isPasswordValid = PasswordHasher.verifyPassword(password, user.getPassword());

        if (!isPasswordValid) {
            throw new Exception("Invalid email or password");
        }

        if (PasswordHasher.needsRehash(user.getPassword())) {
            String newHash = PasswordHasher.hashPassword(password);
            userDao.updatePassword(user.getId(), newHash, System.currentTimeMillis());
        }

        userDao.logoutAllUsers();
        userDao.updateLoginStatus(user.getId(), true, System.currentTimeMillis());
        user.setLoggedIn(true);

        saveUserSession(user);

        user.setPassword(null);
        return user;
    }

    public boolean logout() {
        int result = userDao.logoutAllUsers();
        clearUserSession();
        return result >= 0;
    }

    public boolean logoutUser(String userId) {
        int result = userDao.updateLoginStatus(userId, false, System.currentTimeMillis());

        String currentUserId = prefsManager.getString(KEY_USER_ID, null);
        if (userId.equals(currentUserId)) {
            clearUserSession();
        }

        return result > 0;
    }


    private void saveUserSession(UserEntity user) {
        prefsManager.putString(KEY_USER_ID, user.getId());
        prefsManager.putString(KEY_USER_EMAIL, user.getEmail());
        prefsManager.putString(KEY_USER_NAME, user.getFullName());
        prefsManager.putBoolean(KEY_IS_LOGGED_IN, true);
        prefsManager.putLong(KEY_LOGIN_TIME, System.currentTimeMillis());
    }

    private void clearUserSession() {
        prefsManager.remove(KEY_USER_ID);
        prefsManager.remove(KEY_USER_EMAIL);
        prefsManager.remove(KEY_USER_NAME);
        prefsManager.putBoolean(KEY_IS_LOGGED_IN, false);
        prefsManager.remove(KEY_LOGIN_TIME);
    }

    public String getSessionUserId() {
        return prefsManager.getString(KEY_USER_ID, null);
    }

    public String getSessionUserEmail() {
        return prefsManager.getString(KEY_USER_EMAIL, null);
    }

    public String getSessionUserName() {
        return prefsManager.getString(KEY_USER_NAME, null);
    }

    public boolean isSessionLoggedIn() {
        return prefsManager.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public long getSessionLoginTime() {
        return prefsManager.getLong(KEY_LOGIN_TIME, 0);
    }



    public UserEntity getCurrentUser() throws Exception {
        String userId = prefsManager.getString(KEY_USER_ID, null);

        UserEntity user;
        if (userId != null) {
            user = userDao.getUserById(userId);
        } else {
            user = userDao.getLoggedInUser();
        }

        if (user != null) {
            user.setPassword(null);
            return user;
        } else {
            clearUserSession();
            throw new Exception("No user logged in");
        }
    }

    public LiveData<UserEntity> getCurrentUserLive() {
        return userDao.getLoggedInUserLive();
    }


    public boolean isUserLoggedIn() {
        boolean isLoggedInPrefs = prefsManager.getBoolean(KEY_IS_LOGGED_IN, false);

        if (isLoggedInPrefs) {
            String userId = prefsManager.getString(KEY_USER_ID, null);
            if (userId != null) {
                UserEntity user = userDao.getUserById(userId);
                if (user != null && user.isLoggedIn()) {
                    return true;
                }
            }
            clearUserSession();
        }

        return userDao.isAnyUserLoggedIn();
    }

    public boolean isEmailExists(String email) {
        return userDao.isEmailExists(email);
    }


    public UserEntity getUserById(String userId) throws Exception {
        UserEntity user = userDao.getUserById(userId);
        if (user != null) {
            user.setPassword(null);
            return user;
        } else {
            throw new Exception("User not found");
        }
    }

    public LiveData<UserEntity> getUserByIdLive(String userId) {
        return userDao.getUserByIdLive(userId);
    }

    public List<UserEntity> getAllUsers() {
        List<UserEntity> users = userDao.getAllUsers();
        for (UserEntity user : users) {
            user.setPassword(null);
        }
        return users;
    }

    public LiveData<List<UserEntity>> getAllUsersLive() {
        return userDao.getAllUsersLive();
    }


    public boolean updateUser(UserEntity user) {
        user.setUpdatedAt(System.currentTimeMillis());
        int result = userDao.updateUser(user);

        String currentUserId = prefsManager.getString(KEY_USER_ID, null);
        if (user.getId().equals(currentUserId)) {
            prefsManager.putString(KEY_USER_NAME, user.getFullName());
            prefsManager.putString(KEY_USER_EMAIL, user.getEmail());
        }

        return result > 0;
    }

    public boolean updateFullName(String userId, String fullName) {
        int result = userDao.updateFullName(userId, fullName, System.currentTimeMillis());

        String currentUserId = prefsManager.getString(KEY_USER_ID, null);
        if (userId.equals(currentUserId)) {
            prefsManager.putString(KEY_USER_NAME, fullName);
        }

        return result > 0;
    }

    public boolean updatePassword(String userId, String oldPassword, String newPassword) throws Exception {
        PasswordValidationResult validationResult = PasswordHasher.validatePasswordStrength(newPassword);
        if (!validationResult.isValid()) {
            throw new Exception(validationResult.getMessage());
        }

        UserEntity user = userDao.getUserById(userId);

        if (user == null) {
            throw new Exception("User not found");
        }

        if (!PasswordHasher.verifyPassword(oldPassword, user.getPassword())) {
            throw new Exception("Current password is incorrect");
        }

        String hashedNewPassword = PasswordHasher.hashPassword(newPassword);
        int result = userDao.updatePassword(userId, hashedNewPassword, System.currentTimeMillis());

        return result > 0;
    }


    public boolean deleteUser(UserEntity user) {
        int result = userDao.deleteUser(user);

        String currentUserId = prefsManager.getString(KEY_USER_ID, null);
        if (user.getId().equals(currentUserId)) {
            clearUserSession();
        }

        return result > 0;
    }

    public boolean deleteUserById(String userId) {
        int result = userDao.deleteUserById(userId);

        String currentUserId = prefsManager.getString(KEY_USER_ID, null);
        if (userId.equals(currentUserId)) {
            clearUserSession();
        }

        return result > 0;
    }

    public boolean deleteAllUsers() {
        int result = userDao.deleteAllUsers();
        clearUserSession();
        return result >= 0;
    }


    public PasswordValidationResult validatePassword(String password) {
        return PasswordHasher.validatePasswordStrength(password);
    }
}
