package com.example.recipe_android_project.features.profile.presentation.contract;

public interface ChangePasswordContract {

    interface View {
        void showLoading();
        void hideLoading();
        void showOldPasswordError(String error);
        void showNewPasswordError(String error);
        void showConfirmPasswordError(String error);
        void clearErrors();
        void showSuccess(String message);
        void showError(String message);
        void showOfflineMessage();
        void navigateBack();
        void setChangeButtonEnabled(boolean enabled);
    }

    interface Presenter {
        void attachView(View view);
        void detachView();
        void changePassword(String oldPassword, String newPassword, String confirmPassword);
        void validatePasswords(String oldPassword, String newPassword, String confirmPassword);
    }
}
