package com.example.recipe_android_project.features.home.presentation.view;

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
import com.example.recipe_android_project.features.home.model.Category;
import com.example.recipe_android_project.features.home.model.Meal;
import com.example.recipe_android_project.features.home.presentation.contract.HomeContract;
import com.example.recipe_android_project.features.home.presentation.presenter.HomePresenter;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements HomeContract.View, OnMealClickListener {

    private RecyclerView rvCategories, rvMeals;

    private LottieAnimationView lottieScreenLoading;
    private LottieAnimationView lottieFeaturedLoading;

    private ImageView imgFeatured;
    private TextView tvFeaturedTitle, tvFeaturedCountry;

    private CategoryAdapter categoryAdapter;
    private MealAdapter mealAdapter;

    private HomePresenter presenter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        rvCategories = view.findViewById(R.id.rvCategories);
        rvMeals = view.findViewById(R.id.rvMeals);

        lottieScreenLoading = view.findViewById(R.id.lottieScreenLoading);
        lottieFeaturedLoading = view.findViewById(R.id.lottieFeaturedLoading);

        imgFeatured = view.findViewById(R.id.imgFeatured);
        tvFeaturedTitle = view.findViewById(R.id.tvFeaturedTitle);
        tvFeaturedCountry = view.findViewById(R.id.tvFeaturedCountry);

        setupRecyclerViews();

        presenter = new HomePresenter();
        presenter.attachView(this);

        showScreenLoading();
        showFeaturedLoading();

        presenter.loadHome();

        return view;
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

        tvFeaturedTitle.setText(meal.getName() != null ? meal.getName() : "");
        tvFeaturedCountry.setText(meal.getArea() != null ? meal.getArea().toUpperCase() : "");

        tvFeaturedTitle.setVisibility(View.VISIBLE);
        tvFeaturedCountry.setVisibility(View.VISIBLE);

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
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onHomeLoaded() {
        hideScreenLoading();
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

    @Override
    public void hideMealOfDayLoading() {
        hideFeaturedLoading();
    }

    @Override
    public void onMealClick(Meal meal, int position) {
    }

    @Override
    public void onFavoriteClick(Meal meal, int position, boolean isFavorite) {
    }
}
