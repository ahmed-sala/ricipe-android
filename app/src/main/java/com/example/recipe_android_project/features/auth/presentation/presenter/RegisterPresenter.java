package com.example.recipe_android_project.features.auth.presentation.presenter;

import android.content.Context;
import android.util.Patterns;

import com.example.recipe_android_project.core.utils.PasswordHasher;
import com.example.recipe_android_project.core.utils.PasswordHasher.PasswordStrength;
import com.example.recipe_android_project.core.utils.PasswordHasher.PasswordValidationResult;
import com.example.recipe_android_project.features.auth.data.repository.AuthRepository;
import com.example.recipe_android_project.features.auth.presentation.contract.RegisterContract;

import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;

public class RegisterPresenter implements RegisterContract.Presenter {

    private static final long VALIDATION_DEBOUNCE_MS = 300L;
    private static final long EMAIL_CHECK_DEBOUNCE_MS = 500L;

    private WeakReference<RegisterContract.View> viewRef;
    private final AuthRepository authRepository;
    private final CompositeDisposable disposables;

    private final PublishSubject<String> nameSubject = PublishSubject.create();
    private final PublishSubject<String> emailSubject = PublishSubject.create();
    private final PublishSubject<String> emailCheckSubject = PublishSubject.create();
    private final PublishSubject<String> passwordSubject = PublishSubject.create();

    private boolean isNameValid = false;
    private boolean isEmailValid = false;
    private boolean isPasswordValid = false;
    private boolean isEmailAvailable = true;

    public RegisterPresenter(Context context) {
        this.authRepository = new AuthRepository(context);
        this.disposables = new CompositeDisposable();
        setupValidationStreams();
    }

    private void setupValidationStreams() {
        Disposable nameDisposable = nameSubject
                .debounce(VALIDATION_DEBOUNCE_MS, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::performNameValidation, Throwable::printStackTrace);

        Disposable emailDisposable = emailSubject
                .debounce(VALIDATION_DEBOUNCE_MS, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::performEmailValidation, Throwable::printStackTrace);

        Disposable emailCheckDisposable = emailCheckSubject
                .debounce(EMAIL_CHECK_DEBOUNCE_MS, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .filter(this::isValidEmailFormat)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::performEmailExistsCheck, Throwable::printStackTrace);

        Disposable passwordDisposable = passwordSubject
                .debounce(VALIDATION_DEBOUNCE_MS, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::performPasswordValidation, Throwable::printStackTrace);

        disposables.add(nameDisposable);
        disposables.add(emailDisposable);
        disposables.add(emailCheckDisposable);
        disposables.add(passwordDisposable);
    }

    private boolean isValidEmailFormat(String email) {
        return email != null
                && email.length() > 5
                && email.contains("@")
                && email.contains(".")
                && Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches();
    }

    @Override
    public void attachView(RegisterContract.View view) {
        this.viewRef = new WeakReference<>(view);
    }

    @Override
    public void detachView() {
        if (viewRef != null) {
            viewRef.clear();
            viewRef = null;
        }
    }

    private RegisterContract.View getView() {
        return viewRef != null ? viewRef.get() : null;
    }

    private boolean isViewAttached() {
        return viewRef != null && viewRef.get() != null;
    }

    @Override
    public void onNameChanged(String name) {
        nameSubject.onNext(name != null ? name : "");
    }

    @Override
    public void onEmailChanged(String email) {
        String trimmedEmail = email != null ? email.trim() : "";
        emailSubject.onNext(trimmedEmail);
        emailCheckSubject.onNext(trimmedEmail);
    }

    @Override
    public void onPasswordChanged(String password) {
        passwordSubject.onNext(password != null ? password : "");
    }

    private void performNameValidation(String name) {
        if (!isViewAttached()) return;

        RegisterContract.View view = getView();
        String trimmedName = name.trim();

        if (trimmedName.isEmpty()) {
            isNameValid = false;
            view.clearNameError();
        } else if (trimmedName.length() < 2) {
            isNameValid = false;
            view.showNameError("Name must be at least 2 characters");
        } else if (trimmedName.length() > 50) {
            isNameValid = false;
            view.showNameError("Name cannot exceed 50 characters");
        } else if (!trimmedName.matches("^[a-zA-Z\\s]+$")) {
            isNameValid = false;
            view.showNameError("Name can only contain letters and spaces");
        } else {
            isNameValid = true;
            view.clearNameError();
        }

        updateRegisterButtonState();
    }

    private void performEmailValidation(String email) {
        if (!isViewAttached()) return;

        RegisterContract.View view = getView();
        String trimmedEmail = email.trim();

        if (trimmedEmail.isEmpty()) {
            isEmailValid = false;
            isEmailAvailable = true;
            view.clearEmailError();
        } else if (!Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            isEmailValid = false;
            isEmailAvailable = true;
            view.showEmailError("Please enter a valid email");
        } else {
            isEmailValid = true;
            view.clearEmailError();
        }

        updateRegisterButtonState();
    }

