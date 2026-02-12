package com.example.recipe_android_project.features.meal_detail.presentation.contract;

import com.example.recipe_android_project.features.home.model.Ingredient;
import com.example.recipe_android_project.features.home.model.Meal;
import com.example.recipe_android_project.features.meal_detail.domain.model.InstructionStep;
import com.example.recipe_android_project.features.meal_detail.domain.model.MealPlan;

import java.util.List;

public interface MealDetailContract {

    interface View {
        void showScreenLoading();
        void hideScreenLoading();
        void showMealDetails(Meal meal);
        void showIngredients(List<Ingredient> ingredients);
        void showInstructions(List<InstructionStep> instructions);
        void showOfflineBanner();
        void showLimitedDataMessage();

        void showYoutubeVideo(String youtubeUrl);
        void hideYoutubeVideo();
        void updateFavoriteStatus(boolean isFavorite);
        void showFavoriteLoading();
        void hideFavoriteLoading();
        void showFavoriteSuccess(boolean isFavorite);
        void showFavoriteError(String message);
        void showLoginRequired();
        void showRemoveFavoriteConfirmation(Meal meal);
        void showAddToPlanDialog(Meal meal);
        void showPlanLoading();
        void hidePlanLoading();
        void showPlanAddedSuccess(String mealType, String date);
        void showPlanUpdatedSuccess(String mealType, String date);
        void showPlanRemovedSuccess(String mealType, String date);
        void showPlanError(String message);
        void showPlanExistsDialog(MealPlan existingPlan, String newMealName);
        void showRemovePlanConfirmation(MealPlan mealPlan);
        void updatePlanExistsWarning(boolean exists, String existingMealName);
        void showError(String message);
        void navigateBack();
    }

    interface Presenter {
        void detach();
        void loadMealDetails(String mealId);
        void onFavoriteClicked();
        void addToFavorites();
        void removeFromFavorites();
        void confirmRemoveFromFavorites();
        boolean isUserLoggedIn();
        void onBackClicked();
        void onAddToWeeklyPlanClicked();
        void checkMealPlanExists(String date, String mealType);
        void addToPlan(String date, String mealType);
        void replacePlan(String date, String mealType);
        void confirmRemovePlan(String date, String mealType);
    }
}
