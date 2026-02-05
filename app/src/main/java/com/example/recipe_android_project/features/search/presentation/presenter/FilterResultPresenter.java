package com.example.recipe_android_project.features.search.presentation.presenter;

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
        }
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


    @Override
    public void onMealClicked(FilterResult filterResult) {
        if (view == null || filterResult == null) return;
        view.navigateToMealDetail(filterResult.getId());
    }

    @Override
    public void onFavoriteClicked(FilterResult filterResult, boolean isFavorite) {
        if (filterResult == null) return;
    }

    @Override
    public void onBackPressed() {
        if (view != null) {
            view.navigateBack();
        }
    }


    private void cancelCurrentLoad() {
        if (currentLoadDisposable != null && !currentLoadDisposable.isDisposed()) {
            currentLoadDisposable.dispose();
        }
    }


    @Override
    public void dispose() {
        cancelCurrentLoad();
        disposables.clear();
    }
}
