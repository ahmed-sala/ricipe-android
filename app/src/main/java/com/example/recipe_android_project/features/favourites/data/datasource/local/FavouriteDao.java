package com.example.recipe_android_project.features.favourites.data.datasource.local;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Query;

import com.example.recipe_android_project.features.home.data.entities.FavoriteMealEntity;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

@Dao
public interface FavouriteDao {
    @Query("DELETE FROM favorite_meals WHERE meal_id = :mealId AND user_id = :userId")
    Completable deleteFavoriteByMealIdAndUserId(String mealId, String userId);
    @Query("SELECT * FROM favorite_meals WHERE user_id = :userId ORDER BY created_at DESC")
    Flowable<List<FavoriteMealEntity>> getAllFavoritesByUserId(String userId);
    @Query("SELECT EXISTS(SELECT 1 FROM favorite_meals WHERE meal_id = :mealId AND user_id = :userId)")
    Single<Boolean> isFavorite(String mealId, String userId);
}
