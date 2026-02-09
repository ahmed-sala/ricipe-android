package com.example.recipe_android_project.features.plan.data.repository;

import android.content.Context;

import com.example.recipe_android_project.features.meal_detail.domain.model.MealPlan;
import com.example.recipe_android_project.features.plan.data.datasource.local.MealPlanLocalDatasource;
import com.example.recipe_android_project.features.plan.data.datasource.remote.MealPlanRemoteDatasource;
import com.example.recipe_android_project.features.plan.data.mapper.MealPlanMapper;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MealPlanRepository {
    private final MealPlanLocalDatasource localDatasource;
    private final MealPlanRemoteDatasource remoteDatasource;
    public MealPlanRepository(Context context) {
        this.localDatasource = new MealPlanLocalDatasource(context);
        this.remoteDatasource = new MealPlanRemoteDatasource(context);
    }
    public Flowable<List<MealPlan>> getMealPlansByDate(String userId, String date) {
        if (userId == null || userId.isEmpty() || date == null || date.isEmpty()) {
            return Flowable.error(new IllegalArgumentException("UserId and Date are required"));
        }

        return localDatasource.observeMealPlansByDate(userId, date)
                .map(MealPlanMapper::toDomainList)
                .subscribeOn(Schedulers.io());
    }
    public Completable removeMealPlan(String userId, String date, String mealType) {
        if (userId == null || userId.isEmpty() || date == null || date.isEmpty() ||
                mealType == null || mealType.isEmpty()) {
            return Completable.error(new IllegalArgumentException("UserId, Date, and MealType are required"));
        }

        Completable localRemove = localDatasource.removeMealPlan(userId, date, mealType);

        if (remoteDatasource.isNetworkAvailable()) {
            return localRemove
                    .andThen(remoteDatasource.removeMealPlanFromFirestore(userId, date, mealType))
                    .subscribeOn(Schedulers.io());
        } else {
            return localRemove.subscribeOn(Schedulers.io());
        }
    }
    public boolean isNetworkAvailable() {
        return remoteDatasource.isNetworkAvailable();
    }
}
