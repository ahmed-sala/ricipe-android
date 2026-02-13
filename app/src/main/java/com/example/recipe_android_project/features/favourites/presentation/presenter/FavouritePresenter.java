package com.example.recipe_android_project.features.favourites.presentation.presenter;

import android.content.Context;

import com.example.recipe_android_project.features.favourites.data.repository.FavouritesRepository;
import com.example.recipe_android_project.features.favourites.presentation.contract.FavouriteContract;
import com.example.recipe_android_project.features.home.model.Meal;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;

public class FavouritePresenter implements FavouriteContract.Presenter {

    private FavouriteContract.View view;

    private final FavouritesRepository repository;
    private final CompositeDisposable disposables = new CompositeDisposable();

    private final PublishSubject<String> searchSubject = PublishSubject.create();
    private static final long DEBOUNCE_TIMEOUT = 300;

    private List<Meal> allFavorites = new ArrayList<>();

    private Meal pendingRemoveMeal = null;
    private int pendingRemovePosition = -1;
    private Disposable pendingRemoveDisposable = null;
    private static final long UNDO_TIMEOUT = 3000;

    private String currentQuery = "";

    private boolean isRemovingLastItem = false;

    public FavouritePresenter(Context context) {
        this.repository = new FavouritesRepository(context);
        setupSearchDebounce();
    }


    public void attachView(FavouriteContract.View view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    private boolean isViewAttached() {
        return view != null;
    }



    private void setupSearchDebounce() {
        Disposable searchDisposable = searchSubject
                .debounce(DEBOUNCE_TIMEOUT, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        this::filterFavorites,
                        throwable -> {
                            if (isViewAttached()) {
                                view.showError(
                                        "Search error: "
                                                + throwable.getMessage());
                            }
                        }
                );
        disposables.add(searchDisposable);
    }



    @Override
    public void loadFavorites() {
        if (isRemovingLastItem) return;
        if (!isViewAttached()) return;

        view.showLoading();

        Disposable disposable = repository.getFavorites()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        meals -> {
                            if (isViewAttached()) {
                                view.hideLoading();
                                allFavorites = new ArrayList<>(meals);

                                for (Meal meal : allFavorites) {
                                    meal.setFavorite(true);
                                }

                                filterFavorites(currentQuery);
                            }
                        },
                        throwable -> {
                            if (isViewAttached()) {
                                view.hideLoading();
                                view.showError(
                                        getErrorMessage(throwable,
                                                "Failed to load favorites"));
                                view.showEmptyState();
                            }
                        }
                );
        disposables.add(disposable);
    }



    @Override
    public void onSearchQueryChanged(String query) {
        currentQuery = query != null ? query.trim() : "";
        searchSubject.onNext(currentQuery);
    }

    private void filterFavorites(String query) {
        if (!isViewAttached()) return;

        if (allFavorites.isEmpty()) {
            if (query == null || query.isEmpty()) {
                view.showEmptyState();
            } else {
                view.showSearchEmpty(query);
            }
            return;
        }

        if (query == null || query.trim().isEmpty()) {
            view.hideEmptyState();
            view.showFavorites(new ArrayList<>(allFavorites));
            return;
        }

        String lowerQuery = query.toLowerCase().trim();

        List<Meal> filtered = new ArrayList<>();
        for (Meal meal : allFavorites) {
            if (matchesQuery(meal, lowerQuery)) {
                filtered.add(meal);
            }
        }

        if (filtered.isEmpty()) {
            view.showSearchEmpty(query);
        } else {
            view.hideEmptyState();
            view.showFavorites(filtered);
        }
    }

    private boolean matchesQuery(Meal meal, String query) {
        if (meal == null) return false;

        if (meal.getName() != null
                && meal.getName().toLowerCase().contains(query)) {
            return true;
        }

        if (meal.getCategory() != null
                && meal.getCategory().toLowerCase().contains(query)) {
            return true;
        }

        if (meal.getArea() != null
                && meal.getArea().toLowerCase().contains(query)) {
            return true;
        }

        return false;
    }


    @Override
    public void removeFromFavorites(Meal meal) {
        if (!isViewAttached() || meal == null) return;
        view.showRemoveConfirmDialog(meal);
    }

