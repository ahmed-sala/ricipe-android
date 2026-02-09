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
    @Update
    Completable updateMealPlan(MealPlanEntity mealPlan);
    @Query("UPDATE meal_plans SET is_synced = :isSynced WHERE user_id = :userId AND date = :date AND meal_type = :mealType")
    Completable updateSyncStatus(String userId, String date, String mealType, boolean isSynced);
    @Query("DELETE FROM meal_plans WHERE user_id = :userId AND date = :date AND meal_type = :mealType")
    Completable deleteMealPlanByKey(String userId, String date, String mealType);

    @Query("DELETE FROM meal_plans WHERE user_id = :userId AND date = :date")
    Completable deleteMealPlansByDate(String userId, String date);

    @Query("DELETE FROM meal_plans WHERE user_id = :userId")
    Completable deleteAllMealPlansByUser(String userId);

    @Query("SELECT * FROM meal_plans WHERE user_id = :userId AND date = :date AND meal_type = :mealType LIMIT 1")
    Single<MealPlanEntity> getMealPlan(String userId, String date, String mealType);
    @Query("SELECT * FROM meal_plans WHERE user_id = :userId AND date = :date ORDER BY CASE meal_type WHEN 'breakfast' THEN 1 WHEN 'lunch' THEN 2 WHEN 'dinner' THEN 3 END")
    Single<List<MealPlanEntity>> getMealPlansByDate(String userId, String date);
    @Query("SELECT * FROM meal_plans WHERE user_id = :userId AND date = :date ORDER BY CASE meal_type WHEN 'breakfast' THEN 1 WHEN 'lunch' THEN 2 WHEN 'dinner' THEN 3 END")
    Flowable<List<MealPlanEntity>> observeMealPlansByDate(String userId, String date);
    @Query("SELECT * FROM meal_plans WHERE user_id = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date ASC, CASE meal_type WHEN 'breakfast' THEN 1 WHEN 'lunch' THEN 2 WHEN 'dinner' THEN 3 END")
    Single<List<MealPlanEntity>> getMealPlansByDateRange(String userId, String startDate, String endDate);

    @Query("SELECT * FROM meal_plans WHERE user_id = :userId ORDER BY date DESC, CASE meal_type WHEN 'breakfast' THEN 1 WHEN 'lunch' THEN 2 WHEN 'dinner' THEN 3 END")
    Single<List<MealPlanEntity>> getAllMealPlansByUser(String userId);

    @Query("SELECT EXISTS(SELECT 1 FROM meal_plans WHERE user_id = :userId AND date = :date AND meal_type = :mealType)")
    Single<Boolean> mealPlanExists(String userId, String date, String mealType);

    @Query("SELECT * FROM meal_plans WHERE user_id = :userId AND is_synced = 0")
    Single<List<MealPlanEntity>> getUnsyncedMealPlans(String userId);

    @Query("SELECT COUNT(*) FROM meal_plans WHERE user_id = :userId AND date = :date")
    Single<Integer> countMealPlansForDate(String userId, String date);
}
