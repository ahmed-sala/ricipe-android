package com.example.recipe_android_project.features.onboarding.contract;

import com.example.recipe_android_project.features.onboarding.model.OnboardingItem;

import java.util.List;
public interface OnboardingContract {

    interface View {
        void showOnboardingItems(List<OnboardingItem> items);
        void updateIndicators(int position);
        void showNextButton();
        void showGetStartedButton();
        void navigateToNextPage();
        void navigateToMain();
    }

    interface Presenter {
        void loadOnboardingData();
        void onPageChanged(int position);
        void onNextClicked(int currentPosition);
        void onSkipClicked();
        void onGetStartedClicked();
        int getTotalPages();
        boolean isOnboardingCompleted();
        void onDestroy();
    }
}
