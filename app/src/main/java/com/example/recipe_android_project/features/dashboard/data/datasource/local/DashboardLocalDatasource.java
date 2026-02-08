package com.example.recipe_android_project.features.dashboard.data.datasource.local;

import android.content.Context;

import com.example.recipe_android_project.core.config.DbManager;

import io.reactivex.rxjava3.core.Flowable;

public class DashboardLocalDatasource {
    private final DashboardDao dashboardDao;

    public DashboardLocalDatasource(Context context) {
        this.dashboardDao = DbManager.getInstance(context).dashboardDao();
    }

    public Flowable<Integer> getFavouritesCount(String userId) {
        return dashboardDao.getFavoritesCountFlowable(userId);
    }
}
