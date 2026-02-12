package com.example.recipe_android_project.features.plan.data.datasource.local;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;


import com.example.recipe_android_project.features.plan.data.entity.MealPlanEntity;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

@Dao
public interface MealPlanDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertMealPlan(MealPlanEntity mealPlan);
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertAllMealPlans(List<MealPlanEntity> mealPlans);
    @Query("UPDATE meal_plans SET is_synced = :isSynced WHERE user_id = :userId AND date = :date AND meal_type = :mealType")
    Completable updateSyncStatus(String userId, String date, String mealType, boolean isSynced);
    @Query("DELETE FROM meal_plans WHERE user_id = :userId AND date = :date AND meal_type = :mealType")
    Completable deleteMealPlanByKey(String userId, String date, String mealType);
    @Query("SELECT * FROM meal_plans WHERE user_id = :userId AND date = :date AND meal_type = :mealType LIMIT 1")
    Single<MealPlanEntity> getMealPlan(String userId, String date, String mealType);
    @Query("SELECT * FROM meal_plans WHERE user_id = :userId AND date = :date ORDER BY CASE meal_type WHEN 'breakfast' THEN 1 WHEN 'lunch' THEN 2 WHEN 'dinner' THEN 3 END")
    Flowable<List<MealPlanEntity>> observeMealPlansByDate(String userId, String date);
    @Query("SELECT * FROM meal_plans WHERE meal_id = :mealId AND user_id = :userId LIMIT 1")
    Single<MealPlanEntity> getMealPlanByMealId(String mealId, String userId);

}
