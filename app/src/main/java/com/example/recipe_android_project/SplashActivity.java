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
import com.example.recipe_android_project.features.onboarding.presentation.view.OnboardingActivity;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private LottieAnimationView lottieAnimationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);

        splashScreen.setKeepOnScreenCondition(() -> false);

        makeFullScreen();

        setContentView(R.layout.activity_splash);

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
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                navigateToMain();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                navigateToMain();
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
    }

    private void navigateToMain() {
        Intent intent = new Intent(SplashActivity.this, OnboardingActivity.class);

        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);

        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up animation
        if (lottieAnimationView != null) {
            lottieAnimationView.cancelAnimation();
        }
    }
}
