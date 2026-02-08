package com.example.recipe_android_project.features.meal_detail.presentation.view;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.recipe_android_project.R;
import com.example.recipe_android_project.core.ui.AlertDialogHelper;
import com.example.recipe_android_project.core.ui.SnackbarHelper;
import com.example.recipe_android_project.features.home.model.Ingredient;
import com.example.recipe_android_project.features.home.model.Meal;
import com.example.recipe_android_project.features.meal_detail.domain.model.InstructionStep;
import com.example.recipe_android_project.features.meal_detail.presentation.contract.MealDetailContract;
import com.example.recipe_android_project.features.meal_detail.presentation.presenter.MealDetailPresenter;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MealDetailFragment extends Fragment implements MealDetailContract.View {

    // ==================== VIEWS ====================
    private AppBarLayout appBarLayout;
    private CollapsingToolbarLayout collapsingToolbar;
    private ImageView ivMealImage, ivBack, ivFavorite;
    private View viewOverlay, viewCurvedBottom;
    private FrameLayout btnBack, btnFavorite;
    private TextView tvMealTitle, tvCategory, tvCountryFlag, tvCountry;
    private TextView tvIngredientCount;
    private RecyclerView rvIngredients, rvInstructions;
    private MaterialButton btnReadMore;
    private TextView tvToolbarTitle;

    private YouTubePlayerView youtubePlayerView;
    private MaterialCardView cardVideoPlayer, cardNoVideo;
    private MaterialButton btnAddToWeeklyPlan;

    // Loading views
    private FrameLayout loadingContainer;
    private LottieAnimationView lottieScreenLoading;
    private LottieAnimationView lottieImageLoading;
    private TextView tvLoadingMessage;
    private View contentContainer;

    // Favorite loading (optional - add to layout if needed)
    private ProgressBar favoriteLoadingProgress;

    // Adapters
    private InstructionsAdapter instructionsAdapter;
    private IngredientsAdapter ingredientsAdapter;

    // Presenter
    private MealDetailPresenter presenter;

    // State
    private boolean isAppBarCollapsed = false;
    private String mealTitle = "";
    private boolean currentFavoriteState = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_meal_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupToolbarBehavior();
        setupClickListeners();
        setupRecyclerViews();
        setupYouTubePlayer();
        setupPresenter();

        loadMealFromArguments();
    }

    private void initViews(View view) {
        // Loading views
        loadingContainer = view.findViewById(R.id.loadingContainer);
        lottieScreenLoading = view.findViewById(R.id.lottieScreenLoading);
        lottieImageLoading = view.findViewById(R.id.lottieImageLoading);
        tvLoadingMessage = view.findViewById(R.id.tvLoadingMessage);
        contentContainer = view.findViewById(R.id.contentContainer);

        // AppBar views
        appBarLayout = view.findViewById(R.id.appBarLayout);
        collapsingToolbar = view.findViewById(R.id.collapsingToolbar);
        ivMealImage = view.findViewById(R.id.ivMealImage);
        viewOverlay = view.findViewById(R.id.viewOverlay);
        viewCurvedBottom = view.findViewById(R.id.viewCurvedBottom);
        ivBack = view.findViewById(R.id.ivBack);
        ivFavorite = view.findViewById(R.id.ivFavorite);
        btnBack = view.findViewById(R.id.btnBack);
        btnFavorite = view.findViewById(R.id.btnFavorite);
        tvToolbarTitle = view.findViewById(R.id.tvToolbarTitle);

        // Content views
        tvMealTitle = view.findViewById(R.id.tvMealTitle);
        tvCategory = view.findViewById(R.id.tvCategory);
        tvCountryFlag = view.findViewById(R.id.tvCountryFlag);
        tvCountry = view.findViewById(R.id.tvCountry);
        tvIngredientCount = view.findViewById(R.id.tvIngredientCount);
        rvIngredients = view.findViewById(R.id.rvIngredients);
        rvInstructions = view.findViewById(R.id.rvInstructions);
        btnReadMore = view.findViewById(R.id.btnReadMore);

        // Video views
        youtubePlayerView = view.findViewById(R.id.youtubePlayerView);
        cardVideoPlayer = view.findViewById(R.id.cardVideoPlayer);
        cardNoVideo = view.findViewById(R.id.cardNoVideo);

        // Action buttons
        btnAddToWeeklyPlan = view.findViewById(R.id.btnAddToWeeklyPlan);

        // Optional: Favorite loading progress (add to layout if needed)
        // favoriteLoadingProgress = view.findViewById(R.id.favoriteLoadingProgress);
    }

    private void setupPresenter() {
        presenter = new MealDetailPresenter(requireContext());
        presenter.attachView(this);
    }

    private void loadMealFromArguments() {
        if (getArguments() != null) {
            String mealId = MealDetailFragmentArgs.fromBundle(getArguments()).getMealId();
            presenter.loadMealDetails(mealId);
        }
    }

    private void setupRecyclerViews() {
        // Ingredients RecyclerView
        rvIngredients.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        );
        ingredientsAdapter = new IngredientsAdapter(new ArrayList<>());
        rvIngredients.setAdapter(ingredientsAdapter);

        // Instructions RecyclerView
        rvInstructions.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvInstructions.setNestedScrollingEnabled(false);
        instructionsAdapter = new InstructionsAdapter(new ArrayList<>());
        rvInstructions.setAdapter(instructionsAdapter);
    }

    private void setupToolbarBehavior() {
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }

                float percentage = Math.abs(verticalOffset) / (float) scrollRange;

                if (viewOverlay != null) {
                    viewOverlay.setAlpha(1f - percentage);
                }

                if (viewCurvedBottom != null) {
                    viewCurvedBottom.setAlpha(1f - percentage);
                }

                if (tvToolbarTitle != null) {
                    float titleAlpha = Math.max(0f, (percentage - 0.5f) * 2f);
                    tvToolbarTitle.setAlpha(titleAlpha);
                }

                if (percentage > 0.7f && !isAppBarCollapsed) {
                    isAppBarCollapsed = true;
                    updateToolbarForCollapsed();
                } else if (percentage <= 0.7f && isAppBarCollapsed) {
                    isAppBarCollapsed = false;
                    updateToolbarForExpanded();
                }
            }
        });
    }

    private void updateToolbarForCollapsed() {
        btnBack.setBackgroundResource(R.drawable.bg_toolbar_button_light);
        btnFavorite.setBackgroundResource(R.drawable.bg_toolbar_button_light);
        ivBack.setColorFilter(ContextCompat.getColor(requireContext(), R.color.black), PorterDuff.Mode.SRC_IN);

        // Update favorite icon color based on state
        updateFavoriteIconColor();
    }

    private void updateToolbarForExpanded() {
        btnBack.setBackgroundResource(R.drawable.bg_toolbar_button);
        btnFavorite.setBackgroundResource(R.drawable.bg_toolbar_button);
        ivBack.setColorFilter(ContextCompat.getColor(requireContext(), R.color.white), PorterDuff.Mode.SRC_IN);

        // Update favorite icon color based on state
        updateFavoriteIconColor();
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> presenter.onBackClicked());

        btnFavorite.setOnClickListener(v -> presenter.onFavoriteClicked());

        btnReadMore.setOnClickListener(v -> {
            instructionsAdapter.toggleExpanded();
            if (instructionsAdapter.isExpanded()) {
                btnReadMore.setText("Show Less");
                btnReadMore.setIconResource(R.drawable.up_icon);
            } else {
                btnReadMore.setText("Read More");
                btnReadMore.setIconResource(R.drawable.icon_bottom);
            }
        });

        btnAddToWeeklyPlan.setOnClickListener(v -> {
            presenter.onAddToWeeklyPlanClicked();
        });
    }

    private void setupYouTubePlayer() {
        getLifecycle().addObserver(youtubePlayerView);
    }

    // ==================== LOADING METHODS ====================

    @Override
    public void showScreenLoading() {
        if (loadingContainer != null) {
            loadingContainer.setVisibility(View.VISIBLE);
        }
        if (lottieScreenLoading != null) {
            lottieScreenLoading.playAnimation();
        }
        if (contentContainer != null) {
            contentContainer.setVisibility(View.GONE);
        }
    }

    @Override
    public void hideScreenLoading() {
        if (loadingContainer != null) {
            loadingContainer.animate()
                    .alpha(0f)
                    .setDuration(300)
                    .withEndAction(() -> {
                        loadingContainer.setVisibility(View.GONE);
                        loadingContainer.setAlpha(1f);
                        if (lottieScreenLoading != null) {
                            lottieScreenLoading.cancelAnimation();
                        }
                    })
                    .start();
        }
        if (contentContainer != null) {
            contentContainer.setVisibility(View.VISIBLE);
            contentContainer.setAlpha(0f);
            contentContainer.animate()
                    .alpha(1f)
                    .setDuration(300)
                    .start();
        }
    }

    // ==================== MEAL DETAILS METHODS ====================

    @Override
    public void showMealDetails(Meal meal) {
        if (meal == null) return;

        mealTitle = meal.getName() != null ? meal.getName() : "";
        tvMealTitle.setText(mealTitle);
        tvToolbarTitle.setText(mealTitle);

        tvCategory.setText(meal.getCategory() != null ? meal.getCategory() : "");

        String area = meal.getArea() != null ? meal.getArea() : "";
        tvCountryFlag.setText(getCountryFlag(area));
        tvCountry.setText(area);

        loadMealImageWithLottie(meal.getThumbnailUrl());
    }

    private void loadMealImageWithLottie(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            hideImageLoading();
            ivMealImage.setVisibility(View.VISIBLE);
            ivMealImage.setImageResource(R.drawable.ic_error);
            return;
        }

        showImageLoading();

        Glide.with(this)
                .load(imageUrl)
                .transition(DrawableTransitionOptions.withCrossFade(500))
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                Target<Drawable> target, boolean isFirstResource) {
                        hideImageLoading();
                        ivMealImage.setVisibility(View.VISIBLE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model,
                                                   Target<Drawable> target, DataSource dataSource,
                                                   boolean isFirstResource) {
                        hideImageLoading();
                        ivMealImage.setVisibility(View.VISIBLE);
                        return false;
                    }
                })
                .error(R.drawable.ic_error)
                .into(ivMealImage);
    }

    private void showImageLoading() {
        if (lottieImageLoading != null) {
            lottieImageLoading.setVisibility(View.VISIBLE);
            lottieImageLoading.playAnimation();
        }
        if (ivMealImage != null) {
            ivMealImage.setVisibility(View.INVISIBLE);
        }
    }

    private void hideImageLoading() {
        if (lottieImageLoading != null) {
            lottieImageLoading.cancelAnimation();
            lottieImageLoading.setVisibility(View.GONE);
        }
    }

    @Override
    public void showIngredients(List<Ingredient> ingredients) {
        if (ingredients == null || ingredients.isEmpty()) {
            tvIngredientCount.setText("0 items");
            return;
        }

        tvIngredientCount.setText(ingredients.size() + " items");
        ingredientsAdapter.updateData(ingredients);
    }

    @Override
    public void showInstructions(List<InstructionStep> instructions) {
        if (instructions == null || instructions.isEmpty()) {
            btnReadMore.setVisibility(View.GONE);
            return;
        }

        instructionsAdapter.updateData(instructions);
        btnReadMore.setVisibility(instructionsAdapter.hasMore() ? View.VISIBLE : View.GONE);
    }

    // ==================== VIDEO METHODS ====================

    @Override
    public void showYoutubeVideo(String youtubeUrl) {
        if (youtubeUrl == null || youtubeUrl.isEmpty()) {
            hideYoutubeVideo();
            return;
        }

        cardVideoPlayer.setVisibility(View.VISIBLE);
        cardNoVideo.setVisibility(View.GONE);

        String videoId = extractYouTubeVideoId(youtubeUrl);

        youtubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                if (videoId != null) {
                    youTubePlayer.cueVideo(videoId, 0);
                }
            }
        });
    }

    @Override
    public void hideYoutubeVideo() {
        cardVideoPlayer.setVisibility(View.GONE);
        cardNoVideo.setVisibility(View.VISIBLE);
    }

    // ==================== FAVORITE METHODS ====================

    @Override
    public void updateFavoriteStatus(boolean isFavorite) {
        this.currentFavoriteState = isFavorite;

        // Update icon based on favorite state
        int iconRes = isFavorite ? R.drawable.ic_favorite_filled
                : R.drawable.ic_favorite_border;
        ivFavorite.setImageResource(iconRes);

        // Update icon color
        updateFavoriteIconColor();
    }

    private void updateFavoriteIconColor() {
        if (currentFavoriteState) {
            // When favorite - show orange/red color regardless of toolbar state
            ivFavorite.setColorFilter(
                    ContextCompat.getColor(requireContext(), R.color.primary),
                    PorterDuff.Mode.SRC_IN
            );
        } else {
            // When not favorite - color depends on toolbar state
            int color = isAppBarCollapsed ? R.color.black : R.color.white;
            ivFavorite.setColorFilter(
                    ContextCompat.getColor(requireContext(), color),
                    PorterDuff.Mode.SRC_IN
            );
        }
    }

    @Override
    public void showFavoriteLoading() {
        // Disable button during loading
        btnFavorite.setEnabled(false);

        // Optional: Show progress indicator
        // if (favoriteLoadingProgress != null) {
        //     favoriteLoadingProgress.setVisibility(View.VISIBLE);
        //     ivFavorite.setVisibility(View.INVISIBLE);
        // }
    }

    @Override
    public void hideFavoriteLoading() {
        // Re-enable button
        btnFavorite.setEnabled(true);

        // Optional: Hide progress indicator
        // if (favoriteLoadingProgress != null) {
        //     favoriteLoadingProgress.setVisibility(View.GONE);
        //     ivFavorite.setVisibility(View.VISIBLE);
        // }
    }

    @Override
    public void showFavoriteSuccess(boolean isFavorite) {
        if (getView() != null) {
            String message = isFavorite
                    ? getString(R.string.added_to_favorites)
                    : getString(R.string.removed_from_favorites);
            SnackbarHelper.showSuccess(getView(), message);
        }
    }

    @Override
    public void showFavoriteError(String message) {
        if (getView() != null) {
            SnackbarHelper.showError(getView(), message);
        }
    }

    @Override
    public void showLoginRequired() {
        if (getView() != null) {
            SnackbarHelper.showWarning(getView(), getString(R.string.login_required));
        }

        // Optional: Navigate to login screen
        // Navigation.findNavController(requireView()).navigate(R.id.action_to_login);
    }

    @Override
    public void showRemoveFavoriteConfirmation(Meal meal) {
        String mealName = meal.getName() != null ? meal.getName() : "this meal";

        AlertDialogHelper.showRemoveFavoriteDialog(
                requireContext(),
                mealName,
                new AlertDialogHelper.OnConfirmDialogListener() {
                    @Override
                    public void onConfirm() {
                        presenter.confirmRemoveFromFavorites();
                    }

                    @Override
                    public void onCancel() {
                        // Do nothing - user cancelled
                    }
                }
        );
    }

    // ==================== ERROR & NAVIGATION ====================

    @Override
    public void showError(String message) {
        if (getView() != null) {
            SnackbarHelper.showError(getView(), message);
        }
    }

    @Override
    public void navigateBack() {
        Navigation.findNavController(requireView()).navigateUp();
    }

    // ==================== HELPER METHODS ====================

    private String getCountryFlag(String area) {
        if (area == null) return "🏳️";

        switch (area.toLowerCase()) {
            case "american":
            case "usa":
                return "🇺🇸";
            case "british":
            case "uk":
            case "england":
                return "🇬🇧";
            case "canadian":
                return "🇨🇦";
            case "chinese":
                return "🇨🇳";
            case "croatian":
                return "🇭🇷";
            case "dutch":
                return "🇳🇱";
            case "egyptian":
                return "🇪🇬";
            case "filipino":
                return "🇵🇭";
            case "french":
                return "🇫🇷";
            case "greek":
                return "🇬🇷";
            case "indian":
                return "🇮🇳";
            case "irish":
                return "🇮🇪";
            case "italian":
            case "italy":
                return "🇮🇹";
            case "jamaican":
                return "🇯🇲";
            case "japanese":
                return "🇯🇵";
            case "kenyan":
                return "🇰🇪";
            case "malaysian":
                return "🇲🇾";
            case "mexican":
                return "🇲🇽";
            case "moroccan":
                return "🇲🇦";
            case "polish":
                return "🇵🇱";
            case "portuguese":
                return "🇵🇹";
            case "russian":
                return "🇷🇺";
            case "spanish":
                return "🇪🇸";
            case "thai":
                return "🇹🇭";
            case "tunisian":
                return "🇹🇳";
            case "turkish":
                return "🇹🇷";
            case "vietnamese":
                return "🇻🇳";
            default:
                return "🏳️";
        }
    }

    private String extractYouTubeVideoId(String url) {
        if (url == null) return null;

        String pattern = "(?<=watch\\?v=|/videos/|embed/|youtu.be/|/v/|watch\\?v%3D|/e/|watch\\?feature=player_embedded&v=)[^#&?\\n]*";
        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(url);

        return matcher.find() ? matcher.group() : null;
    }

    // ==================== LIFECYCLE ====================

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (youtubePlayerView != null) {
            youtubePlayerView.release();
        }

        if (ivMealImage != null) {
            Glide.with(this).clear(ivMealImage);
        }

        if (lottieScreenLoading != null) {
            lottieScreenLoading.cancelAnimation();
        }

        if (lottieImageLoading != null) {
            lottieImageLoading.cancelAnimation();
        }

        if (presenter != null) {
            presenter.detach();
        }
    }
}
