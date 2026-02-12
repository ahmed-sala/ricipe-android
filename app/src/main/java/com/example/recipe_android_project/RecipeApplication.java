package com.example.recipe_android_project;

import android.app.Application;
import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;

import com.example.recipe_android_project.core.helper.LocaleHelper;
import com.example.recipe_android_project.core.helper.SyncManager;

import io.reactivex.rxjava3.plugins.RxJavaPlugins;

public class RecipeApplication extends Application {
    private SyncManager syncManager;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.applyLocale(base));
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setupRxJavaErrorHandler();
        AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_NO);
        try {
            syncManager = SyncManager.getInstance(this);
            syncManager.startListening();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupRxJavaErrorHandler() {
        RxJavaPlugins.setErrorHandler(throwable -> {
            if (throwable instanceof
                    io.reactivex.rxjava3.exceptions
                            .UndeliverableException) {
                throwable = throwable.getCause();
            }
        });
    }
}
