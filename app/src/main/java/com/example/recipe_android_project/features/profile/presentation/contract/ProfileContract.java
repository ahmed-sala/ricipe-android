package com.example.recipe_android_project.features.profile.presentation.contract;

import com.example.recipe_android_project.features.auth.domain.model.User;

public interface ProfileContract {

    interface View {
        void showLoading();
        void hideLoading();
        void showUserData(User user);
        void showError(String message);
        void navigateToLogin();
        void showLogoutSuccess();
        void showLanguageChanged(String languageCode);
    }

    interface Presenter {
        void attachView(View view);
        void detachView();
        void loadUserData();
        void logout();
        void changeLanguage(String languageCode);
    }
}
