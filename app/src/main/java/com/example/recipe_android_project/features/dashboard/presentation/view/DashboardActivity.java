package com.example.recipe_android_project.features.dashboard.presentation.view;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.recipe_android_project.R;
import com.example.recipe_android_project.features.dashboard.presentation.contract.DashboardContract;
import com.example.recipe_android_project.features.dashboard.presentation.presenter.DashboardPresenter;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class DashboardActivity extends AppCompatActivity
        implements DashboardContract.View, TabNavigator {

    private NavController navController;
    private BottomNavigationView bottomNavigationView;

    private BadgeDrawable favouritesBadge;

    private DashboardPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_container), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        presenter = new DashboardPresenter(this);
        presenter.attachView(this);

        setupNavigation();
        setupFavoritesBadge();

        presenter.observeFavoritesCount();
    }

    private void setupNavigation() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(bottomNavigationView, navController);

            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                handleDestinationChanged(destination);
            });
        }
    }

    private void setupFavoritesBadge() {
        favouritesBadge = bottomNavigationView.getOrCreateBadge(R.id.favouriteFragment);

        favouritesBadge.setBackgroundColor(getResources().getColor(R.color.primary, getTheme()));
        favouritesBadge.setBadgeTextColor(getResources().getColor(R.color.white, getTheme()));
        favouritesBadge.setMaxCharacterCount(3); // Shows "99+" for counts > 99

        favouritesBadge.setVisible(false);
    }

    private void handleDestinationChanged(NavDestination destination) {
        if (destination == null || presenter == null) return;

        if (destination.getId() == R.id.favouriteFragment) {
            presenter.onFavoritesTabSelected();
        } else {
            presenter.onOtherTabSelected();
        }
    }


    @Override
    public void navigateToSearchTab() {
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.searchFragment);
        }
    }


    @Override
    public void showFavoritesBadge(int count) {
        if (favouritesBadge != null) {
            favouritesBadge.setNumber(count);
            favouritesBadge.setVisible(true);
        }
    }

    @Override
    public void hideFavoritesBadge() {
        if (favouritesBadge != null) {
            favouritesBadge.setVisible(false);
        }
    }

    @Override
    public void updateBadgeCount(int count) {
        if (favouritesBadge != null) {
            favouritesBadge.setNumber(count);
        }
    }


    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (presenter != null) {
            presenter.detachView();
        }
    }
}
