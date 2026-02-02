package com.example.recipe_android_project.features.onboarding.data.local;

import com.example.recipe_android_project.core.helper.SharedPreferencesManager;

public class OnboardingLocalDataSource {
    private static final String KEY_ONBOARDING_COMPLETED = "onboarding_completed";
    private static final String IS_LOGGED_IN = "is_logged_in";
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
    public boolean isLoggedIn() {
        return preferencesManager.getBoolean(IS_LOGGED_IN, false);
    }
}
