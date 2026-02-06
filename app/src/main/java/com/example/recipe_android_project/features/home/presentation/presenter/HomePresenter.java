package com.example.recipe_android_project.features.home.presentation.presenter;

import com.example.recipe_android_project.core.helper.BasePresenter;
import com.example.recipe_android_project.features.home.data.repository.HomeRepository;
import com.example.recipe_android_project.features.home.model.Category;
import com.example.recipe_android_project.features.home.model.Meal;
import com.example.recipe_android_project.features.home.presentation.contract.HomeContract;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class HomePresenter extends BasePresenter<HomeContract.View> implements HomeContract.Presenter {

    private final HomeRepository repository;
    private final CompositeDisposable disposables = new CompositeDisposable();

    private Disposable categoryDisposable;
    private Disposable mealsDisposable;
    private Disposable mealOfDayDisposable;
    private Disposable categoryFilterDisposable;

    private final AtomicInteger completedRequests = new AtomicInteger(0);
    private static final int TOTAL_INITIAL_REQUESTS = 2;

    public HomePresenter() {
        this.repository = new HomeRepository();
    }

    public HomePresenter(HomeRepository repository) {
        this.repository = repository;
    }

    private String randomLetter() {
        char c = (char) ('a' + new Random().nextInt(26));
        return String.valueOf(c);
    }

    @Override
    public void loadHome() {
        if (!isViewAttached()) return;

        cancelAllRequests();
        completedRequests.set(0);

        getView().showScreenLoading();

        loadMealOfTheDay();
        loadCategories();
        loadMealsByRandomLetter();
    }

    private void loadMealOfTheDay() {
        mealOfDayDisposable = repository.getMealOfTheDay()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        meal -> {
                            if (isViewAttached()) {
                                getView().showMealOfTheDay(meal);
                            }
                        },
                        throwable -> {
                            if (isViewAttached()) {
                                getView().hideMealOfDayLoading();
                                getView().showError(getErrorMessage(throwable, "Meal of day error"));
                            }
                        }
                );
        disposables.add(mealOfDayDisposable);
    }

    private void loadCategories() {
        categoryDisposable = repository.getCategories()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        categories -> {
                            if (isViewAttached()) {
                                getView().showCategories(categories);
                            }
                            checkAndFinishLoading();
                        },
                        throwable -> {
                            if (isViewAttached()) {
                                getView().showError(getErrorMessage(throwable, "Categories error"));
                            }
                            checkAndFinishLoading();
                        }
                );
        disposables.add(categoryDisposable);
    }

    private void loadMealsByRandomLetter() {
        mealsDisposable = repository.getMealsByFirstLetter(randomLetter())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        meals -> {
                            if (isViewAttached()) {
                                getView().showMeals(meals);
                            }
                            checkAndFinishLoading();
                        },
                        throwable -> {
                            if (isViewAttached()) {
                                getView().showError(getErrorMessage(throwable, "Meals error"));
                            }
                            checkAndFinishLoading();
                        }
                );
        disposables.add(mealsDisposable);
    }

    private void checkAndFinishLoading() {
        int completed = completedRequests.incrementAndGet();
        if (completed >= TOTAL_INITIAL_REQUESTS && isViewAttached()) {
            getView().hideScreenLoading();
            getView().onHomeLoaded();
        }
    }

    @Override
    public void onCategorySelected(Category category) {
        if (!isViewAttached() || category == null) return;

        cancelCategoryFilter();

        categoryFilterDisposable = repository.getMealsByCategory(category.getName())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        meals -> {
                            if (isViewAttached()) {
                                getView().showMeals(meals);
                            }
                        },
                        throwable -> {
                            if (isViewAttached()) {
                                getView().showError(getErrorMessage(throwable, "Category filter error"));
                            }
                        }
                );
        disposables.add(categoryFilterDisposable);
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

    private void cancelCategoryFilter() {
        if (categoryFilterDisposable != null && !categoryFilterDisposable.isDisposed()) {
            categoryFilterDisposable.dispose();
        }
    }

    private void cancelAllRequests() {
        if (mealOfDayDisposable != null && !mealOfDayDisposable.isDisposed()) {
            mealOfDayDisposable.dispose();
        }
        if (categoryDisposable != null && !categoryDisposable.isDisposed()) {
            categoryDisposable.dispose();
        }
        if (mealsDisposable != null && !mealsDisposable.isDisposed()) {
            mealsDisposable.dispose();
        }
        cancelCategoryFilter();
    }

    @Override
    public void detach() {
        dispose();
        detachView();
    }

    public void dispose() {
        cancelAllRequests();
        disposables.clear();
    }
}
