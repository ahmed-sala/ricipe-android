package com.example.recipe_android_project;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.airbnb.lottie.LottieAnimationView;
import com.example.recipe_android_project.core.helper.SharedPreferencesManager;
import com.example.recipe_android_project.features.auth.presentation.view.AuthActivity;
import com.example.recipe_android_project.features.dashboard.presentation.view.DashboardActivity;
import com.example.recipe_android_project.features.onboarding.presentation.view.OnboardingActivity;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private LottieAnimationView lottieAnimationView;
    private SharedPreferencesManager preferencesManager;

    private static final String KEY_ONBOARDING_COMPLETED = "onboarding_completed";
    private static final String IS_LOGGED_IN = "is_logged_in";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        splashScreen.setKeepOnScreenCondition(() -> false);

        makeFullScreen();
        setContentView(R.layout.activity_splash);

        preferencesManager = SharedPreferencesManager.getInstance(this);

        initViews();
        setupAnimationListener();
    }

    private void makeFullScreen() {
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );
    }

    private void initViews() {
        lottieAnimationView = findViewById(R.id.lottieAnimationView);
    }

    private void setupAnimationListener() {
        lottieAnimationView.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {}

            @Override
            public void onAnimationEnd(Animator animation) {
                decideNavigation();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                decideNavigation();
            }

            @Override
            public void onAnimationRepeat(Animator animation) {}
        });
    }

    private void decideNavigation() {
        boolean onboardingCompleted = preferencesManager
                .getBoolean(KEY_ONBOARDING_COMPLETED, false);
        boolean isLoggedIn = preferencesManager
                .getBoolean(IS_LOGGED_IN, false);

        Intent intent;

        if (!onboardingCompleted) {
            intent = new Intent(this, OnboardingActivity.class);
        } else if (isLoggedIn) {
            intent = new Intent(this, DashboardActivity.class);
        } else {
            intent = new Intent(this, AuthActivity.class);
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (lottieAnimationView != null) {
            lottieAnimationView.cancelAnimation();
        }
    }
}
