package com.example.recipe_android_project.features.auth.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.example.recipe_android_project.core.config.ResultCallback;
import com.example.recipe_android_project.core.utils.PasswordHasher;
import com.example.recipe_android_project.core.utils.PasswordHasher.PasswordValidationResult;
import com.example.recipe_android_project.features.auth.data.datasource.local.AuthLocalDatasource;
import com.example.recipe_android_project.features.auth.data.datasource.remote.AuthRemoteDatasource;
import com.example.recipe_android_project.features.auth.data.entities.UserEntity;
import com.example.recipe_android_project.features.auth.data.mapper.UserMapper;
import com.example.recipe_android_project.features.auth.domain.model.User;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AuthRepository {

    private static final String TAG = "AuthRepository";

    private final AuthLocalDatasource localDatasource;
    private final AuthRemoteDatasource firebaseDatasource;
    private final ExecutorService executorService;

    public AuthRepository(Context context) {
        this.localDatasource = new AuthLocalDatasource(context);
        this.firebaseDatasource = new AuthRemoteDatasource(context);
        this.executorService = Executors.newSingleThreadExecutor();
    }


    public void register(String fullName, String email, String password, ResultCallback<User> callback) {
        executorService.execute(() -> {
            try {
                UserEntity entity;

                if (firebaseDatasource.isNetworkAvailable()) {
                    entity = registerWithFirebaseSync(fullName, email, password);
                } else {
                    entity = localDatasource.registerUser(fullName, email, password);
                }

                User user = UserMapper.toDomain(entity);
                callback.onSuccess(user);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    private UserEntity registerWithFirebaseSync(String fullName, String email, String password) throws Exception {
        UserEntity firebaseUser = firebaseDatasource.registerUser(fullName, email, password);

        if (localDatasource.isEmailExists(email)) {
            throw new Exception("Email already registered locally");
        }

        UserEntity localUser = new UserEntity();
        localUser.setId(firebaseUser.getId());
        localUser.setFullName(fullName);
        localUser.setEmail(email);
        localUser.setPassword(PasswordHasher.hashPassword(password));
        localUser.setLoggedIn(true);
        localUser.setCreatedAt(firebaseUser.getCreatedAt());
        localUser.setUpdatedAt(firebaseUser.getUpdatedAt());

        try {
            localDatasource.registerUser(fullName, email, password);
        } catch (Exception e) {
        }

        firebaseUser.setPassword(null);
        return firebaseUser;
    }


    public void login(String email, String password, ResultCallback<User> callback) {
        executorService.execute(() -> {
            try {
                UserEntity entity;

                if (firebaseDatasource.isNetworkAvailable()) {
                    entity = loginWithFirebaseSync(email, password);
                } else {
                    entity = localDatasource.login(email, password);
                }

                User user = UserMapper.toDomain(entity);
                callback.onSuccess(user);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    private UserEntity loginWithFirebaseSync(String email, String password) throws Exception {
        boolean existsInLocal = localDatasource.isEmailExists(email);
        UserEntity firebaseUser = null;
        UserEntity localUser = null;

        try {
            firebaseUser = firebaseDatasource.login(email, password);
        } catch (Exception e) {
        }

        if (firebaseUser != null) {
            if (existsInLocal) {
                localUser = localDatasource.login(email, password);
                syncFirestoreToLocal(firebaseUser, localUser);
                localUser.setPassword(null);
                return localUser;
            } else {
                localUser = createLocalUserFromFirebase(firebaseUser, password);
                localUser.setPassword(null);
                return localUser;
            }
        } else {
            if (existsInLocal) {
                localUser = localDatasource.login(email, password);
                try {
                    createFirebaseUserFromLocal(localUser, password);
                } catch (Exception e) {
                }
                localUser.setPassword(null);
                return localUser;
            } else {
                throw new Exception("Invalid email or password");
            }
        }
    }

    private void syncFirestoreToLocal(UserEntity firebaseUser, UserEntity localUser) {
        try {
            if (firebaseUser.getFullName() != null && !firebaseUser.getFullName().equals(localUser.getFullName())) {
                localDatasource.updateFullName(localUser.getId(), firebaseUser.getFullName());
            }
            firebaseDatasource.saveUserToFirestore(localUser);
        } catch (Exception e) {
        }
    }

    private UserEntity createLocalUserFromFirebase(UserEntity firebaseUser, String password) throws Exception {
        UserEntity newLocalUser = new UserEntity();
        newLocalUser.setId(firebaseUser.getId());
        newLocalUser.setFullName(firebaseUser.getFullName());
        newLocalUser.setEmail(firebaseUser.getEmail());
        newLocalUser.setPassword(PasswordHasher.hashPassword(password));
        newLocalUser.setLoggedIn(true);
        newLocalUser.setCreatedAt(firebaseUser.getCreatedAt());
        newLocalUser.setUpdatedAt(System.currentTimeMillis());

        return localDatasource.registerUser(
                firebaseUser.getFullName(),
                firebaseUser.getEmail(),
                password
        );
    }

    private void createFirebaseUserFromLocal(UserEntity localUser, String password) {
        try {
            firebaseDatasource.createUserInFirebase(localUser, password);
        } catch (Exception e) {
        }
    }


    public void logout(ResultCallback<Boolean> callback) {
        executorService.execute(() -> {
            try {
                boolean result = localDatasource.logout();

                if (firebaseDatasource.isNetworkAvailable()) {
                    firebaseDatasource.logout();
                }

                callback.onSuccess(result);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    public void logoutUser(String userId, ResultCallback<Boolean> callback) {
        executorService.execute(() -> {
            try {
                boolean result = localDatasource.logoutUser(userId);

                if (firebaseDatasource.isNetworkAvailable()) {
                    firebaseDatasource.logout();
                }

                callback.onSuccess(result);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }


    public String getSessionUserId() {
        return localDatasource.getSessionUserId();
    }

    public String getSessionUserEmail() {
        return localDatasource.getSessionUserEmail();
    }

    public String getSessionUserName() {
        return localDatasource.getSessionUserName();
    }

    public boolean isSessionLoggedIn() {
        return localDatasource.isSessionLoggedIn();
    }


    public void getCurrentUser(ResultCallback<User> callback) {
        executorService.execute(() -> {
            try {
                UserEntity entity = localDatasource.getCurrentUser();
                User user = UserMapper.toDomain(entity);
                callback.onSuccess(user);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    public LiveData<User> getCurrentUserLive() {
        return Transformations.map(
                localDatasource.getCurrentUserLive(),
                UserMapper::toDomain
        );
    }

    public void isUserLoggedIn(ResultCallback<Boolean> callback) {
        executorService.execute(() -> {
            try {
                boolean result = localDatasource.isUserLoggedIn();
                callback.onSuccess(result);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }


    public void getUserById(String userId, ResultCallback<User> callback) {
        executorService.execute(() -> {
            try {
                UserEntity entity = localDatasource.getUserById(userId);
                User user = UserMapper.toDomain(entity);
                callback.onSuccess(user);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    public LiveData<User> getUserByIdLive(String userId) {
        return Transformations.map(
                localDatasource.getUserByIdLive(userId),
                UserMapper::toDomain
        );
    }

    public void getAllUsers(ResultCallback<List<User>> callback) {
        executorService.execute(() -> {
            try {
                List<UserEntity> entities = localDatasource.getAllUsers();
                List<User> users = UserMapper.toDomainListFromEntities(entities);
                callback.onSuccess(users);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    public LiveData<List<User>> getAllUsersLive() {
        return Transformations.map(
                localDatasource.getAllUsersLive(),
                UserMapper::toDomainListFromEntities
        );
    }


    public void isEmailExists(String email, ResultCallback<Boolean> callback) {
        executorService.execute(() -> {
            try {
                boolean exists = localDatasource.isEmailExists(email);
                callback.onSuccess(exists);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    public void validatePassword(String password, ResultCallback<PasswordValidationResult> callback) {
        executorService.execute(() -> {
            try {
                PasswordValidationResult result = localDatasource.validatePassword(password);
                callback.onSuccess(result);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }


    public void updateUser(User user, ResultCallback<Boolean> callback) {
        executorService.execute(() -> {
            try {
                UserEntity entity = UserMapper.toEntity(user);
                boolean result = localDatasource.updateUser(entity);

                if (result && firebaseDatasource.isNetworkAvailable()) {
                    try {
                        firebaseDatasource.saveUserToFirestore(entity);
                    } catch (Exception e) {
                    }
                }

                callback.onSuccess(result);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    public void updateFullName(String userId, String fullName, ResultCallback<Boolean> callback) {
        executorService.execute(() -> {
            try {
                boolean result = localDatasource.updateFullName(userId, fullName);

                if (result && firebaseDatasource.isNetworkAvailable()) {
                    try {
                        UserEntity user = localDatasource.getUserById(userId);
                        if (user != null) {
                            user.setPassword(null);
                            firebaseDatasource.saveUserToFirestore(user);
                        }
                    } catch (Exception e) {
                    }
                }

                callback.onSuccess(result);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    public void updatePassword(String userId, String oldPassword, String newPassword, ResultCallback<Boolean> callback) {
        executorService.execute(() -> {
            try {
                boolean result = localDatasource.updatePassword(userId, oldPassword, newPassword);
                callback.onSuccess(result);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }


    public void deleteUser(String userId, ResultCallback<Boolean> callback) {
        executorService.execute(() -> {
            try {
                boolean result = localDatasource.deleteUserById(userId);
                callback.onSuccess(result);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    public void deleteUser(User user, ResultCallback<Boolean> callback) {
        executorService.execute(() -> {
            try {
                UserEntity entity = UserMapper.toEntity(user);
                boolean result = localDatasource.deleteUser(entity);
                callback.onSuccess(result);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    public void deleteAllUsers(ResultCallback<Boolean> callback) {
        executorService.execute(() -> {
            try {
                boolean result = localDatasource.deleteAllUsers();
                callback.onSuccess(result);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }


    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
    public boolean isUserLoggedIn() {
        return localDatasource.isUserLoggedIn();
    }
}
