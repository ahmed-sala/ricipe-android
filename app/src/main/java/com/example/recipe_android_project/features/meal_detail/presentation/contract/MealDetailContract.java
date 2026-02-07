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
        void updateFavoriteStatus(boolean isFavorite);
        void showFavoriteSuccess(boolean isFavorite);
        void showFavoriteError(String message);
        void navigateBack();
    }

    interface Presenter {
        void loadMealDetails(String mealId);
        void onFavoriteClicked();
        void onBackClicked();
        void onYoutubeVideoClicked();
        void onAddToWeeklyPlanClicked();
        void detach();
    }
}
