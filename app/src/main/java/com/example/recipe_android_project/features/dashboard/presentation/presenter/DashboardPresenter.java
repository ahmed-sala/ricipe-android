package com.example.recipe_android_project.features.dashboard.presentation.presenter;

import android.content.Context;

import com.example.recipe_android_project.core.helper.UserSessionManager;
import com.example.recipe_android_project.features.dashboard.data.repository.DashboardRepository;
import com.example.recipe_android_project.features.dashboard.presentation.contract.DashboardContract;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class DashboardPresenter implements DashboardContract.Presenter {

    private DashboardContract.View view;
    private final DashboardRepository repository;
    private final UserSessionManager sessionManager;
    private final CompositeDisposable disposables = new CompositeDisposable();

    private int currentFavoritesCount = 0;

    private boolean isOnFavoritesTab = false;

    public DashboardPresenter(Context context) {
        this.repository = new DashboardRepository(context);
        this.sessionManager =  UserSessionManager.getInstance(context);
    }



    @Override
    public void attachView(DashboardContract.View view) {
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
    public void observeFavoritesCount() {
        String userId = sessionManager.getCurrentUserId();

        if (userId == null || userId.isEmpty()) {
            if (isViewAttached()) {
                view.hideFavoritesBadge();
            }
            return;
        }

        Disposable disposable = repository.getFavouritesCount(userId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        this::handleFavoritesCountUpdate,
                        throwable -> {
                            if (isViewAttached()) {
                                view.hideFavoritesBadge();
                            }
                        }
                );

        disposables.add(disposable);
    }

    private void handleFavoritesCountUpdate(int count) {
        if (!isViewAttached()) return;

        currentFavoritesCount = count;

        view.updateBadgeCount(count);

        if (isOnFavoritesTab) {
            view.hideFavoritesBadge();
        } else {
            if (count > 0) {
                view.showFavoritesBadge(count);
            } else {
                view.hideFavoritesBadge();
            }
        }
    }

    @Override
    public void onFavoritesTabSelected() {
        isOnFavoritesTab = true;

        if (isViewAttached()) {
            view.hideFavoritesBadge();
        }
    }

    @Override
    public void onOtherTabSelected() {
        isOnFavoritesTab = false;

        if (isViewAttached() && currentFavoritesCount > 0) {
            view.showFavoritesBadge(currentFavoritesCount);
        }
    }

}
