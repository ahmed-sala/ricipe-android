package com.example.recipe_android_project.features.home.presentation.presenter;

import com.example.recipe_android_project.core.config.ResultCallback;
import com.example.recipe_android_project.core.helper.BasePresenter;
import com.example.recipe_android_project.features.home.data.repository.HomeRepository;
import com.example.recipe_android_project.features.home.model.Category;
import com.example.recipe_android_project.features.home.model.Meal;
import com.example.recipe_android_project.features.home.presentation.contract.HomeContract;

import java.util.List;
import java.util.Random;

public class HomePresenter extends BasePresenter<HomeContract.View> implements HomeContract.Presenter {

    private final HomeRepository repository;

    private boolean categoriesDone = false;
    private boolean mealsDone = false;

    public HomePresenter() {
        this.repository = new HomeRepository();
    }
    private String randomLetter() {
        char c = (char) ('a' + new Random().nextInt(26));
        return String.valueOf(c);
    }
    @Override
    public void loadHome() {
        if (!isViewAttached()) return;

        categoriesDone = false;
        mealsDone = false;

        getView().showScreenLoading();

        repository.getMealOfTheDay(new ResultCallback<Meal>() {
            @Override
            public void onSuccess(Meal result) {
                if (!isViewAttached()) return;
                getView().showMealOfTheDay(result);
            }

            @Override
            public void onError(Exception e) {
                if (!isViewAttached()) return;
                getView().hideMealOfDayLoading();
                getView().showError(e.getMessage() != null ? e.getMessage() : "Meal of day error");
            }
        });

        repository.getCategories(new ResultCallback<List<Category>>() {
            @Override
            public void onSuccess(List<Category> result) {
                if (!isViewAttached()) return;
                getView().showCategories(result);
                categoriesDone = true;
                finishIfDone();
            }

            @Override
            public void onError(Exception e) {
                if (!isViewAttached()) return;
                categoriesDone = true;
                getView().showError(e.getMessage() != null ? e.getMessage() : "Categories error");
                finishIfDone();
            }
        });

        repository.getMealsByFirstLetter(randomLetter(), new ResultCallback<List<Meal>>() {
            @Override
            public void onSuccess(List<Meal> result) {
                if (!isViewAttached()) return;
                getView().showMeals(result);
                mealsDone = true;
                finishIfDone();
            }

            @Override
            public void onError(Exception e) {
                if (!isViewAttached()) return;
                mealsDone = true;
                getView().showError(e.getMessage() != null ? e.getMessage() : "Meals error");
                finishIfDone();
            }
        });
    }

    private void finishIfDone() {
        if (!isViewAttached()) return;
        if (categoriesDone && mealsDone) {
            getView().hideScreenLoading();
            getView().onHomeLoaded();
        }
    }

    @Override
    public void onCategorySelected(Category category) {
        if (!isViewAttached() || category == null) return;

        repository.getMealsByCategory(category.getName(), new ResultCallback<List<Meal>>() {
            @Override
            public void onSuccess(List<Meal> result) {
                if (!isViewAttached()) return;
                getView().showMeals(result);
            }

            @Override
            public void onError(Exception e) {
                if (!isViewAttached()) return;
                getView().showError(e.getMessage() != null ? e.getMessage() : "Category filter error");
            }
        });


    }

    @Override
    public void detach() {
        detachView();
    }
}
