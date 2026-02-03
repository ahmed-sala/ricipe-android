package com.example.recipe_android_project.features.search.presentation.presenter;

import com.example.recipe_android_project.core.config.ResultCallback;
import com.example.recipe_android_project.features.home.model.Area;
import com.example.recipe_android_project.features.home.model.Meal;
import com.example.recipe_android_project.features.search.data.repository.SearchRepository;
import com.example.recipe_android_project.features.search.domain.model.Ingredient;
import com.example.recipe_android_project.features.search.presentation.contract.SearchContract;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.PublishSubject;

public class SearchPresenter implements SearchContract.Presenter {

    private static final long DEBOUNCE_TIMEOUT_MS = 400;

    private static final int TAB_MEALS = 0;
    private static final int TAB_INGREDIENTS = 1;
    private static final int TAB_COUNTRY = 2;

    private SearchContract.View view;
    private final SearchRepository repository;
    private final CompositeDisposable disposables = new CompositeDisposable();

    private final PublishSubject<SearchQuery> searchSubject = PublishSubject.create();

    private String currentQuery = "";
    private int currentTab = TAB_MEALS;

    private List<Ingredient> allIngredients;
    private List<Area> allAreas;

    private boolean ingredientsLoaded = false;
    private boolean areasLoaded = false;

    private static class SearchQuery {
        final String query;
        final long timestamp;

        SearchQuery(String query) {
            this.query = query;
            this.timestamp = System.currentTimeMillis();
        }
    }

    public SearchPresenter(SearchRepository repository) {
        this.repository = repository;
        setupSearchDebounce();
    }

    private void setupSearchDebounce() {
        Disposable searchDisposable = searchSubject
                .debounce(DEBOUNCE_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        searchQuery -> performSearch(searchQuery.query),
                        throwable -> {
                            if (view != null) {
                                view.showError(throwable.getMessage());
                            }
                        }
                );
        disposables.add(searchDisposable);
    }

    @Override
    public void attachView(SearchContract.View view) {
        this.view = view;
    }

    @Override
    public void detachView() {
        this.view = null;
    }

    @Override
    public void onSearchQueryChanged(String query) {
        currentQuery = query != null ? query.trim() : "";

        if (view == null) return;

        if (currentQuery.isEmpty()) {
            view.hideLoading();
            view.hideEmptyState();
            resetToInitialState();
            return;
        }

        view.hideSearchPlaceholder();
        view.showLoading();

        searchSubject.onNext(new SearchQuery(currentQuery));
    }


    private void resetToInitialState() {
        if (view == null) return;

        switch (currentTab) {
            case TAB_MEALS:
                view.showSearchPlaceholder();
                view.clearMeals();
                break;
            case TAB_INGREDIENTS:
                view.hideSearchPlaceholder();
                if (allIngredients != null && !allIngredients.isEmpty()) {
                    view.showIngredients(allIngredients);
                } else {
                    loadAllIngredients();
                }
                break;
            case TAB_COUNTRY:
                view.hideSearchPlaceholder();
                if (allAreas != null && !allAreas.isEmpty()) {
                    view.showAreas(allAreas);
                } else {
                    loadAllAreas();
                }
                break;
        }
    }

    @Override
    public void onTabChanged(int tabIndex) {
        this.currentTab = tabIndex;

        if (view == null) return;

        view.hideEmptyState();

        if (currentQuery.isEmpty()) {
            resetToInitialState();
        } else {
            view.showLoading();
            performSearch(currentQuery);
        }
    }

    private void performSearch(String query) {
        if (view == null) return;

        if (query == null || query.trim().isEmpty()) {
            view.hideLoading();
            resetToInitialState();
            return;
        }

        switch (currentTab) {
            case TAB_MEALS:
                searchMeals(query);
                break;
            case TAB_INGREDIENTS:
                filterIngredients(query);
                break;
            case TAB_COUNTRY:
                filterAreas(query);
                break;
        }
    }

    private void searchMeals(String query) {
        repository.searchMealsByName(query, new ResultCallback<List<Meal>>() {
            @Override
            public void onSuccess(List<Meal> meals) {
                if (view == null) return;

                if (!query.equals(currentQuery)) return;

                view.hideLoading();

                if (meals == null || meals.isEmpty()) {
                    view.showEmptyMeals();
                } else {
                    view.hideEmptyState();
                    view.showMeals(meals);
                }
            }

            @Override
            public void onError(Exception e) {
                if (view == null) return;

                if (!query.equals(currentQuery)) return;

                view.hideLoading();
                view.showError(e.getMessage());
            }
        });
    }

