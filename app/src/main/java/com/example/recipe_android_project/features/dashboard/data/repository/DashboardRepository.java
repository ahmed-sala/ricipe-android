package com.example.recipe_android_project.features.dashboard.data.repository;

import android.content.Context;

import com.example.recipe_android_project.features.dashboard.data.datasource.local.DashboardLocalDatasource;

import io.reactivex.rxjava3.core.Flowable;

public class DashboardRepository {
    private final DashboardLocalDatasource dashboardLocalDatasource;

    public DashboardRepository(Context context) {
        this.dashboardLocalDatasource = new DashboardLocalDatasource(context);
    }

    public Flowable<Integer> getFavouritesCount(String userId) {
        return dashboardLocalDatasource.getFavouritesCount(userId);
    }
}
