package com.example.recipe_android_project.features.search.presentation.contract;

import com.example.recipe_android_project.features.home.model.Area;
import com.example.recipe_android_project.features.home.model.Meal;
import com.example.recipe_android_project.features.search.domain.model.FilterParams;
import com.example.recipe_android_project.features.search.domain.model.Ingredient;

import java.util.List;

public interface SearchContract {

    interface View {
        void showLoading();
        void hideLoading();
        void showMeals(List<Meal> meals);
        void showIngredients(List<Ingredient> ingredients);
        void showAreas(List<Area> areas);
        void clearMeals();
        void showEmptyMeals();
        void showEmptyIngredients();
        void showEmptyAreas();
        void showError(String message);
        void showSearchPlaceholder();
        void hideSearchPlaceholder();
        void hideEmptyState();
        void navigateToFilterResult(FilterParams filterParams);
        void navigateToMealDetail(String mealId);
        void onFavoriteAdded(Meal meal);
        void onFavoriteRemoved(Meal meal);
        void onFavoriteError(String message);
        void updateMealFavoriteStatus(Meal meal, boolean isFavorite);
        void showLoginRequired();
    }

    interface Presenter {
        void attachView(View view);
        void detachView();
        void onSearchQueryChanged(String query);
        void onTabChanged(int tabIndex);
        void loadInitialData();
        void onMealClicked(Meal meal);
        void onIngredientClicked(Ingredient ingredient);
        void onAreaClicked(Area area);
        void addToFavorites(Meal meal);
        void removeFromFavorites(Meal meal);
        boolean isUserLoggedIn();
        void dispose();
    }
}
