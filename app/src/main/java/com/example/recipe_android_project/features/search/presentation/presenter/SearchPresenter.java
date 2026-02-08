package com.example.recipe_android_project.features.search.presentation.presenter;

import com.example.recipe_android_project.features.home.model.Area;
import com.example.recipe_android_project.features.home.model.Meal;
import com.example.recipe_android_project.features.search.data.repository.SearchRepository;
import com.example.recipe_android_project.features.search.domain.model.FilterParams;
import com.example.recipe_android_project.features.search.domain.model.FilterType;
import com.example.recipe_android_project.features.search.domain.model.Ingredient;
import com.example.recipe_android_project.features.search.presentation.contract.SearchContract;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;

public class SearchPresenter implements SearchContract.Presenter {

    private static final long DEBOUNCE_TIMEOUT_MS = 400;

    private static final int TAB_MEALS = 0;
    private static final int TAB_INGREDIENTS = 1;
    private static final int TAB_COUNTRY = 2;

    private SearchContract.View view;
    private final SearchRepository repository;
    private final CompositeDisposable disposables = new CompositeDisposable();

    private final PublishSubject<String> searchSubject = PublishSubject.create();

    private String currentQuery = "";
    private int currentTab = TAB_MEALS;

    private Disposable currentSearchDisposable;
    private Disposable favoriteDisposable;

    public SearchPresenter(SearchRepository repository) {
        this.repository = repository;
        setupSearchDebounce();
    }

    private void setupSearchDebounce() {
        Disposable searchDisposable = searchSubject
                .debounce(DEBOUNCE_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        this::performSearch,
                        throwable -> handleError("Search error: " + throwable.getMessage())
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

    private boolean isViewAttached() {
        return view != null;
    }

    @Override
    public void onSearchQueryChanged(String query) {
        currentQuery = query != null ? query.trim() : "";

        if (view == null) return;

        if (currentQuery.isEmpty()) {
            cancelCurrentSearch();
            view.hideLoading();
            view.hideEmptyState();
            resetToInitialState();
            return;
        }

        view.hideSearchPlaceholder();
        view.showLoading();

        searchSubject.onNext(currentQuery);
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
                loadAllIngredients();
                break;
            case TAB_COUNTRY:
                view.hideSearchPlaceholder();
                loadAllAreas();
                break;
        }
    }

    @Override
    public void onTabChanged(int tabIndex) {
        this.currentTab = tabIndex;

        if (view == null) return;

        cancelCurrentSearch();
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

        cancelCurrentSearch();

        switch (currentTab) {
            case TAB_MEALS:
                searchMeals(query);
                break;
            case TAB_INGREDIENTS:
                searchIngredients(query);
                break;
            case TAB_COUNTRY:
                searchAreas(query);
                break;
        }
    }

    private void searchMeals(String query) {
        currentSearchDisposable = repository.getSearchedMealsWithFavoriteStatus(query)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        meals -> handleMealsResult(query, meals),
                        throwable -> handleSearchError(query, throwable)
                );
        disposables.add(currentSearchDisposable);
    }

