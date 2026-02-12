package com.example.recipe_android_project.features.search.presentation.contract;

import com.example.recipe_android_project.features.search.domain.model.FilterResult;

import java.util.List;

public interface FilterResultContract {

    interface View {
        void showLoading();
        void hideLoading();

        void showFilterResults(List<FilterResult> results);
        void showEmptyState(String message);
        void hideEmptyState();
        void showError(String message);

        void navigateBack();
        void navigateToMealDetail(String mealId);

        void onFavoriteAdded(FilterResult filterResult);
        void onFavoriteRemoved(FilterResult filterResult);
        void onFavoriteError(String message);
        void updateFilterResultFavoriteStatus(int mealId, boolean isFavorite);
        void showLoginRequired();

        void showNoInternet();
        void hideNoInternet();
    }

    interface Presenter {
        void attachView(View view);
        void detachView();

        void loadFilterResults(String filterType, String filterValue);
        void onMealClicked(FilterResult filterResult);

        void addToFavorites(FilterResult filterResult);
        void removeFromFavorites(FilterResult filterResult);
        boolean isUserLoggedIn();
        void dispose();
    }
}