    private void performPasswordValidation(String password) {
        if (!isViewAttached()) return;

        RegisterContract.View view = getView();

        if (password.isEmpty()) {
            isPasswordValid = false;
            view.clearPasswordError();
            view.hidePasswordStrengthIndicator();
            updateRegisterButtonState();
            return;
        }

        PasswordValidationResult validationResult =
                PasswordHasher.validatePasswordStrength(password);
        PasswordStrength strengthLevel = validationResult.getStrengthLevel();
        String strengthMessage = getStrengthMessage(
                strengthLevel, validationResult.getStrengthScore());

        if (strengthLevel != null) {
            view.showPasswordStrengthIndicator(strengthLevel, strengthMessage);
        } else {
            view.hidePasswordStrengthIndicator();
        }

        if (validationResult.isValid()) {
            isPasswordValid = true;
            view.clearPasswordError();
        } else {
            isPasswordValid = false;
            view.showPasswordError(validationResult.getMessage());
        }

        updateRegisterButtonState();
    }

    private void performEmailExistsCheck(String email) {
        if (!isViewAttached()) return;

        Disposable disposable = authRepository.isEmailExists(email.trim())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        exists -> {
                            if (isViewAttached()) {
                                isEmailAvailable = !exists;
                                if (exists) {
                                    getView().showEmailError(
                                            "This email is already registered");
                                }
                                updateRegisterButtonState();
                            }
                        },
                        error -> {
                            isEmailAvailable = true;
                            updateRegisterButtonState();
                        }
                );

        disposables.add(disposable);
    }

    @Override
    public void validateName(String name) {
        performNameValidation(name);
    }

    @Override
    public void validateEmail(String email) {
        performEmailValidation(email);
    }

    @Override
    public void validatePassword(String password) {
        performPasswordValidation(password);
    }

    @Override
    public void checkEmailExists(String email) {
        if (isValidEmailFormat(email)) {
            performEmailExistsCheck(email);
        }
    }

    private String getStrengthMessage(PasswordStrength strength, int score) {
        if (strength == null) return "";

        switch (strength) {
            case WEAK:
                return "Weak password - Add more variety";
            case MEDIUM:
                return "Medium password - Getting better";
            case STRONG:
                return "Strong password - Great job!";
            default:
                return "";
        }
    }

    private void updateRegisterButtonState() {
        if (isViewAttached()) {
            boolean enabled = isNameValid && isEmailValid
                    && isPasswordValid && isEmailAvailable;
            getView().setRegisterButtonEnabled(enabled);
        }
    }

    private boolean validateAllInputs(String fullName, String email, String password) {
        boolean isValid = true;
        RegisterContract.View view = getView();

        if (fullName == null || fullName.trim().isEmpty()) {
            view.showNameError("Full name is required");
            isValid = false;
        } else if (fullName.trim().length() < 2) {
            view.showNameError("Name must be at least 2 characters");
            isValid = false;
        } else if (!fullName.trim().matches("^[a-zA-Z\\s]+$")) {
            view.showNameError("Name can only contain letters and spaces");
            isValid = false;
        }

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
        } else {
            PasswordValidationResult validationResult =
                    PasswordHasher.validatePasswordStrength(password);
            if (!validationResult.isValid()) {
                view.showPasswordError(validationResult.getMessage());
                isValid = false;
            }
        }

        return isValid;
    }

    @Override
    public void register(String fullName, String email, String password) {
        if (!isViewAttached()) return;

        RegisterContract.View view = getView();
        view.clearErrors();

        if (!validateAllInputs(fullName, email, password)) {
            return;
        }

        view.showLoading("Creating account…");

        Disposable disposable = authRepository.register(
                        fullName.trim(), email.trim(), password)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        user -> {
                            if (isViewAttached()) {
                                getView().hideLoading();
                                getView().showSuccessDialog(
                                        "Welcome to MenuMaster, "
                                                + user.getFirstName() + "!",
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
    public void signInWithGoogle(String idToken) {
        if (!isViewAttached()) return;

        RegisterContract.View view = getView();
        view.showLoading("Signing in with Google…");

        Disposable disposable = authRepository.signInWithGoogle(idToken)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        user -> {
                            if (isViewAttached()) {
                                getView().hideLoading();
                                String name = user.getFirstName() != null
                                        && !user.getFirstName().isEmpty()
                                        ? user.getFirstName()
                                        : "there";
                                getView().showSuccessDialog(
                                        "Welcome, " + name + "!",
                                        () -> getView().navigateToHome()
                                );
                            }
                        },
                        error -> {
                            if (isViewAttached()) {
                                getView().hideLoading();
                                getView().showErrorDialog(
                                        "Google sign-in failed: "
                                                + error.getMessage());
                            }
                        }
                );

        disposables.add(disposable);
    }

    @Override
    public void onDestroy() {
        disposables.clear();
        detachView();
    }
}
