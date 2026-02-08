package com.example.recipe_android_project.features.favourites.presentation.contract;

import com.example.recipe_android_project.features.home.model.Meal;

import java.util.List;

public interface FavouriteContract {

    interface View {
        void showLoading();
        void hideLoading();
        void showFavorites(List<Meal> meals);
        void showEmptyState();
        void hideEmptyState();
        void showError(String message);
        void onFavoriteRestored(Meal meal);
        void onRemoveError(String message);
        void showRemoveConfirmDialog(Meal meal);
        void showUndoSnackbar(Meal meal);
        void showSearchEmpty(String query);
    }

    interface Presenter {
        void loadFavorites();
        void onSearchQueryChanged(String query);
        void removeFromFavorites(Meal meal);
        void confirmRemoveFavorite(Meal meal);
        void undoRemove();
        void detach();
    }
}