    private void filterIngredients(String query) {
        if (allIngredients == null || allIngredients.isEmpty()) {
            repository.getAllIngredients(new ResultCallback<List<Ingredient>>() {
                @Override
                public void onSuccess(List<Ingredient> ingredients) {
                    allIngredients = ingredients;
                    ingredientsLoaded = true;

                    if (!query.equals(currentQuery)) return;

                    filterIngredientsLocally(query);
                }

                @Override
                public void onError(Exception e) {
                    if (view == null) return;
                    view.hideLoading();
                    view.showError(e.getMessage());
                }
            });
        } else {
            filterIngredientsLocally(query);
        }
    }

    private void filterIngredientsLocally(String query) {
        if (view == null) return;

        String lowerQuery = query.toLowerCase();
        List<Ingredient> filtered = new ArrayList<>();

        for (Ingredient ingredient : allIngredients) {
            if (ingredient != null &&
                    ingredient.getName() != null &&
                    ingredient.getName().toLowerCase().contains(lowerQuery)) {
                filtered.add(ingredient);
            }
        }

        view.hideLoading();

        if (filtered.isEmpty()) {
            view.showEmptyIngredients();
        } else {
            view.hideEmptyState();
            view.showIngredients(filtered);
        }
    }

    private void filterAreas(String query) {
        if (allAreas == null || allAreas.isEmpty()) {
            repository.getAllAreas(new ResultCallback<List<Area>>() {
                @Override
                public void onSuccess(List<Area> areas) {
                    allAreas = areas;
                    areasLoaded = true;

                    if (!query.equals(currentQuery)) return;

                    filterAreasLocally(query);
                }

                @Override
                public void onError(Exception e) {
                    if (view == null) return;
                    view.hideLoading();
                    view.showError(e.getMessage());
                }
            });
        } else {
            filterAreasLocally(query);
        }
    }

    private void filterAreasLocally(String query) {
        if (view == null) return;

        String lowerQuery = query.toLowerCase();
        List<Area> filtered = new ArrayList<>();

        for (Area area : allAreas) {
            if (area != null &&
                    area.getName() != null &&
                    area.getName().toLowerCase().contains(lowerQuery)) {
                filtered.add(area);
            }
        }

        view.hideLoading();

        if (filtered.isEmpty()) {
            view.showEmptyAreas();
        } else {
            view.hideEmptyState();
            view.showAreas(filtered);
        }
    }

    @Override
    public void loadInitialData() {
        loadAllIngredients();
        loadAllAreas();
    }

    private void loadAllIngredients() {
        if (view != null && currentTab == TAB_INGREDIENTS) {
            view.showLoading();
        }

        repository.getAllIngredients(new ResultCallback<List<Ingredient>>() {
            @Override
            public void onSuccess(List<Ingredient> ingredients) {
                allIngredients = ingredients;
                ingredientsLoaded = true;

                if (view != null) {
                    view.hideLoading();
                    if (currentTab == TAB_INGREDIENTS && currentQuery.isEmpty()) {
                        view.showIngredients(ingredients);
                    }
                }
            }

            @Override
            public void onError(Exception e) {
                if (view != null) {
                    view.hideLoading();
                    if (currentTab == TAB_INGREDIENTS) {
                        view.showError("Failed to load ingredients");
                    }
                }
            }
        });
    }

    private void loadAllAreas() {
        if (view != null && currentTab == TAB_COUNTRY) {
            view.showLoading();
        }

        repository.getAllAreas(new ResultCallback<List<Area>>() {
            @Override
            public void onSuccess(List<Area> areas) {
                allAreas = areas;
                areasLoaded = true;

                if (view != null) {
                    view.hideLoading();
                    if (currentTab == TAB_COUNTRY && currentQuery.isEmpty()) {
                        view.showAreas(areas);
                    }
                }
            }

            @Override
            public void onError(Exception e) {
                if (view != null) {
                    view.hideLoading();
                    if (currentTab == TAB_COUNTRY) {
                        view.showError("Failed to load countries");
                    }
                }
            }
        });
    }

    @Override
    public void onMealClicked(Meal meal) {
    }

    @Override
    public void onIngredientClicked(Ingredient ingredient) {
    }

    @Override
    public void onAreaClicked(Area area) {
    }

    @Override
    public void dispose() {
        disposables.clear();
        repository.dispose();
    }
}
