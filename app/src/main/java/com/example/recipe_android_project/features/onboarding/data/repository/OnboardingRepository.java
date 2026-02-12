package com.example.recipe_android_project.features.onboarding.data.repository;

import com.example.recipe_android_project.R;
import com.example.recipe_android_project.features.onboarding.data.local.OnboardingLocalDataSource;
import com.example.recipe_android_project.features.onboarding.domain.model.OnboardingItem;

import java.util.ArrayList;
import java.util.List;

public class OnboardingRepository {

    private final OnboardingLocalDataSource localDataSource;

    public OnboardingRepository(OnboardingLocalDataSource localDataSource) {
        this.localDataSource = localDataSource;
    }

    public List<OnboardingItem> getOnboardingItems() {
        List<OnboardingItem> items = new ArrayList<>();

        items.add(new OnboardingItem(
                R.drawable.onboarding_1,
                "Discover Meals",
                "Browse thousands of recipes by categories and countries."
        ));

        items.add(new OnboardingItem(
                R.drawable.onboarding_3,
                "Find by Ingredients",
                "Search recipes based on ingredients you already have at home."
        ));

        items.add(new OnboardingItem(
                R.drawable.onboarding_3,
                "Save Favorites",
                "Keep your favorite recipes organized and accessible anytime."
        ));

        return items;
    }

    public void setOnboardingCompleted() {
        localDataSource.setOnboardingCompleted(true);
    }

    public boolean isOnboardingCompleted() {
        return localDataSource.isOnboardingCompleted();
    }
}
