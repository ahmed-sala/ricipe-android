package com.example.recipe_android_project.features.search.presentation.presenter;

import com.example.recipe_android_project.features.home.model.Meal;
import com.example.recipe_android_project.features.search.data.repository.SearchRepository;
import com.example.recipe_android_project.features.search.domain.model.FilterResult;
import com.example.recipe_android_project.features.search.domain.model.FilterResultList;
import com.example.recipe_android_project.features.search.domain.model.FilterType;
import com.example.recipe_android_project.features.search.presentation.contract.FilterResultContract;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class FilterResultPresenter implements FilterResultContract.Presenter {

    private FilterResultContract.View view;
    private final SearchRepository repository;
    private final CompositeDisposable disposables = new CompositeDisposable();

    private Disposable currentLoadDisposable;
    private Disposable favoriteDisposable;

    public FilterResultPresenter(SearchRepository repository) {
        this.repository = repository;
    }

    @Override
    public void attachView(FilterResultContract.View view) {
        this.view = view;
    }

    @Override
    public void detachView() {
        this.view = null;
    }

    private boolean isViewAttached() {
        return view != null;
    }

    @Override
    public boolean isUserLoggedIn() {
        return repository.isUserAuthenticated();
    }

    @Override
    public void loadFilterResults(String filterType, String filterValue) {
        if (view == null) return;

        cancelCurrentLoad();

        view.showLoading();
        view.hideEmptyState();

        if (FilterType.INGREDIENT.equals(filterType)) {
            loadMealsByIngredient(filterValue);
        } else if (FilterType.AREA.equals(filterType)) {
            loadMealsByArea(filterValue);
        } else {
            view.hideLoading();
            view.showError("Unknown filter type");
        }
    }

    private void loadMealsByIngredient(String ingredient) {
        currentLoadDisposable = repository.filterMealsByIngredient(ingredient)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> handleFilterResult(result, "No meals found with ingredient: " + ingredient),
                        throwable -> handleError(throwable, "Failed to load meals by ingredient")
                );
        disposables.add(currentLoadDisposable);
    }

    private void loadMealsByArea(String area) {
        currentLoadDisposable = repository.filterMealsByArea(area)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> handleFilterResult(result, "No meals found from: " + area),
                        throwable -> handleError(throwable, "Failed to load meals by area")
                );
        disposables.add(currentLoadDisposable);
    }

    private void handleFilterResult(FilterResultList result, String emptyMessage) {
        if (view == null) return;

        view.hideLoading();

        if (result == null || result.getMeals() == null || result.getMeals().isEmpty()) {
            view.showEmptyState(emptyMessage);
        } else {
            view.hideEmptyState();
            view.showFilterResults(result.getMeals());
            loadFavoriteStatuses(result.getMeals());
        }
    }
    private void loadFavoriteStatuses(java.util.List<FilterResult> results) {
        if (!isViewAttached() || results == null || results.isEmpty()) return;

        for (FilterResult result : results) {
            Disposable disposable = repository.isFavorite(String.valueOf(result.getId()))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            isFavorite -> {
                                if (isViewAttached()) {
                                    view.updateFilterResultFavoriteStatus(result.getId(), isFavorite);
                                }
                            },
                            throwable -> {  }
                    );
            disposables.add(disposable);
        }
    }


    @Override
    public void addToFavorites(FilterResult filterResult) {
        if (!isViewAttached() || filterResult == null) return;

        if (!isUserLoggedIn()) {
            view.showLoginRequired();
            return;
        }

        cancelFavoriteRequest();


        favoriteDisposable = repository.getMealById(String.valueOf(filterResult.getId()))
                .flatMapCompletable(meal -> repository.addToFavorites(meal))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> {
                            if (isViewAttached()) {
                                view.onFavoriteAdded(filterResult);
                                view.updateFilterResultFavoriteStatus(filterResult.getId(), true);
                            }
                        },
                        throwable -> {
                            if (isViewAttached()) {
                                view.onFavoriteError(getErrorMessage(throwable, "Failed to add favorite"));
                            }
                        }
                );
        disposables.add(favoriteDisposable);
    }

    @Override
    public void removeFromFavorites(FilterResult filterResult) {
        if (!isViewAttached() || filterResult == null) return;

        if (!isUserLoggedIn()) {
            view.showLoginRequired();
            return;
        }

        cancelFavoriteRequest();


        favoriteDisposable = repository.removeFromFavorites(String.valueOf(filterResult.getId()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> {
                            if (isViewAttached()) {
                                view.onFavoriteRemoved(filterResult);
                                view.updateFilterResultFavoriteStatus(filterResult.getId(), false);
                            }
                        },
                        throwable -> {
                            if (isViewAttached()) {
                                view.onFavoriteError(getErrorMessage(throwable, "Failed to remove favorite"));
                            }
                        }
                );
        disposables.add(favoriteDisposable);
    }
    private void cancelFavoriteRequest() {
        if (favoriteDisposable != null && !favoriteDisposable.isDisposed()) {
            favoriteDisposable.dispose();
        }
    }


    @Override
    public void onMealClicked(FilterResult filterResult) {
        if (view == null || filterResult == null) return;
        view.navigateToMealDetail(String.valueOf(filterResult.getId()));
    }
    private void handleError(Throwable throwable, String defaultMessage) {
        if (view == null) return;

        view.hideLoading();
        view.showError(getErrorMessage(throwable, defaultMessage));
    }

    private String getErrorMessage(Throwable throwable, String defaultMessage) {
        if (throwable == null) {
            return defaultMessage;
        }

        String message = throwable.getMessage();
        if (message == null || message.isEmpty()) {
            return defaultMessage;
        }

        if (throwable instanceof java.net.UnknownHostException) {
            return "No internet connection";
        } else if (throwable instanceof java.net.SocketTimeoutException) {
            return "Connection timed out";
        } else if (throwable instanceof java.io.IOException) {
            return "Network error occurred";
        }

        return message;
    }


    private void cancelCurrentLoad() {
        if (currentLoadDisposable != null && !currentLoadDisposable.isDisposed()) {
            currentLoadDisposable.dispose();
        }
    }

    @Override
    public void dispose() {
        cancelCurrentLoad();
        cancelFavoriteRequest();
        disposables.clear();
    }
}
