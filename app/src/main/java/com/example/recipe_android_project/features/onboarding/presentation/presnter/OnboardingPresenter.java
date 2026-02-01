package com.example.recipe_android_project.features.onboarding.presentation.presnter;



import com.example.recipe_android_project.features.onboarding.presentation.contract.OnboardingContract;
import com.example.recipe_android_project.features.onboarding.data.repository.OnboardingRepository;
import com.example.recipe_android_project.features.onboarding.domain.model.OnboardingItem;

import java.util.List;
public class OnboardingPresenter implements OnboardingContract.Presenter {

    private OnboardingContract.View view;
    private final OnboardingRepository repository;
    private List<OnboardingItem> onboardingItems;

    public OnboardingPresenter(OnboardingContract.View view, OnboardingRepository repository) {
        this.view = view;
        this.repository = repository;
    }

    @Override
    public void loadOnboardingData() {
        onboardingItems = repository.getOnboardingItems();

        if (view != null) {
            view.showOnboardingItems(onboardingItems);
            view.updateIndicators(0);
            view.showNextButton();
        }
    }

    @Override
    public void onPageChanged(int position) {
        if (view == null) return;

        view.updateIndicators(position);

        if (position == onboardingItems.size() - 1) {
            view.showGetStartedButton();
        } else {
            view.showNextButton();
        }
    }

    @Override
    public void onNextClicked(int currentPosition) {
        if (view != null && currentPosition < onboardingItems.size() - 1) {
            view.navigateToNextPage();
        }
    }

    @Override
    public void onSkipClicked() {
        completeOnboarding();
    }

    @Override
    public void onGetStartedClicked() {
        completeOnboarding();
    }

    private void completeOnboarding() {
        repository.setOnboardingCompleted();
        if (view != null) {
            view.navigateToMain();
        }
    }

    @Override
    public int getTotalPages() {
        return onboardingItems != null ? onboardingItems.size() : 0;
    }

    @Override
    public boolean isOnboardingCompleted() {
        return repository.isOnboardingCompleted();
    }

    @Override
    public void onDestroy() {
        view = null;
    }
}
