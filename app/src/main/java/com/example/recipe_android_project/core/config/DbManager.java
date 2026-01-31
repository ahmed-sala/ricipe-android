package com.example.recipe_android_project.core.config;


import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.recipe_android_project.features.auth.data.datasource.local.UserDao;
import com.example.recipe_android_project.features.auth.data.entities.UserEntity;


@Database(entities = {UserEntity.class}, version = 1)
public abstract class DbManager extends RoomDatabase {
    public abstract UserDao userDao();
    private static DbManager INSTANCE;

    public static DbManager getInstance(Context context) {
        if(INSTANCE==null){
            INSTANCE= Room.databaseBuilder(context, DbManager.class,
                    "user_db").build();
        }
        return INSTANCE;
    }
}
