package com.example.recipe_android_project.features.auth.presentation.contract;

public interface LoginContract {

    interface View {
        // Loading
        void showLoading(String message);
        void hideLoading();

        // Field Errors
        void showEmailError(String message);
        void showPasswordError(String message);
        void clearErrors();

        // Dialogs
        void showErrorDialog(String message);
        void showSuccessDialog(String message, Runnable onContinue);

        // Snackbar
        void showErrorSnackbar(String message);
        void showSuccessSnackbar(String message);

        // Navigation
        void navigateToHome();
        void navigateToForgotPassword();
    }

    interface Presenter {
        void attachView(View view);
        void detachView();
        void login(String email, String password);
        boolean isLoggedIn();
    }
}
