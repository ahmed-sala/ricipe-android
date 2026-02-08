package com.example.recipe_android_project.features.home.data.datasource.local;

import android.content.Context;

import com.example.recipe_android_project.core.config.DbManager;
import com.example.recipe_android_project.features.home.data.entities.FavoriteMealEntity;
import com.example.recipe_android_project.features.home.data.mapper.MealMapper;
import com.example.recipe_android_project.features.home.model.Meal;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

public class HomeLocalDatasource {

    private final MealDao mealDao;

    public HomeLocalDatasource(Context context) {
        DbManager dbManager = DbManager.getInstance(context);
        this.mealDao = dbManager.favoriteMealDao();
    }
    public Completable addToFavorites(FavoriteMealEntity entity) {
        return mealDao.insertFavorite(entity);
    }

    public Completable addToFavorites(Meal meal, String userId) {
        FavoriteMealEntity entity = MealMapper.toEntity(meal, userId);
        if (entity == null) {
            return Completable.error(new IllegalArgumentException("Invalid meal or userId"));
        }
        return mealDao.insertFavorite(entity);
    }
    public Completable removeFromFavorites(FavoriteMealEntity entity) {
        return mealDao.deleteFavorite(entity);
    }

    public Completable removeFromFavorites(String mealId, String userId) {
        return mealDao.deleteFavoriteByMealIdAndUserId(mealId, userId);
    }
    public Single<List<FavoriteMealEntity>> getFavoriteEntitiesSingle(String userId) {
        return mealDao.getAllFavoritesByUserIdSingle(userId);
    }
    public Flowable<List<Meal>> getFavorites(String userId) {
        return mealDao.getAllFavoritesByUserId(userId)
                .map(MealMapper::toDomainListFromEntities);
    }
    public Single<Boolean> isFavorite(String mealId, String userId) {
        return mealDao.isFavorite(mealId, userId);
    }
}
