package com.example.recipe_android_project.core.config;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.recipe_android_project.features.auth.data.datasource.local.UserDao;
import com.example.recipe_android_project.features.auth.data.entities.UserEntity;
import com.example.recipe_android_project.features.dashboard.data.datasource.local.DashboardDao;
import com.example.recipe_android_project.features.favourites.data.datasource.local.FavouriteDao;
import com.example.recipe_android_project.features.home.data.datasource.local.MealDao;
import com.example.recipe_android_project.features.home.data.entities.FavoriteMealEntity;
import com.example.recipe_android_project.features.plan.data.datasource.local.MealPlanDao;
import com.example.recipe_android_project.features.plan.data.entity.MealPlanEntity;
import com.example.recipe_android_project.features.profile.data.datasource.local.ProfileDao;

@Database(
        entities = {
                UserEntity.class,
                FavoriteMealEntity.class,
                MealPlanEntity.class
        },
        version = 6,
        exportSchema = false
)
public abstract class DbManager extends RoomDatabase {

    public abstract UserDao userDao();
    public abstract MealDao favoriteMealDao();
    public abstract FavouriteDao favouriteDao();
    public abstract DashboardDao dashboardDao();
    public abstract MealPlanDao mealPlanDao();
    public abstract ProfileDao profileDao();

    private static volatile DbManager INSTANCE;
    private static final String DATABASE_NAME = "recipe_app_db";



    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL(
                    "ALTER TABLE users ADD COLUMN pending_sync INTEGER NOT NULL DEFAULT 0"
            );
            database.execSQL(
                    "ALTER TABLE users ADD COLUMN pending_sync_action TEXT"
            );
            database.execSQL(
                    "ALTER TABLE users ADD COLUMN last_synced_at INTEGER NOT NULL DEFAULT 0"
            );
        }
    };


    static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL(
                    "ALTER TABLE users ADD COLUMN pending_password_sync INTEGER NOT NULL DEFAULT 0"
            );
            database.execSQL(
                    "ALTER TABLE users ADD COLUMN pending_new_password TEXT"
            );

            database.execSQL(
                    "ALTER TABLE users ADD COLUMN pending_registration_sync INTEGER NOT NULL DEFAULT 0"
            );
            database.execSQL(
                    "ALTER TABLE users ADD COLUMN pending_plain_password TEXT"
            );
        }
    };


    static final Migration MIGRATION_3_5 = new Migration(3, 5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL(
                    "ALTER TABLE users ADD COLUMN pending_sync INTEGER NOT NULL DEFAULT 0"
            );
            database.execSQL(
                    "ALTER TABLE users ADD COLUMN pending_sync_action TEXT"
            );
            database.execSQL(
                    "ALTER TABLE users ADD COLUMN last_synced_at INTEGER NOT NULL DEFAULT 0"
            );

            database.execSQL(
                    "ALTER TABLE users ADD COLUMN pending_password_sync INTEGER NOT NULL DEFAULT 0"
            );
            database.execSQL(
                    "ALTER TABLE users ADD COLUMN pending_new_password TEXT"
            );

            database.execSQL(
                    "ALTER TABLE users ADD COLUMN pending_registration_sync INTEGER NOT NULL DEFAULT 0"
            );
            database.execSQL(
                    "ALTER TABLE users ADD COLUMN pending_plain_password TEXT"
            );
        }
    };


    public static DbManager getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (DbManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    DbManager.class,
                                    DATABASE_NAME
                            )
                            .addMigrations(
                                    MIGRATION_3_4,
                                    MIGRATION_4_5,
                                    MIGRATION_3_5
                            )
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public static void closeDatabase() {
        if (INSTANCE != null) {
            synchronized (DbManager.class) {
                if (INSTANCE != null && INSTANCE.isOpen()) {
                    INSTANCE.close();
                    INSTANCE = null;
                }
            }
        }
    }


    public void clearAllTables() {
        if (INSTANCE != null) {
            INSTANCE.clearAllTables();
        }
    }
}
