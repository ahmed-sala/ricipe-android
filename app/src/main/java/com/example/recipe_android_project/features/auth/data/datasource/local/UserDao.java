package com.example.recipe_android_project.features.auth.data.datasource.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.recipe_android_project.features.auth.data.entities.UserEntity;

import java.util.List;

@Dao
public interface UserDao {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertUser(UserEntity user);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insertUsers(List<UserEntity> users);


    @Update
    int updateUser(UserEntity user);

    @Query("UPDATE users SET is_logged_in = :isLoggedIn, updated_at = :updatedAt WHERE id = :userId")
    int updateLoginStatus(String userId, boolean isLoggedIn, long updatedAt);

    @Query("UPDATE users SET is_logged_in = 0")
    int logoutAllUsers();

    @Query("UPDATE users SET full_name = :fullName, updated_at = :updatedAt WHERE id = :userId")
    int updateFullName(String userId, String fullName, long updatedAt);

    @Query("UPDATE users SET password = :password, updated_at = :updatedAt WHERE id = :userId")
    int updatePassword(String userId, String password, long updatedAt);


    @Delete
    int deleteUser(UserEntity user);

    @Query("DELETE FROM users WHERE id = :userId")
    int deleteUserById(String userId);

    @Query("DELETE FROM users")
    int deleteAllUsers();


    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    UserEntity getUserById(String userId);

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    LiveData<UserEntity> getUserByIdLive(String userId);

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    UserEntity getUserByEmail(String email);

    @Query("SELECT * FROM users WHERE email = :email AND password = :password LIMIT 1")
    UserEntity getUserByEmailAndPassword(String email, String password);

    @Query("SELECT * FROM users WHERE is_logged_in = 1 LIMIT 1")
    UserEntity getLoggedInUser();

    @Query("SELECT * FROM users WHERE is_logged_in = 1 LIMIT 1")
    LiveData<UserEntity> getLoggedInUserLive();

    @Query("SELECT * FROM users ORDER BY created_at DESC")
    List<UserEntity> getAllUsers();

    @Query("SELECT * FROM users ORDER BY created_at DESC")
    LiveData<List<UserEntity>> getAllUsersLive();


    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE email = :email)")
    boolean isEmailExists(String email);

    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE id = :userId)")
    boolean isUserExists(String userId);

    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE is_logged_in = 1)")
    boolean isAnyUserLoggedIn();

    @Query("SELECT COUNT(*) FROM users")
    int getUserCount();
}
