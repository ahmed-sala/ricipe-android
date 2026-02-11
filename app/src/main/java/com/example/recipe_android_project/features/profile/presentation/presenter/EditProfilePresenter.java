package com.example.recipe_android_project.features.profile.presentation.presenter;

import android.content.Context;
import android.util.Patterns;

import com.example.recipe_android_project.features.auth.domain.model.ProfileData;
import com.example.recipe_android_project.features.profile.data.datasource.repository.ProfileRepository;
import com.example.recipe_android_project.features.profile.presentation.contract.EditProfileContract;

import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;

public class EditProfilePresenter implements EditProfileContract.Presenter {

    private static final long VALIDATION_DEBOUNCE_MS = 300L;
    private static final int MIN_NAME_LENGTH = 2;
    private static final int MAX_NAME_LENGTH = 50;

    private WeakReference<EditProfileContract.View> viewRef;
    private final ProfileRepository profileRepository;
    private final CompositeDisposable disposables;

    private final PublishSubject<String> fullNameSubject = PublishSubject.create();
    private final PublishSubject<String> emailSubject = PublishSubject.create();

    private boolean isFullNameValid = false;
    private boolean isEmailValid = false;
    private boolean hasChanges = false;

    private String originalFullName = "";
    private String originalEmail = "";

    public EditProfilePresenter(Context context) {
        this.profileRepository = new ProfileRepository(context);
        this.disposables = new CompositeDisposable();
        setupValidationStreams();
    }

    private void setupValidationStreams() {
        Disposable fullNameDisposable = fullNameSubject
                .debounce(VALIDATION_DEBOUNCE_MS, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::performFullNameValidation, Throwable::printStackTrace);

        Disposable emailDisposable = emailSubject
                .debounce(VALIDATION_DEBOUNCE_MS, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::performEmailValidation, Throwable::printStackTrace);

        disposables.add(fullNameDisposable);
        disposables.add(emailDisposable);
    }

    @Override
    public void attachView(EditProfileContract.View view) {
        this.viewRef = new WeakReference<>(view);
    }

    @Override
    public void detachView() {
        if (viewRef != null) {
            viewRef.clear();
            viewRef = null;
        }
    }

    private EditProfileContract.View getView() {
        return viewRef != null ? viewRef.get() : null;
    }

    private boolean isViewAttached() {
        return viewRef != null && viewRef.get() != null;
    }

    @Override
    public void initWithProfileData(ProfileData profileData) {
        if (!isViewAttached() || profileData == null) return;

        Disposable disposable = Single.just(profileData)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        data -> {
                            if (isViewAttached()) {
                                originalFullName = data.getFullName() != null
                                        ? data.getFullName() : "";
                                originalEmail = data.getEmail() != null
                                        ? data.getEmail() : "";
                                isFullNameValid = true;
                                isEmailValid = true;
                                Objects.requireNonNull(getView())
                                        .displayUserData(originalFullName, originalEmail);
                                updateSaveButtonState();
                            }
                        },
                        error -> {
                            if (isViewAttached()) {
                                Objects.requireNonNull(getView())
                                        .showErrorDialog("Failed to load profile data");
                            }
                        }
                );

