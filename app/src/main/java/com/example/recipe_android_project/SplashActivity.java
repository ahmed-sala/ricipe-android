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
        // Install splash screen - MUST be before super.onCreate()
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);

        // Immediately dismiss system splash to show our custom one
        splashScreen.setKeepOnScreenCondition(() -> false);

        // Make fullscreen
        makeFullScreen();

        setContentView(R.layout.activity_splash);

        // Initialize views
        initViews();

        // Setup animation listener
        setupAnimationListener();
    }

    private void makeFullScreen() {
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );

        // Hide system UI
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
                // Animation started
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                // Animation finished - navigate to main
                navigateToMain();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                // Animation cancelled - still navigate
                navigateToMain();
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                // Not used since loop is false
            }
        });
    }

    private void navigateToMain() {
        Intent intent = new Intent(SplashActivity.this, OnboardingActivity.class);

        // Prevent lag by starting activity before animation ends visually
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);

        // Use fade transition
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
