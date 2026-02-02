package com.example.recipe_android_project.features.onboarding.presentation.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.recipe_android_project.R;
import com.example.recipe_android_project.core.helper.SharedPreferencesManager;
import com.example.recipe_android_project.features.auth.presentation.view.AuthActivity;
import com.example.recipe_android_project.features.dashboard.view.DashboardActivity;
import com.example.recipe_android_project.features.onboarding.presentation.contract.OnboardingContract;
import com.example.recipe_android_project.features.onboarding.data.local.OnboardingLocalDataSource;
import com.example.recipe_android_project.features.onboarding.data.repository.OnboardingRepository;
import com.example.recipe_android_project.features.onboarding.domain.model.OnboardingItem;
import com.example.recipe_android_project.features.onboarding.presentation.presnter.OnboardingPresenter;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class OnboardingActivity extends AppCompatActivity implements OnboardingContract.View {

    private ViewPager2 viewPagerOnboarding;
    private LinearLayout layoutIndicators;
    private MaterialButton buttonNext;
    private TextView textSkip;

    private OnboardingContract.Presenter presenter;
    private OnboardingAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initPresenter();

        if (presenter.isOnboardingCompleted()) {
            if(presenter.isLoggedIn()){
                navigateToHome();
            }else{
                navigateToMain();
            }
            return;
        }

        setContentView(R.layout.activity_onboarding);

        initViews();
        setupListeners();
        setupViewPagerTransformer();

        presenter.loadOnboardingData();
    }

    private void initViews() {
        viewPagerOnboarding = findViewById(R.id.viewPagerOnboarding);
        layoutIndicators = findViewById(R.id.layoutIndicators);
        buttonNext = findViewById(R.id.buttonNext);
        textSkip = findViewById(R.id.textSkip);
    }

    private void initPresenter() {
        SharedPreferencesManager preferencesManager = SharedPreferencesManager.getInstance(this);
        OnboardingLocalDataSource localDataSource = new OnboardingLocalDataSource(preferencesManager);
        OnboardingRepository repository = new OnboardingRepository(localDataSource);

        presenter = new OnboardingPresenter(this, repository);
    }

    private void setupListeners() {
        viewPagerOnboarding.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                presenter.onPageChanged(position);
            }
        });

        buttonNext.setOnClickListener(v -> {
            int currentItem = viewPagerOnboarding.getCurrentItem();
            if (currentItem < presenter.getTotalPages() - 1) {
                presenter.onNextClicked(currentItem);
            } else {
                presenter.onGetStartedClicked();
            }
        });

        textSkip.setOnClickListener(v -> presenter.onSkipClicked());
    }

    private void setupViewPagerTransformer() {
        viewPagerOnboarding.setPageTransformer((page, position) -> {
            float absPos = Math.abs(position);
            page.setAlpha(1.0f - absPos * 0.3f);
            page.setScaleY(1.0f - absPos * 0.1f);
        });
    }

    @Override
    public void showOnboardingItems(List<OnboardingItem> items) {
        adapter = new OnboardingAdapter(items);
        viewPagerOnboarding.setAdapter(adapter);
        viewPagerOnboarding.getChildAt(0).setOverScrollMode(View.OVER_SCROLL_NEVER);
        setupIndicators(items.size());
    }

    private void setupIndicators(int count) {
        layoutIndicators.removeAllViews();

        for (int i = 0; i < count; i++) {
            ImageView indicator = new ImageView(this);

            if (i == 0) {
                indicator.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.indicator_active));
                indicator.setLayoutParams(getActiveParams());
            } else {
                indicator.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.indicator_inactive));
                indicator.setLayoutParams(getInactiveParams());
            }

            layoutIndicators.addView(indicator);
        }
    }

    @Override
    public void updateIndicators(int position) {
        int childCount = layoutIndicators.getChildCount();

        for (int i = 0; i < childCount; i++) {
            ImageView indicator = (ImageView) layoutIndicators.getChildAt(i);

            if (i == position) {
                indicator.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.indicator_active));
                indicator.setLayoutParams(getActiveParams());
            } else {
                indicator.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.indicator_inactive));
                indicator.setLayoutParams(getInactiveParams());
            }
        }
    }

    private LinearLayout.LayoutParams getActiveParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dpToPx(24), dpToPx(8));
        params.setMargins(dpToPx(4), 0, dpToPx(4), 0);
        return params;
    }

    private LinearLayout.LayoutParams getInactiveParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dpToPx(8), dpToPx(8));
        params.setMargins(dpToPx(4), 0, dpToPx(4), 0);
        return params;
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    @Override
    public void showNextButton() {
        buttonNext.setText(R.string.next);
        buttonNext.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_arrow_forward));
        buttonNext.setIconGravity(MaterialButton.ICON_GRAVITY_END);
        textSkip.setVisibility(View.VISIBLE);
    }

    @Override
    public void showGetStartedButton() {
        buttonNext.setText(R.string.get_started);
        buttonNext.setIcon(null);
        textSkip.setVisibility(View.INVISIBLE);
    }

    @Override
    public void navigateToNextPage() {
        viewPagerOnboarding.setCurrentItem(viewPagerOnboarding.getCurrentItem() + 1, true);
    }

    @Override
    public void navigateToMain() {
        Intent intent = new Intent(this, AuthActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    @Override
    public void navigateToHome() {
        Intent intent = new Intent(this, DashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (presenter != null) {
            presenter.onDestroy();
        }
    }
}
