package com.example.recipe_android_project.features.meal_detail.presentation.contract;

import com.example.recipe_android_project.features.home.model.Ingredient;
import com.example.recipe_android_project.features.home.model.Meal;
import com.example.recipe_android_project.features.meal_detail.domain.model.InstructionStep;

import java.util.List;

public interface MealDetailContract {

    interface View {
        void showScreenLoading();
        void hideScreenLoading();

        void showMealDetails(Meal meal);
        void showIngredients(List<Ingredient> ingredients);
        void showInstructions(List<InstructionStep> instructions);

        void showYoutubeVideo(String youtubeUrl);
        void hideYoutubeVideo();

        void showError(String message);

        // ==================== FAVORITE METHODS ====================
        void updateFavoriteStatus(boolean isFavorite);
        void showFavoriteLoading();
        void hideFavoriteLoading();
        void showFavoriteSuccess(boolean isFavorite);
        void showFavoriteError(String message);
        void showLoginRequired();
        void showRemoveFavoriteConfirmation(Meal meal);

        void navigateBack();
    }

    interface Presenter {
        void loadMealDetails(String mealId);

        // ==================== FAVORITE METHODS ====================
        void onFavoriteClicked();
        void addToFavorites();
        void removeFromFavorites();
        void confirmRemoveFromFavorites();
        boolean isUserLoggedIn();

        void onBackClicked();
        void onYoutubeVideoClicked();
        void onAddToWeeklyPlanClicked();

        void detach();
    }
}
