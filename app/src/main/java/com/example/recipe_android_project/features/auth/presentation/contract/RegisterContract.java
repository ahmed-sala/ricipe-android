// features/auth/presentation/contract/RegisterContract.java
package com.example.recipe_android_project.features.auth.presentation.contract;

import com.example.recipe_android_project.core.utils.PasswordHasher.PasswordStrength;

public interface RegisterContract {

    interface View {
        void showLoading(String message);
        void hideLoading();
        void showNameError(String message);
        void showEmailError(String message);
        void showPasswordError(String message);
        void clearNameError();
        void clearEmailError();
        void clearPasswordError();
        void clearErrors();
        void setRegisterButtonEnabled(boolean enabled);
        void showPasswordStrengthIndicator(PasswordStrength strength, String message);
        void hidePasswordStrengthIndicator();
        void showErrorDialog(String message);
        void showSuccessDialog(String message, Runnable onContinue);
        void showErrorSnackbar(String message);
        void navigateToHome();
        void navigateToLogin();
    }

    interface Presenter {
        void attachView(View view);
        void detachView();
        void onNameChanged(String name);
        void onEmailChanged(String email);
        void onPasswordChanged(String password);
        void validateName(String name);
        void validateEmail(String email);
        void validatePassword(String password);
        void checkEmailExists(String email);
        void register(String fullName, String email, String password);
        void signInWithGoogle(String idToken);
        void onDestroy();
    }
}
