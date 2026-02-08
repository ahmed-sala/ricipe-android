package com.example.recipe_android_project.features.dashboard.presentation.contract;

public interface DashboardContract {

    interface View {
        void showFavoritesBadge(int count);
        void hideFavoritesBadge();
        void updateBadgeCount(int count);
    }

    interface Presenter {
        void attachView(View view);
        void detachView();
        void observeFavoritesCount();
        void onFavoritesTabSelected();
        void onOtherTabSelected();
        boolean isOnFavoritesTab();
    }
}
