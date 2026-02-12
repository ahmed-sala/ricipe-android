package com.example.recipe_android_project.features.home.presentation.view;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

public class HomeFragment extends Fragment implements HomeContract.View, OnMealClickListener {

    private RecyclerView rvCategories, rvMeals;

    private LottieAnimationView lottieScreenLoading;
    private LottieAnimationView lottieFeaturedLoading;

    private ImageView imgFeatured;
    private TextView tvFeaturedTitle, tvFeaturedCountry;

    private MaterialCardView cardFeatured;
    private MaterialCardView btnFeaturedFavorite;
    private ImageView icFeaturedFavorite;

    private Meal currentFeaturedMeal;

    private CategoryAdapter categoryAdapter;
    private MealAdapter mealAdapter;

    private HomePresenter presenter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        initViews(view);
        setupRecyclerViews();
        setupClickListeners();

        presenter = new HomePresenter(requireContext());
        presenter.attachView(this);

        showScreenLoading();
        showFeaturedLoading();

        presenter.loadHome();

        return view;
    }

    private void initViews(View view) {
        rvCategories = view.findViewById(R.id.rvCategories);
        rvMeals = view.findViewById(R.id.rvMeals);

        lottieScreenLoading = view.findViewById(R.id.lottieScreenLoading);
        lottieFeaturedLoading = view.findViewById(R.id.lottieFeaturedLoading);

        imgFeatured = view.findViewById(R.id.imgFeatured);
        tvFeaturedTitle = view.findViewById(R.id.tvFeaturedTitle);
        tvFeaturedCountry = view.findViewById(R.id.tvFeaturedCountry);

        cardFeatured = view.findViewById(R.id.cardFeatured);
        btnFeaturedFavorite = view.findViewById(R.id.btnFavorite);
        icFeaturedFavorite = view.findViewById(R.id.icFavorite);
    }

    private void setupRecyclerViews() {
        rvCategories.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        );
        categoryAdapter = new CategoryAdapter(new ArrayList<>(), (item, position) -> {
            presenter.onCategorySelected(item);
        });
        rvCategories.setAdapter(categoryAdapter);

        rvMeals.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvMeals.setNestedScrollingEnabled(false);
        mealAdapter = new MealAdapter(new ArrayList<>(), this);
        rvMeals.setAdapter(mealAdapter);
    }

    private void setupClickListeners() {
        cardFeatured.setOnClickListener(v -> {
            navigateToMealDetail(currentFeaturedMeal);
        });

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
        String mealName = meal.getName() != null ? meal.getName() : "this meal";

        AlertDialogHelper.showRemoveFavoriteDialog(
                requireContext(),
                mealName,
                new AlertDialogHelper.OnConfirmDialogListener() {
                    @Override
                    public void onConfirm() {
                        presenter.removeFromFavorites(meal);
                    }

                    @Override
                    public void onCancel() {
                    }
                }
        );
    }

    private void navigateToMealDetail(Meal meal) {
        if (meal == null || meal.getId() == null) {
            Toast.makeText(requireContext(), "Meal not available", Toast.LENGTH_SHORT).show();
            return;
        }

        HomeFragmentDirections.ActionHomeFragmentToMealDetailFragment action =
                HomeFragmentDirections.actionHomeFragmentToMealDetailFragment(meal.getId());

        Navigation.findNavController(requireView()).navigate(action);
    }
    private void showFeaturedLoading() {
        if (lottieFeaturedLoading != null) {
            lottieFeaturedLoading.setVisibility(View.VISIBLE);
            lottieFeaturedLoading.playAnimation();
        }

        tvFeaturedTitle.setVisibility(View.GONE);
        tvFeaturedCountry.setVisibility(View.GONE);
        tvFeaturedTitle.setText("");
        tvFeaturedCountry.setText("");
        imgFeatured.setImageDrawable(null);
    }

    private void hideFeaturedLoading() {
        if (lottieFeaturedLoading != null) {
            lottieFeaturedLoading.cancelAnimation();
            lottieFeaturedLoading.setVisibility(View.GONE);
        }
    }

    private void updateFeaturedFavoriteIcon(boolean isFavorite) {
        if (icFeaturedFavorite == null || btnFeaturedFavorite == null) return;

        if (isFavorite) {
            icFeaturedFavorite.setImageResource(R.drawable.favorite_icon);
            icFeaturedFavorite.setColorFilter(0xFFFFFFFF); // White
            btnFeaturedFavorite.setCardBackgroundColor(0xFFFF7A1A); // Orange
        } else {
            icFeaturedFavorite.setImageResource(R.drawable.ic_favorite_border);
            icFeaturedFavorite.setColorFilter(0xFFFFFFFF); // White
            btnFeaturedFavorite.setCardBackgroundColor(0x40FFFFFF); // Semi-transparent white
        }
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
        categoryAdapter.setItems(categories);
    }

    @Override
    public void showMeals(List<Meal> meals) {
        mealAdapter.setItems(meals);
    }

    @Override
    public void showMealOfTheDay(Meal meal) {
        if (meal == null) return;

        this.currentFeaturedMeal = meal;

        tvFeaturedTitle.setText(meal.getName() != null ? meal.getName() : "");
        tvFeaturedCountry.setText(meal.getArea() != null ? meal.getArea().toUpperCase() : "");

        tvFeaturedTitle.setVisibility(View.VISIBLE);
        tvFeaturedCountry.setVisibility(View.VISIBLE);

        updateFeaturedFavoriteIcon(meal.isFavorite());

        if (lottieFeaturedLoading != null) {
            lottieFeaturedLoading.setVisibility(View.VISIBLE);
            lottieFeaturedLoading.playAnimation();
        }

        Glide.with(imgFeatured)
                .load(meal.getThumbnailUrl())
                .centerCrop()
                .error(R.drawable.ic_error)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e,
                                                Object model,
                                                Target<Drawable> target,
                                                boolean isFirstResource) {
                        hideFeaturedLoading();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource,
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

    @Override
    public void showError(String message) {
        if (getView() != null) {
            SnackbarHelper.showError(getView(), message);
        }
    }

    @Override
    public void onHomeLoaded() {
        hideScreenLoading();
    }

    @Override
    public void hideMealOfDayLoading() {
        hideFeaturedLoading();
    }


    @Override
    public void onFavoriteAdded(Meal meal) {
        if (getView() != null) {
            SnackbarHelper.showSuccess(getView(), getString(R.string.added_to_favorites));
        }
    }

    @Override
    public void onFavoriteRemoved(Meal meal) {
        if (getView() != null) {
            SnackbarHelper.showSuccess(getView(), getString(R.string.removed_from_favorites));
        }
    }

    @Override
    public void onFavoriteError(String message) {
        if (getView() != null) {
            SnackbarHelper.showError(getView(), message);
        }
    }

    @Override
    public void updateMealFavoriteStatus(Meal meal, boolean isFavorite) {
        if (meal == null) return;

        if (currentFeaturedMeal != null && currentFeaturedMeal.getId().equals(meal.getId())) {
            currentFeaturedMeal.setFavorite(isFavorite);
            updateFeaturedFavoriteIcon(isFavorite);
        }

        mealAdapter.updateMealFavoriteStatus(meal.getId(), isFavorite);
    }

    @Override
    public void showLoginRequired() {
        AlertDialogHelper.showLoginRequiredDialog(
                requireContext(),
                new AlertDialogHelper.OnConfirmDialogListener() {
                    @Override
                    public void onConfirm() {
                        Intent intent = new Intent(requireContext(), AuthActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        if (getActivity() != null) {
                            getActivity().finish();
                        }
                    }
                    @Override
                    public void onCancel() {
                    }
                }
        );
    }


    @Override
    public void onMealClick(Meal meal, int position) {
        navigateToMealDetail(meal);
    }

    @Override
    public void onFavoriteClick(Meal meal, int position, boolean currentlyFavorite) {
        handleFavoriteClick(meal);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();

        Glide.with(this).clear(imgFeatured);
        if (lottieFeaturedLoading != null) lottieFeaturedLoading.cancelAnimation();
        if (lottieScreenLoading != null) lottieScreenLoading.cancelAnimation();

        if (presenter != null) {
            presenter.detach();
        }
    }
}
