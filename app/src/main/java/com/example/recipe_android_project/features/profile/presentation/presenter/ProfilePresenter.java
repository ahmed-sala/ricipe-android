package com.example.recipe_android_project.features.profile.presentation.presenter;

import android.content.Context;

import com.example.recipe_android_project.features.auth.data.repository.AuthRepository;
import com.example.recipe_android_project.features.auth.domain.model.User;
import com.example.recipe_android_project.features.profile.data.repository.ProfileRepository;
import com.example.recipe_android_project.features.profile.presentation.contract.ProfileContract;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ProfilePresenter implements ProfileContract.Presenter {

    private ProfileContract.View view;
    private final ProfileRepository profileRepository;
    private final AuthRepository authRepository;
    private final CompositeDisposable disposables;
    private User currentUser;

    public ProfilePresenter(Context context) {
        this.profileRepository = new ProfileRepository(context);
        this.authRepository = new AuthRepository(context);
        this.disposables = new CompositeDisposable();
    }

    @Override
    public void attachView(ProfileContract.View view) {
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
    public boolean isUserLoggedIn() {
        return authRepository.isSessionLoggedIn();
    }

    @Override
    public void loadUserData() {
        if (!isViewAttached()) return;

        if (!isUserLoggedIn()) {
            view.showGuestMode();
            return;
        }

        view.showLoading();

        Disposable disposable = profileRepository.getCurrentUser()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        user -> {
                            if (isViewAttached()) {
                                view.hideLoading();
                                currentUser = user;
                                view.showUserData(user);
                            }
                        },
                        throwable -> {
                            if (isViewAttached()) {
                                view.hideLoading();
                                view.showError(throwable.getMessage() != null
                                        ? throwable.getMessage()
                                        : "Failed to load user data");
                            }
                        }
                );

        disposables.add(disposable);
    }

    @Override
    public void logout() {
        if (!isViewAttached()) return;

        view.showLoading();

        Disposable disposable = authRepository.logout()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> {
                            if (isViewAttached()) {
                                view.hideLoading();
                                view.showLogoutSuccess();
                                view.navigateToLogin();
                            }
                        },
                        throwable -> {
                            if (isViewAttached()) {
                                view.hideLoading();
                                view.showError(throwable.getMessage() != null
                                        ? throwable.getMessage()
                                        : "Logout failed");
                            }
                        }
                );

        disposables.add(disposable);
    }

    @Override
    public void changeLanguage(String languageCode) {
        if (!isViewAttached()) return;
        view.showLanguageChanged(languageCode);
    }

    public User getCurrentUser() {
        return currentUser;
    }
}
