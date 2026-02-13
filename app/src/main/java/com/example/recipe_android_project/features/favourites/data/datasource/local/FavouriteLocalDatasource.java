package com.example.recipe_android_project.features.favourites.data.datasource.local;

import android.content.Context;

import com.example.recipe_android_project.core.config.DbManager;
import com.example.recipe_android_project.features.home.data.entities.FavoriteMealEntity;
import com.example.recipe_android_project.features.home.data.mapper.MealMapper;
import com.example.recipe_android_project.features.home.model.Meal;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;

public class FavouriteLocalDatasource {
    private final FavouriteDao favouriteDao;
    public FavouriteLocalDatasource(Context context) {
        this.favouriteDao = DbManager.getInstance(context).favouriteDao();
    }
    public Flowable<List<Meal>> getFavorites(String userId) {
        return favouriteDao.getAllFavoritesByUserId(userId)
                .map(MealMapper::toDomainListFromEntities);
    }
    public Completable removeFromFavorites(String mealId, String userId) {
        return favouriteDao.deleteFavoriteByMealIdAndUserId(mealId, userId);
    }
    public Completable addToFavorites(FavoriteMealEntity favoriteMealEntity) {
        return favouriteDao.insertFavorite(favoriteMealEntity);
    }
}
