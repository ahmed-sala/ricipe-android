package com.example.recipe_android_project.features.home.presentation.contract;

import com.example.recipe_android_project.features.home.model.Category;
import com.example.recipe_android_project.features.home.model.Meal;

import java.util.List;

public interface HomeContract {

    interface View {
        void showScreenLoading();
        void hideScreenLoading();
        void showCategories(List<Category> categories);
        void showMeals(List<Meal> meals);
        void showMealOfTheDay(Meal meal);
        void showError(String message);
        void onHomeLoaded();
        void hideMealOfDayLoading();


    }

    interface Presenter {
        void loadHome();
        void onCategorySelected(Category category);
        void detach();
    }
}
