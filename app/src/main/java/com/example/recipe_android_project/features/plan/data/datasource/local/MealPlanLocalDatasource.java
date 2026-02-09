package com.example.recipe_android_project.features.plan.data.datasource.local;


import android.content.Context;

import com.example.recipe_android_project.core.config.DbManager;
import com.example.recipe_android_project.features.plan.data.entity.MealPlanEntity;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

public class MealPlanLocalDatasource {

    private final MealPlanDao mealPlanDao;

    public MealPlanLocalDatasource(Context context) {
        this.mealPlanDao = DbManager.getInstance(context).mealPlanDao();
    }
    public Completable removeMealPlan(String userId, String date, String mealType) {
        return mealPlanDao.deleteMealPlanByKey(userId, date, mealType);
    }
    public Flowable<List<MealPlanEntity>> observeMealPlansByDate(String userId, String date) {
        return mealPlanDao.observeMealPlansByDate(userId, date);
    }
}
