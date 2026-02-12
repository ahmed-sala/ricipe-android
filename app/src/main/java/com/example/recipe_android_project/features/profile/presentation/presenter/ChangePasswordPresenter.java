package com.example.recipe_android_project.features.profile.presentation.presenter;

import android.content.Context;

import com.example.recipe_android_project.features.profile.data.repository.ProfileRepository;
import com.example.recipe_android_project.features.profile.presentation.contract.ChangePasswordContract;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ChangePasswordPresenter implements ChangePasswordContract.Presenter {

    private ChangePasswordContract.View view;
    private final ProfileRepository profileRepository;
    private final CompositeDisposable disposables;

    public ChangePasswordPresenter(Context context) {
        this.profileRepository = new ProfileRepository(context);
        this.disposables = new CompositeDisposable();
    }

    @Override
    public void attachView(ChangePasswordContract.View view) {
        this.view = view;
    }

    @Override
    public void detachView() {
        this.view = null;
        disposables.clear();
    }

    private boolean isViewAttached() {
        return view != null;
    }

    @Override
    public void validatePasswords(String oldPassword, String newPassword, String confirmPassword) {
        if (!isViewAttached()) return;

        view.clearErrors();
        boolean isValid = true;

        if (oldPassword == null || oldPassword.trim().isEmpty()) {
            isValid = false;
        }

        if (newPassword == null || newPassword.trim().isEmpty()) {
            isValid = false;
        } else if (newPassword.length() < 8) {
            isValid = false;
        }

        if (confirmPassword == null || confirmPassword.trim().isEmpty()) {
            isValid = false;
        } else if (!confirmPassword.equals(newPassword)) {
            isValid = false;
        }

        view.setChangeButtonEnabled(isValid);
    }

    @Override
    public void changePassword(String oldPassword, String newPassword, String confirmPassword) {
        if (!isViewAttached()) return;

        view.clearErrors();
        if (!validateInputs(oldPassword, newPassword, confirmPassword)) {
            return;
        }

        view.showLoading();

        String userId = profileRepository.getCurrentUserId();
        if (userId == null) {
            view.hideLoading();
            view.showError("User not found. Please login again.");
            return;
        }

        Disposable disposable = profileRepository.changePassword(userId, oldPassword, newPassword)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> {
                            if (isViewAttached()) {
                                view.hideLoading();
                                if (result.isSuccess()) {
                                    if (result.isPendingSync()) {
                                        view.showOfflineMessage();
                                    }
                                    view.showSuccess("Password changed successfully");
                                } else {
                                    view.showError(result.getMessage());
                                }
                            }
                        },
                        throwable -> {
                            if (isViewAttached()) {
                                view.hideLoading();
                                String errorMessage = throwable.getMessage();

                                if (errorMessage != null
                                        && errorMessage.toLowerCase().contains("current password")) {
                                    view.showOldPasswordError(errorMessage);
                                } else {
                                    view.showError(errorMessage != null
                                            ? errorMessage
                                            : "Failed to change password");
                                }
                            }
                        }
                );

        disposables.add(disposable);
    }

    private boolean validateInputs(String oldPassword, String newPassword, String confirmPassword) {
        boolean isValid = true;

        if (oldPassword == null || oldPassword.trim().isEmpty()) {
            view.showOldPasswordError("Please enter your current password");
            isValid = false;
        }

        if (newPassword == null || newPassword.trim().isEmpty()) {
            view.showNewPasswordError("Please enter a new password");
            isValid = false;
        } else if (newPassword.length() < 8) {
            view.showNewPasswordError("Password must be at least 8 characters");
            isValid = false;
        } else if (!isPasswordStrong(newPassword)) {
            view.showNewPasswordError(
                    "Password must contain uppercase, lowercase, number and special character");
            isValid = false;
        } else if (newPassword.equals(oldPassword)) {
            view.showNewPasswordError("New password must be different from current password");
            isValid = false;
        }

        if (confirmPassword == null || confirmPassword.trim().isEmpty()) {
            view.showConfirmPasswordError("Please confirm your new password");
            isValid = false;
        } else if (!confirmPassword.equals(newPassword)) {
            view.showConfirmPasswordError("Passwords do not match");
            isValid = false;
        }

        return isValid;
    }

    private boolean isPasswordStrong(String password) {
        String pattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
        return password.matches(pattern);
    }
}
