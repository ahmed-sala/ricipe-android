package com.example.recipe_android_project.features.search.presentation.presenter;

import com.example.recipe_android_project.core.config.ResultCallback;
import com.example.recipe_android_project.features.search.data.repository.SearchRepository;
import com.example.recipe_android_project.features.search.domain.model.FilterResult;
import com.example.recipe_android_project.features.search.domain.model.FilterResultList;
import com.example.recipe_android_project.features.search.domain.model.FilterType;
import com.example.recipe_android_project.features.search.presentation.contract.FilterResultContract;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class FilterResultPresenter implements FilterResultContract.Presenter {

    private FilterResultContract.View view;
    private final SearchRepository repository;
    private final CompositeDisposable disposables = new CompositeDisposable();



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
        repository.filterMealsByIngredient(ingredient, new ResultCallback<FilterResultList>() {
            @Override
            public void onSuccess(FilterResultList result) {
                if (view == null) return;

                view.hideLoading();

                if (result == null || result.getMeals() == null || result.getMeals().isEmpty()) {
                    view.showEmptyState("No meals found with ingredient: " + ingredient);
                } else {
                    view.hideEmptyState();
                    view.showFilterResults(result.getMeals());
                }
            }

            @Override
            public void onError(Exception e) {
                if (view == null) return;

                view.hideLoading();
                view.showError(e.getMessage() != null ? e.getMessage() : "Failed to load meals");
            }
        });
    }

    private void loadMealsByArea(String area) {
        repository.filterMealsByArea(area, new ResultCallback<FilterResultList>() {
            @Override
            public void onSuccess(FilterResultList result) {
                if (view == null) return;

                view.hideLoading();

                if (result == null || result.getMeals() == null || result.getMeals().isEmpty()) {
                    view.showEmptyState("No meals found from: " + area);
                } else {
                    view.hideEmptyState();
                    view.showFilterResults(result.getMeals());
                }
            }

            @Override
            public void onError(Exception e) {
                if (view == null) return;

                view.hideLoading();
                view.showError(e.getMessage() != null ? e.getMessage() : "Failed to load meals");
            }
        });
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

    @Override
    public void dispose() {
        disposables.clear();
        repository.dispose();
    }
}
