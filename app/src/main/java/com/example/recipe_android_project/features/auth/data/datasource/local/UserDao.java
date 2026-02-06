package com.example.recipe_android_project.features.auth.data.datasource.local;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.recipe_android_project.features.auth.data.entities.UserEntity;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

@Dao
public interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Single<Long> insertUser(UserEntity user);
    @Update
    Single<Integer> updateUser(UserEntity user);
    @Query("UPDATE users SET is_logged_in = :isLoggedIn, updated_at = :updatedAt WHERE id = :userId")
    Single<Integer> updateLoginStatus(String userId, boolean isLoggedIn, long updatedAt);
    @Query("UPDATE users SET is_logged_in = 0")
    Single<Integer> logoutAllUsers();
    @Query("UPDATE users SET full_name = :fullName, updated_at = :updatedAt WHERE id = :userId")
    Single<Integer> updateFullName(String userId, String fullName, long updatedAt);
    @Query("UPDATE users SET password = :password, updated_at = :updatedAt WHERE id = :userId")
    Single<Integer> updatePassword(String userId, String password, long updatedAt);
    @Delete
    Single<Integer> deleteUser(UserEntity user);
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    Maybe<UserEntity> getUserById(String userId);
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    Maybe<UserEntity> getUserByEmail(String email);
    @Query("SELECT * FROM users WHERE is_logged_in = 1 LIMIT 1")
    Maybe<UserEntity> getLoggedInUser();
    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE email = :email)")
    Single<Boolean> isEmailExists(String email);
}
