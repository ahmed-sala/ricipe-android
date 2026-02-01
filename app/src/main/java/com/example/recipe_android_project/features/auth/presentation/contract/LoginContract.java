package com.example.recipe_android_project.features.auth.presentation.contract;

public interface LoginContract {

    interface View {
        void showLoading(String message);
        void hideLoading();

        void showEmailError(String message);
        void showPasswordError(String message);
        void clearErrors();

        void showErrorDialog(String message);
        void showSuccessDialog(String message, Runnable onContinue);

        void showErrorSnackbar(String message);
        void showSuccessSnackbar(String message);

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