    @Override
    public void confirmRemoveFavorite(Meal meal) {
        if (!isViewAttached() || meal == null) return;

        // Cancel any previous pending undo
        cancelPendingUndo();

        int position = findMealPosition(meal.getId());
        if (position == -1) return;

        pendingRemoveMeal = meal;
        pendingRemovePosition = position;

        boolean wasLastItem = allFavorites.size() == 1;
        isRemovingLastItem = wasLastItem;

        // 1. Remove from local list immediately
        allFavorites.remove(position);

        // 2. Update UI
        if (wasLastItem) {
            view.showEmptyState();
        } else {
            filterFavorites(currentQuery);
        }

        // 3. *** REMOVE FROM DATABASE IMMEDIATELY ***
        Disposable removeDisposable = repository
                .removeFromFavorites(meal)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> {
                            // Successfully removed from DB
                        },
                        throwable -> {
                            if (isViewAttached()) {
                                // Removal failed — restore the item
                                restoreMealToList(meal, position);
                                filterFavorites(currentQuery);
                                view.onRemoveError(
                                        getErrorMessage(throwable,
                                                "Failed to remove favorite"));
                            }
                        }
                );
        disposables.add(removeDisposable);

        // 4. Show undo snackbar (undo will RE-ADD)
        view.showUndoSnackbar(meal);

        // 5. Timer just to clear the pending undo reference
        pendingRemoveDisposable = Observable
                .timer(UNDO_TIMEOUT, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        tick -> clearPendingUndo(),
                        throwable -> clearPendingUndo()
                );
        disposables.add(pendingRemoveDisposable);
    }
    @Override
    public void undoRemove() {
        if (pendingRemoveMeal == null) return;

        // Cancel the cleanup timer
        if (pendingRemoveDisposable != null
                && !pendingRemoveDisposable.isDisposed()) {
            pendingRemoveDisposable.dispose();
            pendingRemoveDisposable = null;
        }

        Meal restoredMeal = pendingRemoveMeal;
        int restorePosition = pendingRemovePosition;

        clearPendingUndo();

        // 1. Restore to local list
        restoreMealToList(restoredMeal, restorePosition);

        // 2. Update UI
        if (isViewAttached()) {
            filterFavorites(currentQuery);
            view.onFavoriteRestored(restoredMeal);
        }

        // 3. *** RE-ADD TO DATABASE ***
        Disposable disposable = repository
                .addToFavorites(restoredMeal)  // <-- You need this method
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> {
                            // Successfully re-added
                        },
                        throwable -> {
                            if (isViewAttached()) {
                                view.onRemoveError(
                                        getErrorMessage(throwable,
                                                "Failed to restore favorite"));
                            }
                        }
                );
        disposables.add(disposable);
    }
    private void restoreMealToList(Meal meal, int position) {
        isRemovingLastItem = false;
        if (position >= 0 && position <= allFavorites.size()) {
            allFavorites.add(position, meal);
        } else {
            allFavorites.add(0, meal);
        }
    }

    private void clearPendingUndo() {
        pendingRemoveMeal = null;
        pendingRemovePosition = -1;
        isRemovingLastItem = false;
    }

    private void cancelPendingUndo() {
        if (pendingRemoveDisposable != null
                && !pendingRemoveDisposable.isDisposed()) {
            pendingRemoveDisposable.dispose();
            pendingRemoveDisposable = null;
        }
        clearPendingUndo();
    }
    private int findMealPosition(String mealId) {
        if (mealId == null) return -1;
        for (int i = 0; i < allFavorites.size(); i++) {
            Meal m = allFavorites.get(i);
            if (m != null && mealId.equals(m.getId())) {
                return i;
            }
        }
        return -1;
    }
    private void executePendingRemoveImmediately() {
        if (pendingRemoveDisposable != null
                && !pendingRemoveDisposable.isDisposed()) {
            pendingRemoveDisposable.dispose();
            pendingRemoveDisposable = null;
        }
        executePendingRemove();
    }

    private void executePendingRemove() {
        if (pendingRemoveMeal == null) return;

        Meal mealToRemove = pendingRemoveMeal;
        pendingRemoveMeal = null;
        pendingRemovePosition = -1;

        Disposable disposable = repository
                .removeFromFavorites(mealToRemove)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> {
                            isRemovingLastItem = false;
                        },
                        throwable -> {
                            isRemovingLastItem = false;
                            if (isViewAttached()) {
                                allFavorites.add(0, mealToRemove);
                                filterFavorites(currentQuery);
                                view.onRemoveError(
                                        getErrorMessage(throwable,
                                                "Failed to remove favorite"));
                            }
                        }
                );
        disposables.add(disposable);
    }



    private String getErrorMessage(Throwable throwable,
                                   String defaultMessage) {
        if (throwable == null) return defaultMessage;

        String message = throwable.getMessage();
        if (message == null || message.isEmpty())
            return defaultMessage;

        if (throwable instanceof java.net.UnknownHostException) {
            return "No internet connection";
        } else if (throwable instanceof
                java.net.SocketTimeoutException) {
            return "Connection timed out";
        } else if (throwable instanceof java.io.IOException) {
            return "Network error occurred";
        }

        return message;
    }



    @Override
    public void detach() {
        executePendingRemoveImmediately();
        isRemovingLastItem = false;
        disposables.clear();
        detachView();
    }
}
