package com.example.recipe_android_project.features.auth.presenter;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Patterns;

import com.example.recipe_android_project.core.config.ResultCallback;
import com.example.recipe_android_project.features.auth.contract.LoginContract;
import com.example.recipe_android_project.features.auth.data.repository.AuthRepository;
import com.example.recipe_android_project.features.auth.model.User;

public class LoginPresenter extends BasePresenter<LoginContract.View> implements LoginContract.Presenter {

    private final AuthRepository authRepository;
    private final Handler mainHandler;

    public LoginPresenter(Context context) {
        this.authRepository = new AuthRepository(context);
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void login(String email, String password) {
        if (!isViewAttached()) return;

        // Clear previous errors
        getView().clearErrors();

        // Validate inputs
        if (!validateInputs(email, password)) {
            return;
        }

        // Show loading
        getView().showLoading("Logging in…");

        authRepository.login(email, password, new ResultCallback<User>() {
            @Override
            public void onSuccess(User user) {
                mainHandler.post(() -> {
                    if (isViewAttached()) {
                        getView().hideLoading();
                        getView().showSuccessDialog(
                                "Welcome back, " + user.getFirstName() + "!",
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

    private boolean validateInputs(String email, String password) {
        boolean isValid = true;

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
    public boolean isLoggedIn() {
        return authRepository.isSessionLoggedIn();
    }

    @Override
    public void detachView() {
        super.detachView();
    }
}
