package com.example.recipe_android_project.features.onboarding.domain.model;

public class OnboardingItem {
    private final int imageRes;
    private final String title;
    private final String subtitle;

    public OnboardingItem(int imageRes, String title, String subtitle) {
        this.imageRes = imageRes;
        this.title = title;
        this.subtitle = subtitle;
    }

    public int getImageRes() {
        return imageRes;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }
}
