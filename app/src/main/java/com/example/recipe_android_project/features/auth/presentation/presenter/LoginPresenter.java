package com.example.recipe_android_project.features.auth.presentation.presenter;

import android.content.Context;
import android.util.Patterns;

import com.example.recipe_android_project.features.auth.data.repository.AuthRepository;
import com.example.recipe_android_project.features.auth.presentation.contract.LoginContract;

import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.PublishSubject;

public class LoginPresenter implements LoginContract.Presenter {

    private static final long VALIDATION_DEBOUNCE_MS = 300L;

    private WeakReference<LoginContract.View> viewRef;
    private final AuthRepository authRepository;
    private final CompositeDisposable disposables;

    private final PublishSubject<String> emailSubject = PublishSubject.create();
    private final PublishSubject<String> passwordSubject = PublishSubject.create();

    private boolean isEmailValid = false;
    private boolean isPasswordValid = false;

    public LoginPresenter(Context context) {
        this.authRepository = new AuthRepository(context);
        this.disposables = new CompositeDisposable();
        setupValidationStreams();
    }

    private void setupValidationStreams() {
        Disposable emailDisposable = emailSubject
                .debounce(VALIDATION_DEBOUNCE_MS, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::performEmailValidation, Throwable::printStackTrace);

        Disposable passwordDisposable = passwordSubject
                .debounce(VALIDATION_DEBOUNCE_MS, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::performPasswordValidation, Throwable::printStackTrace);

        disposables.add(emailDisposable);
        disposables.add(passwordDisposable);
    }

    @Override
    public void attachView(LoginContract.View view) {
        this.viewRef = new WeakReference<>(view);
    }

    @Override
    public void detachView() {
        if (viewRef != null) {
            viewRef.clear();
            viewRef = null;
        }
    }

    private LoginContract.View getView() {
        return viewRef != null ? viewRef.get() : null;
    }

    private boolean isViewAttached() {
        return viewRef != null && viewRef.get() != null;
    }

    @Override
    public void onEmailChanged(String email) {
        emailSubject.onNext(email != null ? email : "");
    }

    @Override
    public void onPasswordChanged(String password) {
        passwordSubject.onNext(password != null ? password : "");
    }

    private void performEmailValidation(String email) {
        if (!isViewAttached()) return;

        LoginContract.View view = getView();
        String trimmedEmail = email.trim();

        if (trimmedEmail.isEmpty()) {
            isEmailValid = false;
            view.clearEmailError();
        } else if (!Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            isEmailValid = false;
            view.showEmailError("Please enter a valid email");
        } else {
            isEmailValid = true;
            view.clearEmailError();
        }

        updateLoginButtonState();
    }

    private void performPasswordValidation(String password) {
        if (!isViewAttached()) return;

        LoginContract.View view = getView();

        if (password.isEmpty()) {
            isPasswordValid = false;
            view.clearPasswordError();
        } else if (password.length() < 6) {
            isPasswordValid = false;
            view.showPasswordError("Password must be at least 6 characters");
        } else {
            isPasswordValid = true;
            view.clearPasswordError();
        }

        updateLoginButtonState();
    }

    @Override
    public void validateEmail(String email) {
        performEmailValidation(email);
    }

    @Override
    public void validatePassword(String password) {
        performPasswordValidation(password);
    }

    private void updateLoginButtonState() {
        if (isViewAttached()) {
            getView().setLoginButtonEnabled(isEmailValid && isPasswordValid);
        }
    }

    private boolean validateInputs(String email, String password) {
        boolean isValid = true;
        LoginContract.View view = getView();

        if (email == null || email.trim().isEmpty()) {
            view.showEmailError("Email is required");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            view.showEmailError("Please enter a valid email");
            isValid = false;
        }

        if (password == null || password.isEmpty()) {
            view.showPasswordError("Password is required");
            isValid = false;
        } else if (password.length() < 6) {
            view.showPasswordError("Password must be at least 6 characters");
            isValid = false;
        }

        return isValid;
    }

    @Override
    public void login(String email, String password) {
        if (!isViewAttached()) return;

        LoginContract.View view = getView();
        view.clearErrors();

        if (!validateInputs(email, password)) {
            return;
        }

        view.showLoading("Logging in…");

        Disposable disposable = authRepository.login(email.trim(), password)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        user -> {
                            if (isViewAttached()) {
                                getView().hideLoading();
                                getView().showSuccessDialog(
                                        "Welcome back, " + user.getFirstName() + "!",
                                        () -> getView().navigateToHome()
                                );
                            }
                        },
                        error -> {
                            if (isViewAttached()) {
                                getView().hideLoading();
                                getView().showErrorDialog(error.getMessage());
                            }
                        }
                );

        disposables.add(disposable);
    }

    @Override
    public boolean isLoggedIn() {
        return authRepository.isSessionLoggedIn();
    }

    @Override
    public void onDestroy() {
        disposables.clear();
        detachView();
    }
}
