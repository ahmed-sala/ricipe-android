package com.example.recipe_android_project.features.auth.presentation.contract;

public interface RegisterContract {

    interface View {
        void showLoading(String message);
        void hideLoading();

        void showNameError(String message);
        void showEmailError(String message);
        void showPasswordError(String message);
        void clearErrors();

        void showErrorDialog(String message);
        void showSuccessDialog(String message, Runnable onContinue);

        void showErrorSnackbar(String message);
        void showSuccessSnackbar(String message);

        void navigateToHome();
        void navigateToLogin();
    }

    interface Presenter {
        void attachView(View view);
        void detachView();
        void register(String fullName, String email, String password);
        void checkEmailExists(String email);
    }
}