        disposables.add(disposable);
    }

    @Override
    public void onFullNameChanged(String fullName) {
        fullNameSubject.onNext(fullName != null ? fullName : "");
        checkForChanges(fullName, null);
    }

    @Override
    public void onEmailChanged(String email) {
        emailSubject.onNext(email != null ? email : "");
        checkForChanges(null, email);
    }

    private void checkForChanges(String fullName, String email) {
        if (fullName != null) {
            hasChanges = !fullName.trim().equals(originalFullName);
        }
        if (email != null) {
            hasChanges = hasChanges || !email.trim().equals(originalEmail);
        }
    }

    private void performFullNameValidation(String fullName) {
        if (!isViewAttached()) return;

        EditProfileContract.View view = getView();
        String trimmed = fullName.trim();

        if (trimmed.isEmpty()) {
            isFullNameValid = false;
            view.showFullNameError("Full name is required");
        } else if (trimmed.length() < MIN_NAME_LENGTH) {
            isFullNameValid = false;
            view.showFullNameError("Name must be at least " + MIN_NAME_LENGTH + " characters");
        } else if (trimmed.length() > MAX_NAME_LENGTH) {
            isFullNameValid = false;
            view.showFullNameError("Name must be less than " + MAX_NAME_LENGTH + " characters");
        } else if (!isValidNameFormat(trimmed)) {
            isFullNameValid = false;
            view.showFullNameError("Name can only contain letters and spaces");
        } else {
            isFullNameValid = true;
            view.clearFullNameError();
        }

        updateSaveButtonState();
    }

    private void performEmailValidation(String email) {
        if (!isViewAttached()) return;

        EditProfileContract.View view = getView();
        String trimmed = email.trim();

        if (trimmed.isEmpty()) {
            isEmailValid = false;
            view.showEmailError("Email is required");
        } else if (!Patterns.EMAIL_ADDRESS.matcher(trimmed).matches()) {
            isEmailValid = false;
            view.showEmailError("Please enter a valid email address");
        } else {
            if (!trimmed.equalsIgnoreCase(originalEmail)) {
                checkEmailAvailability(trimmed);
            } else {
                isEmailValid = true;
                view.clearEmailError();
                updateSaveButtonState();
            }
        }
    }

    private void checkEmailAvailability(String email) {
        Disposable disposable = profileRepository.isEmailExists(email)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        exists -> {
                            if (isViewAttached()) {
                                if (exists) {
                                    isEmailValid = false;
                                    getView().showEmailError("This email is already in use");
                                } else {
                                    isEmailValid = true;
                                    getView().clearEmailError();
                                }
                                updateSaveButtonState();
                            }
                        },
                        error -> {
                            if (isViewAttached()) {
                                isEmailValid = true;
                                getView().clearEmailError();
                                updateSaveButtonState();
                            }
                        }
                );

        disposables.add(disposable);
    }

    private boolean isValidNameFormat(String name) {
        return name.matches("^[a-zA-Z\\s'-]+$");
    }

    @Override
    public void validateFullName(String fullName) {
        performFullNameValidation(fullName);
    }

    @Override
    public void validateEmail(String email) {
        performEmailValidation(email);
    }

    private void updateSaveButtonState() {
        if (isViewAttached()) {
            boolean shouldEnable = isFullNameValid
                    && isEmailValid
                    && hasUnsavedChanges(null, null);
            Objects.requireNonNull(getView()).setSaveButtonEnabled(shouldEnable);
        }
    }

    @Override
    public boolean hasUnsavedChanges(String currentFullName, String currentEmail) {
        if (currentFullName == null && currentEmail == null) {
            return hasChanges;
        }

        boolean nameChanged = currentFullName != null
                && !currentFullName.trim().equals(originalFullName);
        boolean emailChanged = currentEmail != null
                && !currentEmail.trim().equals(originalEmail);

        return nameChanged || emailChanged;
    }

    @Override
    public void onBackPressed(String currentFullName, String currentEmail) {
        if (!isViewAttached()) return;

        if (hasUnsavedChanges(currentFullName, currentEmail)) {
            getView().showDiscardChangesDialog(() -> getView().navigateBack());
        } else {
            getView().navigateBack();
        }
    }

    private boolean validateInputsSync(String fullName, String email) {
        boolean isValid = true;
        EditProfileContract.View view = getView();

        String trimmedName = fullName.trim();
        if (trimmedName.isEmpty()) {
            view.showFullNameError("Full name is required");
            isValid = false;
        } else if (trimmedName.length() < MIN_NAME_LENGTH) {
            view.showFullNameError("Name must be at least " + MIN_NAME_LENGTH + " characters");
            isValid = false;
        } else if (trimmedName.length() > MAX_NAME_LENGTH) {
            view.showFullNameError("Name must be less than " + MAX_NAME_LENGTH + " characters");
            isValid = false;
        } else if (!isValidNameFormat(trimmedName)) {
            view.showFullNameError("Name can only contain letters and spaces");
            isValid = false;
        }

        String trimmedEmail = email.trim();
        if (trimmedEmail.isEmpty()) {
            view.showEmailError("Email is required");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            view.showEmailError("Please enter a valid email address");
            isValid = false;
        }

        return isValid;
    }

    @Override
    public void saveProfile(String fullName, String email) {
        if (!isViewAttached()) return;

        EditProfileContract.View view = getView();
        view.clearErrors();

        if (!validateInputsSync(fullName, email)) {
            return;
        }

        String trimmedFullName = fullName.trim();
        String trimmedEmail = email.trim();

        if (!hasUnsavedChanges(trimmedFullName, trimmedEmail)) {
            view.showSuccessSnackbar("No changes to save");
            return;
        }

        boolean isOnline = profileRepository.isNetworkAvailable();

        if (isOnline) {
            saveProfileOnline(trimmedFullName, trimmedEmail);
        } else {
            saveProfileOffline(trimmedFullName, trimmedEmail);
        }
    }

    private void saveProfileOnline(String fullName, String email) {
        if (!isViewAttached()) return;

        EditProfileContract.View view = getView();
        view.showLoading("Updating profile...");

        Disposable disposable = profileRepository.updateProfile(fullName, email)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        success -> {
                            if (isViewAttached()) {
                                getView().hideLoading();
                                if (success) {
                                    originalFullName = fullName;
                                    originalEmail = email;
                                    hasChanges = false;
                                    updateSaveButtonState();

                                    getView().showSuccessDialog(
                                            "Profile updated successfully!",
                                            () -> getView().navigateBack()
                                    );
                                } else {
                                    getView().showErrorDialog("Failed to update profile");
                                }
                            }
                        },
                        error -> {
                            if (isViewAttached()) {
                                getView().hideLoading();
                                getView().showErrorDialog(
                                        error.getMessage() != null
                                                ? error.getMessage()
                                                : "Failed to update profile"
                                );
                            }
                        }
                );

        disposables.add(disposable);
    }

    private void saveProfileOffline(String fullName, String email) {
        if (!isViewAttached()) return;

        EditProfileContract.View view = getView();
        view.showLoading("Saving locally...");

        Disposable disposable = profileRepository.updateProfileOffline(fullName, email)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        success -> {
                            if (isViewAttached()) {
                                getView().hideLoading();
                                if (success) {
                                    originalFullName = fullName;
                                    originalEmail = email;
                                    hasChanges = false;
                                    updateSaveButtonState();

                                    getView().showOfflineUpdateSnackbar();
                                    getView().showSuccessDialog(
                                            "Profile saved locally. Will sync when online.",
                                            () -> getView().navigateBack()
                                    );
                                } else {
                                    getView().showErrorDialog("Failed to save profile");
                                }
                            }
                        },
                        error -> {
                            if (isViewAttached()) {
                                getView().hideLoading();
                                getView().showErrorDialog(
                                        error.getMessage() != null
                                                ? error.getMessage()
                                                : "Failed to save profile"
                                );
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
