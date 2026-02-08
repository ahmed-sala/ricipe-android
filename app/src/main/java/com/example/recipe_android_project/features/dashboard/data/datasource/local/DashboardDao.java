package com.example.recipe_android_project.features.dashboard.data.datasource.local;

import androidx.room.Dao;
import androidx.room.Query;

import io.reactivex.rxjava3.core.Flowable;

@Dao
public interface DashboardDao {
    @Query("SELECT COUNT(*) FROM favorite_meals WHERE user_id = :userId")
    Flowable<Integer> getFavoritesCountFlowable(String userId);
}