    private void searchIngredients(String query) {
        currentSearchDisposable = repository.searchIngredientsByName(query)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        ingredients -> handleIngredientsResult(query, ingredients),
                        throwable -> handleSearchError(query, throwable)
                );
        disposables.add(currentSearchDisposable);
    }

    private void searchAreas(String query) {
        currentSearchDisposable = repository.searchAreasByName(query)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        areas -> handleAreasResult(query, areas),
                        throwable -> handleSearchError(query, throwable)
                );
        disposables.add(currentSearchDisposable);
    }

    private void handleMealsResult(String query, List<Meal> meals) {
        if (!isValidResult(query)) return;

        view.hideLoading();

        if (meals == null || meals.isEmpty()) {
            view.showEmptyMeals();
        } else {
            view.hideEmptyState();
            view.showMeals(meals);
        }
    }

    private void handleIngredientsResult(String query, List<Ingredient> ingredients) {
        if (!isValidResult(query)) return;

        view.hideLoading();

        if (ingredients == null || ingredients.isEmpty()) {
            view.showEmptyIngredients();
        } else {
            view.hideEmptyState();
            view.showIngredients(ingredients);
        }
    }

    private void handleAreasResult(String query, List<Area> areas) {
        if (!isValidResult(query)) return;

        view.hideLoading();

        if (areas == null || areas.isEmpty()) {
            view.showEmptyAreas();
        } else {
            view.hideEmptyState();
            view.showAreas(areas);
        }
    }

    private boolean isValidResult(String query) {
        return view != null && query.equals(currentQuery);
    }

    @Override
    public void loadInitialData() {
        preloadIngredients();
        preloadAreas();
    }

    private void preloadIngredients() {
        Disposable disposable = repository.getAllIngredients()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        ingredients -> {
                            if (view != null && currentTab == TAB_INGREDIENTS && currentQuery.isEmpty()) {
                                view.hideLoading();
                                view.showIngredients(ingredients);
                            }
                        },
                        throwable -> { }
                );
        disposables.add(disposable);
    }

    private void preloadAreas() {
        Disposable disposable = repository.getAllAreas()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        areas -> {
                            if (view != null && currentTab == TAB_COUNTRY && currentQuery.isEmpty()) {
                                view.hideLoading();
                                view.showAreas(areas);
                            }
                        },
                        throwable -> { }
                );
        disposables.add(disposable);
    }

    private void loadAllIngredients() {
        if (view != null) {
            view.showLoading();
        }

        Disposable disposable = repository.getAllIngredients()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        ingredients -> {
                            if (view != null) {
                                view.hideLoading();
                                if (currentTab == TAB_INGREDIENTS && currentQuery.isEmpty()) {
                                    if (ingredients == null || ingredients.isEmpty()) {
                                        view.showEmptyIngredients();
                                    } else {
                                        view.hideEmptyState();
                                        view.showIngredients(ingredients);
                                    }
                                }
                            }
                        },
                        throwable -> handleError("Failed to load ingredients")
                );
        disposables.add(disposable);
    }

    private void loadAllAreas() {
        if (view != null) {
            view.showLoading();
        }

        Disposable disposable = repository.getAllAreas()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        areas -> {
                            if (view != null) {
                                view.hideLoading();
                                if (currentTab == TAB_COUNTRY && currentQuery.isEmpty()) {
                                    if (areas == null || areas.isEmpty()) {
                                        view.showEmptyAreas();
                                    } else {
                                        view.hideEmptyState();
                                        view.showAreas(areas);
                                    }
                                }
                            }
                        },
                        throwable -> handleError("Failed to load countries")
                );
        disposables.add(disposable);
    }


    @Override
    public void onMealClicked(Meal meal) {
        if (view != null && meal != null) {
            view.navigateToMealDetail(meal.getId());
        }
    }
    @Override
    public void onIngredientClicked(Ingredient ingredient) {
        if (view != null && ingredient != null) {
            FilterParams params = new FilterParams(
                    FilterType.INGREDIENT,
                    ingredient.getName(),
                    ingredient.getName() + " Recipes"
            );
            view.navigateToFilterResult(params);
        }
    }
    @Override
    public void onAreaClicked(Area area) {
        if (view != null && area != null) {
            FilterParams params = new FilterParams(
                    FilterType.AREA,
                    area.getName(),
                    area.getName() + " Cuisine"
            );
            view.navigateToFilterResult(params);
        }
    }
    @Override
    public void addToFavorites(Meal meal) {
        if (!isViewAttached() || meal == null) return;

        if (!isUserLoggedIn()) {
            view.showLoginRequired();
            return;
        }

        cancelFavoriteRequest();

        favoriteDisposable = repository.addToFavorites(meal)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> {
                            if (isViewAttached()) {
                                meal.setFavorite(true);
                                view.onFavoriteAdded(meal);
                                view.updateMealFavoriteStatus(meal, true);
                            }
                        },
                        throwable -> {
                            if (isViewAttached()) {
                                view.onFavoriteError(getErrorMessage(throwable));
                            }
                        }
                );
        disposables.add(favoriteDisposable);
    }
    @Override
    public void removeFromFavorites(Meal meal) {
        if (!isViewAttached() || meal == null) return;

        if (!isUserLoggedIn()) {
            view.showLoginRequired();
            return;
        }

        cancelFavoriteRequest();

        favoriteDisposable = repository.removeFromFavorites(meal)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> {
                            if (isViewAttached()) {
                                meal.setFavorite(false);
                                view.onFavoriteRemoved(meal);
                                view.updateMealFavoriteStatus(meal, false);
                            }
                        },
                        throwable -> {
                            if (isViewAttached()) {
                                view.onFavoriteError(getErrorMessage(throwable));
                            }
                        }
                );
        disposables.add(favoriteDisposable);
    }
    @Override
    public boolean isUserLoggedIn() {
        return repository.isUserAuthenticated();
    }
    private void cancelFavoriteRequest() {
        if (favoriteDisposable != null && !favoriteDisposable.isDisposed()) {
            favoriteDisposable.dispose();
        }
    }
    private void handleSearchError(String query, Throwable throwable) {
        if (!isValidResult(query)) return;

        view.hideLoading();
        view.showError(getErrorMessage(throwable));
    }
    private void handleError(String message) {
        if (view != null) {
            view.hideLoading();
            view.showError(message);
        }
    }
    private String getErrorMessage(Throwable throwable) {
        if (throwable == null) {
            return "An unknown error occurred";
        }

        String message = throwable.getMessage();
        if (message == null || message.isEmpty()) {
            return "An error occurred";
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
    private void cancelCurrentSearch() {
        if (currentSearchDisposable != null && !currentSearchDisposable.isDisposed()) {
            currentSearchDisposable.dispose();
        }
    }
    @Override
    public void dispose() {
        cancelCurrentSearch();
        cancelFavoriteRequest();
        disposables.clear();
        repository.clearCache();
    }
}
