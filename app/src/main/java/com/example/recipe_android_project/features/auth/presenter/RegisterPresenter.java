package com.example.recipe_android_project.features.auth.presenter;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Patterns;

import com.example.recipe_android_project.core.config.ResultCallback;
import com.example.recipe_android_project.features.auth.contract.RegisterContract;
import com.example.recipe_android_project.features.auth.data.repository.AuthRepository;
import com.example.recipe_android_project.features.auth.model.User;

public class RegisterPresenter extends BasePresenter<RegisterContract.View> implements RegisterContract.Presenter {

    private final AuthRepository authRepository;
    private final Handler mainHandler;

    public RegisterPresenter(Context context) {
        this.authRepository = new AuthRepository(context);
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void register(String fullName, String email, String password) {
        if (!isViewAttached()) return;

        // Clear previous errors
        getView().clearErrors();

        // Validate inputs
        if (!validateInputs(fullName, email, password)) {
            return;
        }

        // Show loading
        getView().showLoading("Creating account…");

        authRepository.register(fullName, email, password, new ResultCallback<User>() {
            @Override
            public void onSuccess(User user) {
                mainHandler.post(() -> {
                    if (isViewAttached()) {
                        getView().hideLoading();
                        getView().showSuccessDialog(
                                "Welcome to MealMate, " + user.getFirstName() + "!",
                                () -> getView().navigateToHome()
                        );
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                mainHandler.post(() -> {
                    if (isViewAttached()) {
                        getView().hideLoading();
                        getView().showErrorDialog(e.getMessage());
                    }
                });
            }
        });
    }

    private boolean validateInputs(String fullName, String email, String password) {
        boolean isValid = true;

        // Validate name
        if (fullName == null || fullName.trim().isEmpty()) {
            getView().showNameError("Full name is required");
            isValid = false;
        } else if (fullName.trim().length() < 2) {
            getView().showNameError("Name must be at least 2 characters");
            isValid = false;
        }

        // Validate email
        if (email == null || email.trim().isEmpty()) {
            getView().showEmailError("Email is required");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            getView().showEmailError("Please enter a valid email");
            isValid = false;
        }

        // Validate password
        if (password == null || password.isEmpty()) {
            getView().showPasswordError("Password is required");
            isValid = false;
        } else if (password.length() < 6) {
            getView().showPasswordError("Password must be at least 6 characters");
            isValid = false;
        }

        return isValid;
    }

    @Override
    public void checkEmailExists(String email) {
        if (!isViewAttached()) return;

        if (email == null || email.trim().isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return;
        }

        authRepository.isEmailExists(email, new ResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean exists) {
                mainHandler.post(() -> {
                    if (isViewAttached() && exists) {
                        getView().showEmailError("This email is already registered");
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                // Ignore
            }
        });
    }

    @Override
    public void detachView() {
        super.detachView();
    }
}
