package com.example.recipe_android_project.features.profile.presentation.contract;


import com.example.recipe_android_project.features.auth.domain.model.ProfileData;

public interface EditProfileContract {

    interface View {
        void showLoading(String message);
        void hideLoading();
        void displayUserData(String fullName, String email);
        void showFullNameError(String message);
        void showEmailError(String message);
        void clearFullNameError();
        void clearEmailError();
        void clearErrors();
        void setSaveButtonEnabled(boolean enabled);
        void showErrorDialog(String message);
        void showSuccessDialog(String message, Runnable onDismiss);
        void showErrorSnackbar(String message);
        void showSuccessSnackbar(String message);
        void showOfflineUpdateSnackbar();
        void navigateBack();
        void showDiscardChangesDialog(Runnable onDiscard);
    }

    interface Presenter {
        void attachView(View view);
        void detachView();
        void initWithProfileData(ProfileData profileData);
        void onFullNameChanged(String fullName);
        void onEmailChanged(String email);
        void validateFullName(String fullName);
        void validateEmail(String email);
        boolean hasUnsavedChanges(String currentFullName, String currentEmail);
        void onBackPressed(String currentFullName, String currentEmail);
        void saveProfile(String fullName, String email);
        void onDestroy();
    }
}
