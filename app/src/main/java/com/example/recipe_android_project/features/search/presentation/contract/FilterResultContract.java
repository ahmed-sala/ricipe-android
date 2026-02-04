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
        void setToolbarTitle(String title);
        void navigateBack();
        void navigateToMealDetail(int mealId);
    }

    interface Presenter {
        void attachView(View view);
        void detachView();
        void loadFilterResults(String filterType, String filterValue);
        void onMealClicked(FilterResult filterResult);
        void onFavoriteClicked(FilterResult filterResult, boolean isFavorite);
        void onBackPressed();
        void dispose();
    }
}
