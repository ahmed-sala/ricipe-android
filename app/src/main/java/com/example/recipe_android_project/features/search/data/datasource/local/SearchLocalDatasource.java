package com.example.recipe_android_project.features.search.data.datasource.local;

import android.content.Context;

import com.example.recipe_android_project.core.config.DbManager;
import com.example.recipe_android_project.features.home.data.datasource.local.MealDao;
import com.example.recipe_android_project.features.home.data.entities.FavoriteMealEntity;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public class SearchLocalDatasource {
    private final MealDao mealDao;
    public SearchLocalDatasource(Context context){
        this.mealDao = DbManager.getInstance(context).favoriteMealDao();
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
    public Single<List<FavoriteMealEntity>> getFavoriteEntitiesSingle(String userId) {
        return mealDao.getAllFavoritesByUserIdSingle(userId);
    }
}
