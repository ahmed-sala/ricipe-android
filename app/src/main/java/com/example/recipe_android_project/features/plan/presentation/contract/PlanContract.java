package com.example.recipe_android_project.features.plan.presentation.contract;

import com.example.recipe_android_project.features.meal_detail.domain.model.MealPlan;
import com.example.recipe_android_project.features.plan.domain.model.DayModel;

import java.util.List;

public interface PlanContract {

    interface View {
        void showLoading();
        void hideLoading();
        void showMealLoading(String mealType);
        void hideMealLoading(String mealType);
        void showBreakfast(MealPlan mealPlan);
        void showLunch(MealPlan mealPlan);
        void showDinner(MealPlan mealPlan);
        void showBreakfastEmpty();
        void showLunchEmpty();
        void showDinnerEmpty();
        void showMealPlansForDate(List<MealPlan> mealPlans);
        void showEmptyState();
        void showError(String message);
        void navigateToMealDetail(String mealId);
        void navigateToAddMeal(String date, String mealType);
        void showLoginRequired();
        void updateSelectedDateDisplay(int day, int month, int year);
        void updateSpinners(int month, int year);
    }

    interface Presenter {
        void attachView(View view);
        void detachView();
        void dispose();
        void onDateSelected(DayModel day);
        void onDateSelected(int day, int month, int year);
        void loadMealPlansForDate(String date);
        void loadMealPlansForCurrentDate();
        void onBreakfastClicked();
        void onLunchClicked();
        void onDinnerClicked();
        void confirmRemoveMeal(String mealType);
        void onAddBreakfastClicked();
        void onAddLunchClicked();
        void onAddDinnerClicked();
        String getCurrentDate();
        boolean isUserLoggedIn();
    }
}
