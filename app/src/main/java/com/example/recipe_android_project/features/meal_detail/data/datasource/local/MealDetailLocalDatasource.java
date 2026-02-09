package com.example.recipe_android_project.features.meal_detail.data.datasource.local;

import android.content.Context;

import com.example.recipe_android_project.core.config.DbManager;
import com.example.recipe_android_project.features.home.data.datasource.local.MealDao;
import com.example.recipe_android_project.features.home.data.entities.FavoriteMealEntity;
import com.example.recipe_android_project.features.plan.data.datasource.local.MealPlanDao;
import com.example.recipe_android_project.features.plan.data.entity.MealPlanEntity;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

public class MealDetailLocalDatasource {
    private final MealDao mealDao;
    private final MealPlanDao mealPlanDao;
    public MealDetailLocalDatasource(Context context) {
        DbManager dbManager = DbManager.getInstance(context);
        this.mealDao = dbManager.favoriteMealDao();
        this.mealPlanDao = dbManager.mealPlanDao();
    }
    public Completable addToFavorites(FavoriteMealEntity entity) {
        return mealDao.insertFavorite(entity);
    }

    public Completable removeFromFavorites(String mealId, String userId) {
        return mealDao.deleteFavoriteByMealIdAndUserId(mealId, userId);
    }

    public Single<Boolean> isFavorite(String mealId, String userId) {
        return mealDao.isFavorite(mealId, userId);
    }
    public Completable addMealPlan(MealPlanEntity mealPlan) {
        return mealPlanDao.insertMealPlan(mealPlan);
    }
    public Completable removeMealPlan(String userId, String date, String mealType) {
        return mealPlanDao.deleteMealPlanByKey(userId, date, mealType);
    }
    public Single<MealPlanEntity> getMealPlan(String userId, String date, String mealType) {
        return mealPlanDao.getMealPlan(userId, date, mealType);
    }
    public Completable updateMealPlanSyncStatus(String userId, String date, String mealType, boolean isSynced) {
        return mealPlanDao.updateSyncStatus(userId, date, mealType, isSynced);
    }
}
