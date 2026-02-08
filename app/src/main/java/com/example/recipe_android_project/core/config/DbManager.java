package com.example.recipe_android_project.core.config;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.recipe_android_project.features.auth.data.datasource.local.UserDao;
import com.example.recipe_android_project.features.auth.data.entities.UserEntity;
import com.example.recipe_android_project.features.dashboard.data.datasource.local.DashboardDao;
import com.example.recipe_android_project.features.favourites.data.datasource.local.FavouriteDao;
import com.example.recipe_android_project.features.home.data.datasource.local.MealDao;
import com.example.recipe_android_project.features.home.data.entities.FavoriteMealEntity;

@Database(
        entities = {
                UserEntity.class,
                FavoriteMealEntity.class
        },
        version = 2,
        exportSchema = false
)
public abstract class DbManager extends RoomDatabase {

    public abstract UserDao userDao();
    public abstract MealDao favoriteMealDao();
    public abstract FavouriteDao favouriteDao();
    public abstract DashboardDao dashboardDao();

    private static volatile DbManager INSTANCE;
    private static final String DATABASE_NAME = "recipe_app_db";

    public static DbManager getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (DbManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    DbManager.class,
                                    DATABASE_NAME
                            )
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
