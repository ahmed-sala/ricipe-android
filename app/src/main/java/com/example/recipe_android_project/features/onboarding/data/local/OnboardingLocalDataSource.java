package com.example.recipe_android_project.features.onboarding.data.local;

import com.example.recipe_android_project.common.SharedPreferencesManager;

public class OnboardingLocalDataSource {
    private static final String KEY_ONBOARDING_COMPLETED = "onboarding_completed";

    private final SharedPreferencesManager preferencesManager;

    public OnboardingLocalDataSource(SharedPreferencesManager preferencesManager) {
        this.preferencesManager = preferencesManager;
    }

    public void setOnboardingCompleted(boolean completed) {
        preferencesManager.putBoolean(KEY_ONBOARDING_COMPLETED, completed);
    }

    public boolean isOnboardingCompleted() {
        return preferencesManager.getBoolean(KEY_ONBOARDING_COMPLETED, false);
    }
}
