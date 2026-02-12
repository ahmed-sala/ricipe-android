package com.example.recipe_android_project.features.home.presentation.view;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.recipe_android_project.R;
import com.example.recipe_android_project.core.listeners.OnMealClickListener;
import com.example.recipe_android_project.core.ui.AlertDialogHelper;
import com.example.recipe_android_project.core.ui.SnackbarHelper;
import com.example.recipe_android_project.features.auth.presentation.view.AuthActivity;
import com.example.recipe_android_project.features.home.model.Category;
import com.example.recipe_android_project.features.home.model.Meal;
import com.example.recipe_android_project.features.home.presentation.contract.HomeContract;
import com.example.recipe_android_project.features.home.presentation.presenter.HomePresenter;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment
        implements HomeContract.View, OnMealClickListener {

    private RecyclerView rvCategories, rvMeals;
    private SwipeRefreshLayout swipeRefresh;
    private LinearLayout topSection;

    private LottieAnimationView lottieScreenLoading;
    private LottieAnimationView lottieFeaturedLoading;

    private LinearLayout layoutNoInternet;
    private LottieAnimationView lottieNoInternet;

    private ImageView imgFeatured;
    private TextView tvFeaturedTitle, tvFeaturedCountry;

    private MaterialCardView cardFeatured;
    private MaterialCardView btnFeaturedFavorite;
    private ImageView icFeaturedFavorite;

    private Meal currentFeaturedMeal;

    private CategoryAdapter categoryAdapter;
    private MealAdapter mealAdapter;

    private HomePresenter presenter;

    private boolean isDataLoaded = false;
    private List<Category> cachedCategories = new ArrayList<>();
    private List<Meal> cachedMeals = new ArrayList<>();
    private Meal cachedFeaturedMeal = null;
    private int cachedCategoryIndex = -1;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter = new HomePresenter(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerViews();
        setupSwipeRefresh();
        setupClickListeners();

        presenter.attachView(this);
        presenter.startNetworkMonitoring();

        if (!isDataLoaded) {
            if (presenter.isNetworkCurrentlyAvailable()) {
                showScreenLoading();
                showFeaturedLoading();
                presenter.loadHome();
            }

        } else {
            restoreCachedData();
        }
    }

    private void initViews(View view) {
        rvCategories = view.findViewById(R.id.rvCategories);
        rvMeals = view.findViewById(R.id.rvMeals);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        topSection = view.findViewById(R.id.topSection);

        lottieScreenLoading = view.findViewById(R.id.lottieScreenLoading);
        lottieFeaturedLoading = view.findViewById(R.id.lottieFeaturedLoading);

        layoutNoInternet = view.findViewById(R.id.layoutNoInternet);
        lottieNoInternet = view.findViewById(R.id.lottieNoInternet);

        imgFeatured = view.findViewById(R.id.imgFeatured);
        tvFeaturedTitle = view.findViewById(R.id.tvFeaturedTitle);
        tvFeaturedCountry = view.findViewById(R.id.tvFeaturedCountry);

        cardFeatured = view.findViewById(R.id.cardFeatured);
        btnFeaturedFavorite = view.findViewById(R.id.btnFavorite);
        icFeaturedFavorite = view.findViewById(R.id.icFavorite);
    }


    private void setupSwipeRefresh() {
        swipeRefresh.setColorSchemeColors(
                getResources().getColor(R.color.primary, null));
        swipeRefresh.setProgressBackgroundColorSchemeColor(
                getResources().getColor(R.color.white, null));

        swipeRefresh.setOnRefreshListener(this::refreshAllData);
    }

    private void refreshAllData() {
        if (!presenter.isNetworkCurrentlyAvailable()) {
            stopRefreshing();
            showNoInternet();
            return;
        }

        isDataLoaded = false;
        cachedCategories.clear();
        cachedMeals.clear();
        cachedFeaturedMeal = null;
        cachedCategoryIndex = -1;

        if (categoryAdapter != null) {
            categoryAdapter.resetSelection();
        }

        showFeaturedLoading();
        presenter.loadHome();
    }


    private void restoreCachedData() {
        hideScreenLoading();
        hideNoInternet();
        stopRefreshing();

        if (!cachedCategories.isEmpty()) {
            categoryAdapter.setItems(cachedCategories);
            if (cachedCategoryIndex >= 0) {
                categoryAdapter.setSelectedIndex(cachedCategoryIndex);
            }
        }

        if (!cachedMeals.isEmpty()) {
            mealAdapter.setItems(cachedMeals);
        }

        if (cachedFeaturedMeal != null) {
            showMealOfTheDay(cachedFeaturedMeal);
        }
    }

    private void stopRefreshing() {
        if (swipeRefresh != null && swipeRefresh.isRefreshing()) {
            swipeRefresh.setRefreshing(false);
        }
    }

    private void setupRecyclerViews() {
        rvCategories.setLayoutManager(
                new LinearLayoutManager(requireContext(),
                        LinearLayoutManager.HORIZONTAL, false));
        categoryAdapter = new CategoryAdapter(
                new ArrayList<>(), (item, position) -> {
            cachedCategoryIndex = position;
            presenter.onCategorySelected(item);
        });
        rvCategories.setAdapter(categoryAdapter);

        rvMeals.setLayoutManager(
                new LinearLayoutManager(requireContext()));
        mealAdapter = new MealAdapter(new ArrayList<>(), this);
        rvMeals.setAdapter(mealAdapter);
    }

    private void setupClickListeners() {
        cardFeatured.setOnClickListener(v ->
                navigateToMealDetail(currentFeaturedMeal));

        btnFeaturedFavorite.setOnClickListener(v -> {
            if (currentFeaturedMeal != null) {
                handleFavoriteClick(currentFeaturedMeal);
            }
        });
    }


    private void handleFavoriteClick(Meal meal) {
        if (meal == null) return;

        if (!presenter.isUserLoggedIn()) {
            showLoginRequired();
            return;
        }

        if (meal.isFavorite()) {
            showRemoveFavoriteDialog(meal);
        } else {
            presenter.addToFavorites(meal);
        }
    }

    private void showRemoveFavoriteDialog(Meal meal) {
        String mealName = meal.getName() != null
                ? meal.getName() : "this meal";

        AlertDialogHelper.showRemoveFavoriteDialog(
                requireContext(), mealName,
                new AlertDialogHelper.OnConfirmDialogListener() {
                    @Override
                    public void onConfirm() {
                        presenter.removeFromFavorites(meal);
                    }

                    @Override
                    public void onCancel() {}
                }
        );
    }


    private void navigateToMealDetail(Meal meal) {
        if (meal == null || meal.getId() == null) {
            Toast.makeText(requireContext(),
                    "Meal not available", Toast.LENGTH_SHORT).show();
            return;
        }

        HomeFragmentDirections
                .ActionHomeFragmentToMealDetailFragment action =
                HomeFragmentDirections
                        .actionHomeFragmentToMealDetailFragment(
                                meal.getId());

        Navigation.findNavController(requireView()).navigate(action);
    }


    @Override
    public void showFeaturedLoading() {
        if (lottieFeaturedLoading != null) {
            lottieFeaturedLoading.setVisibility(View.VISIBLE);
            lottieFeaturedLoading.playAnimation();
        }

        if (tvFeaturedTitle != null) {
            tvFeaturedTitle.setVisibility(View.GONE);
            tvFeaturedTitle.setText("");
        }
        if (tvFeaturedCountry != null) {
            tvFeaturedCountry.setVisibility(View.GONE);
            tvFeaturedCountry.setText("");
        }
        if (imgFeatured != null) {
            imgFeatured.setImageDrawable(null);
        }
    }

    private void hideFeaturedLoading() {
        if (lottieFeaturedLoading != null) {
            lottieFeaturedLoading.cancelAnimation();
            lottieFeaturedLoading.setVisibility(View.GONE);
        }
    }

    private void updateFeaturedFavoriteIcon(boolean isFavorite) {
        if (icFeaturedFavorite == null
                || btnFeaturedFavorite == null) return;

        if (isFavorite) {
            icFeaturedFavorite.setImageResource(
                    R.drawable.favorite_icon);
            icFeaturedFavorite.setColorFilter(0xFFFFFFFF);
            btnFeaturedFavorite.setCardBackgroundColor(0xFFFF7A1A);
        } else {
            icFeaturedFavorite.setImageResource(
                    R.drawable.ic_favorite_border);
            icFeaturedFavorite.setColorFilter(0xFFFFFFFF);
            btnFeaturedFavorite.setCardBackgroundColor(0x40FFFFFF);
        }
    }


    @Override
    public void showNoInternet() {
        if (layoutNoInternet == null) return;

        hideScreenLoading();
        hideFeaturedLoading();
        stopRefreshing();

        if (topSection != null) topSection.setVisibility(View.GONE);
        if (rvMeals != null) rvMeals.setVisibility(View.GONE);

        if (swipeRefresh != null) swipeRefresh.setEnabled(false);

        layoutNoInternet.setVisibility(View.VISIBLE);
        if (lottieNoInternet != null) {
            lottieNoInternet.playAnimation();
        }
    }

    @Override
    public void hideNoInternet() {
        if (layoutNoInternet == null) return;

        layoutNoInternet.setVisibility(View.GONE);
        if (lottieNoInternet != null) {
            lottieNoInternet.cancelAnimation();
        }

        if (topSection != null) topSection.setVisibility(View.VISIBLE);
        if (rvMeals != null) rvMeals.setVisibility(View.VISIBLE);

        if (swipeRefresh != null) swipeRefresh.setEnabled(true);
    }


    @Override
    public void showScreenLoading() {
        if (lottieScreenLoading != null) {
            lottieScreenLoading.setVisibility(View.VISIBLE);
            lottieScreenLoading.playAnimation();
        }
    }

    @Override
    public void hideScreenLoading() {
        if (lottieScreenLoading != null) {
            lottieScreenLoading.cancelAnimation();
            lottieScreenLoading.setVisibility(View.GONE);
        }
    }

    @Override
    public void showCategories(List<Category> categories) {
        cachedCategories.clear();
        cachedCategories.addAll(categories);
        if (categoryAdapter != null) {
            categoryAdapter.setItems(categories);
        }
    }

    @Override
    public void showMeals(List<Meal> meals) {
        cachedMeals.clear();
        cachedMeals.addAll(meals);
        if (mealAdapter != null) {
            mealAdapter.setItems(meals);
        }
    }

    @Override
    public void showMealOfTheDay(Meal meal) {
        if (meal == null) return;

        this.currentFeaturedMeal = meal;
        this.cachedFeaturedMeal = meal;

        if (tvFeaturedTitle != null) {
            tvFeaturedTitle.setText(
                    meal.getName() != null ? meal.getName() : "");
            tvFeaturedTitle.setVisibility(View.VISIBLE);
        }
        if (tvFeaturedCountry != null) {
            tvFeaturedCountry.setText(
                    meal.getArea() != null
                            ? meal.getArea().toUpperCase() : "");
            tvFeaturedCountry.setVisibility(View.VISIBLE);
        }

        updateFeaturedFavoriteIcon(meal.isFavorite());

        if (lottieFeaturedLoading != null) {
            lottieFeaturedLoading.setVisibility(View.VISIBLE);
            lottieFeaturedLoading.playAnimation();
        }

        if (imgFeatured != null) {
            Glide.with(imgFeatured)
                    .load(meal.getThumbnailUrl())
                    .centerCrop()
                    .error(R.drawable.ic_error)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(
                                @Nullable GlideException e,
                                Object model,
                                Target<Drawable> target,
                                boolean isFirstResource) {
                            hideFeaturedLoading();
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(
                                Drawable resource,
                                Object model,
                                Target<Drawable> target,
                                DataSource dataSource,
                                boolean isFirstResource) {
                            hideFeaturedLoading();
                            return false;
                        }
                    })
                    .into(imgFeatured);
        }
    }

    @Override
    public void showError(String message) {
        stopRefreshing();
        if (getView() != null) {
            SnackbarHelper.showError(getView(), message);
        }
    }

    @Override
    public void onHomeLoaded() {
        isDataLoaded = true;
        hideScreenLoading();
        stopRefreshing();
    }

    @Override
    public void hideMealOfDayLoading() {
        hideFeaturedLoading();
    }

    @Override
    public void onFavoriteAdded(Meal meal) {
        if (getView() != null) {
            SnackbarHelper.showSuccess(getView(),
                    getString(R.string.added_to_favorites));
        }
    }

    @Override
    public void onFavoriteRemoved(Meal meal) {
        if (getView() != null) {
            SnackbarHelper.showSuccess(getView(),
                    getString(R.string.removed_from_favorites));
        }
    }

    @Override
    public void onFavoriteError(String message) {
        if (getView() != null) {
            SnackbarHelper.showError(getView(), message);
        }
    }

    @Override
    public void updateMealFavoriteStatus(Meal meal,
                                         boolean isFavorite) {
        if (meal == null) return;

        if (currentFeaturedMeal != null
                && currentFeaturedMeal.getId().equals(meal.getId())) {
            currentFeaturedMeal.setFavorite(isFavorite);
            updateFeaturedFavoriteIcon(isFavorite);
        }

        if (mealAdapter != null) {
            mealAdapter.updateMealFavoriteStatus(
                    meal.getId(), isFavorite);
        }
    }

    @Override
    public void showLoginRequired() {
        AlertDialogHelper.showLoginRequiredDialog(
                requireContext(),
                new AlertDialogHelper.OnConfirmDialogListener() {
                    @Override
                    public void onConfirm() {
                        Intent intent = new Intent(
                                requireContext(), AuthActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        if (getActivity() != null) {
                            getActivity().finish();
                        }
                    }

                    @Override
                    public void onCancel() {}
                }
        );
    }


    @Override
    public void onMealClick(Meal meal, int position) {
        navigateToMealDetail(meal);
    }

    @Override
    public void onFavoriteClick(Meal meal, int position,
                                boolean currentlyFavorite) {
        handleFavoriteClick(meal);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (imgFeatured != null && getContext() != null) {
            Glide.with(requireContext()).clear(imgFeatured);
        }
        if (lottieFeaturedLoading != null)
            lottieFeaturedLoading.cancelAnimation();
        if (lottieScreenLoading != null)
            lottieScreenLoading.cancelAnimation();
        if (lottieNoInternet != null)
            lottieNoInternet.cancelAnimation();

        if (presenter != null) {
            presenter.detachView();
        }

        rvCategories = null;
        rvMeals = null;
        swipeRefresh = null;
        topSection = null;
        imgFeatured = null;
        tvFeaturedTitle = null;
        tvFeaturedCountry = null;
        lottieFeaturedLoading = null;
        lottieScreenLoading = null;
        cardFeatured = null;
        btnFeaturedFavorite = null;
        icFeaturedFavorite = null;
        layoutNoInternet = null;
        lottieNoInternet = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (presenter != null) {
            presenter.detach();
            presenter = null;
        }
    }
}
