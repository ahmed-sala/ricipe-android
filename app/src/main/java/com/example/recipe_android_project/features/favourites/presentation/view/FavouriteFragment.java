package com.example.recipe_android_project.features.favourites.presentation.view;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.recipe_android_project.R;
import com.example.recipe_android_project.core.ui.AlertDialogHelper;
import com.example.recipe_android_project.core.ui.SnackbarHelper;
import com.example.recipe_android_project.features.favourites.presentation.contract.FavouriteContract;
import com.example.recipe_android_project.features.favourites.presentation.presenter.FavouritePresenter;
import com.example.recipe_android_project.features.home.model.Meal;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class FavouriteFragment extends Fragment implements
        FavouriteContract.View,
        FavouriteAdapter.OnFavouriteItemListener {

    private CoordinatorLayout coordinatorLayout;
    private RecyclerView rvFavorites;
    private EditText etSearch;
    private ImageView icClearSearch;
    private TextView tvFavoritesCount;
    private MaterialCardView cardSearch;

    private LinearLayout layoutEmpty;
    private LinearLayout layoutSearchEmpty;
    private TextView tvSearchEmptySubtitle;
    private LottieAnimationView lottieEmpty;
    private LottieAnimationView lottieSearchEmpty;

    private LottieAnimationView lottieLoading;

    private FavouriteAdapter adapter;

    private FavouritePresenter presenter;

    private boolean isShowingEmptyState = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favourite, container, false);

        initViews(view);
        setupRecyclerView();
        setupSearch();

        presenter = new FavouritePresenter(requireContext());
        presenter.attachView(this);

        presenter.loadFavorites();

        return view;
    }

    private void initViews(View view) {
        coordinatorLayout = view.findViewById(R.id.coordinatorLayout);
        rvFavorites = view.findViewById(R.id.rvFavorites);
        etSearch = view.findViewById(R.id.etSearch);
        icClearSearch = view.findViewById(R.id.icClearSearch);
        tvFavoritesCount = view.findViewById(R.id.tvFavoritesCount);
        cardSearch = view.findViewById(R.id.cardSearch);

        layoutEmpty = view.findViewById(R.id.layoutEmpty);
        layoutSearchEmpty = view.findViewById(R.id.layoutSearchEmpty);
        tvSearchEmptySubtitle = view.findViewById(R.id.tvSearchEmptySubtitle);
        lottieEmpty = view.findViewById(R.id.lottieEmpty);
        lottieSearchEmpty = view.findViewById(R.id.lottieSearchEmpty);

        lottieLoading = view.findViewById(R.id.lottieLoading);
    }

    private void setupRecyclerView() {
        rvFavorites.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvFavorites.setItemAnimator(null); // DISABLE item animator to prevent flicker
        adapter = new FavouriteAdapter(this);
        rvFavorites.setAdapter(adapter);
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString();
                presenter.onSearchQueryChanged(query);
                icClearSearch.setVisibility(query.isEmpty() ? View.GONE : View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        icClearSearch.setOnClickListener(v -> {
            etSearch.setText("");
            etSearch.clearFocus();
            hideKeyboard();
        });

        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard();
                return true;
            }
            return false;
        });
    }

    private void hideKeyboard() {
        if (etSearch != null && getContext() != null) {
            InputMethodManager imm = (InputMethodManager) requireContext()
                    .getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
            }
        }
    }


    @Override
    public void showLoading() {
        isShowingEmptyState = false;

        lottieLoading.setVisibility(View.VISIBLE);
        lottieLoading.playAnimation();
        rvFavorites.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.GONE);
        layoutSearchEmpty.setVisibility(View.GONE);
        cardSearch.setVisibility(View.GONE);
        stopAllAnimations();
    }

    @Override
    public void hideLoading() {
        lottieLoading.cancelAnimation();
        lottieLoading.setVisibility(View.GONE);
    }

    @Override
    public void showFavorites(List<Meal> meals) {
        isShowingEmptyState = false;

        cardSearch.setVisibility(View.VISIBLE);

        layoutEmpty.setVisibility(View.GONE);
        layoutSearchEmpty.setVisibility(View.GONE);
        lottieLoading.setVisibility(View.GONE);
        stopAllAnimations();

        rvFavorites.setVisibility(View.VISIBLE);

        adapter.setItemsImmediate(meals);

        updateFavoritesCount(meals.size());
    }

    @Override
    public void showEmptyState() {
        if (isShowingEmptyState) return;
        isShowingEmptyState = true;

        adapter.clearItemsImmediate();

        cardSearch.setVisibility(View.GONE);
        rvFavorites.setVisibility(View.GONE);
        layoutSearchEmpty.setVisibility(View.GONE);
        lottieLoading.setVisibility(View.GONE);

        if (etSearch != null) {
            etSearch.setText("");
        }

        stopSearchEmptyAnimation();

        layoutEmpty.setVisibility(View.VISIBLE);
        lottieEmpty.setVisibility(View.VISIBLE);
        lottieEmpty.playAnimation();

        updateFavoritesCount(0);
    }

    @Override
    public void hideEmptyState() {
        isShowingEmptyState = false;
        layoutEmpty.setVisibility(View.GONE);
        layoutSearchEmpty.setVisibility(View.GONE);
        stopAllAnimations();
    }

    @Override
    public void showError(String message) {
        SnackbarHelper.showError(coordinatorLayout, message);
    }



    @Override
    public void onFavoriteRestored(Meal meal) {
        isShowingEmptyState = false;
        SnackbarHelper.showSuccess(coordinatorLayout, getString(R.string.favorite_restored));
    }

    @Override
    public void onRemoveError(String message) {
        SnackbarHelper.showError(coordinatorLayout, message);
    }

    @Override
    public void showRemoveConfirmDialog(Meal meal) {
        String mealName = meal.getName() != null ? meal.getName() : "this meal";

        AlertDialogHelper.showRemoveFavoriteDialog(
                requireContext(),
                mealName,
                new AlertDialogHelper.OnConfirmDialogListener() {
                    @Override
                    public void onConfirm() {
                        presenter.confirmRemoveFavorite(meal);
                    }

                    @Override
                    public void onCancel() {
                    }
                }
        );
    }

    @Override
    public void showUndoSnackbar(Meal meal) {
        String message = meal.getName() != null
                ? getString(R.string.favorite_removed_name, meal.getName())
                : getString(R.string.favorite_removed);

        SnackbarHelper.showUndoSnackbar(
                coordinatorLayout,
                message,
                getString(R.string.undo),
                v -> presenter.undoRemove()
        );
    }

    @Override
    public void showSearchEmpty(String query) {
        isShowingEmptyState = false;

        cardSearch.setVisibility(View.VISIBLE);

        rvFavorites.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.GONE);
        lottieLoading.setVisibility(View.GONE);
        lottieEmpty.cancelAnimation();

        adapter.clearItemsImmediate();

        layoutSearchEmpty.setVisibility(View.VISIBLE);
        tvSearchEmptySubtitle.setText(getString(R.string.no_favorites_match, query));

        if (lottieSearchEmpty != null) {
            lottieSearchEmpty.setVisibility(View.VISIBLE);
            lottieSearchEmpty.playAnimation();
        }

        tvFavoritesCount.setVisibility(View.GONE);
    }
    private void stopSearchEmptyAnimation() {
        if (lottieSearchEmpty != null) {
            lottieSearchEmpty.cancelAnimation();
        }
    }
    private void stopAllAnimations() {
        if (lottieEmpty != null) {
            lottieEmpty.cancelAnimation();
        }
        if (lottieSearchEmpty != null) {
            lottieSearchEmpty.cancelAnimation();
        }
    }
    private void updateFavoritesCount(int count) {
        if (count > 0) {
            tvFavoritesCount.setText(String.valueOf(count));
            tvFavoritesCount.setVisibility(View.VISIBLE);
        } else {
            tvFavoritesCount.setVisibility(View.GONE);
        }
    }
    @Override
    public void onItemClick(Meal meal, int position) {
        if (meal == null || meal.getId() == null) return;

        FavouriteFragmentDirections.ActionFavouriteFragmentToMealDetailFragment action =
                FavouriteFragmentDirections.actionFavouriteFragmentToMealDetailFragment(meal.getId());
        Navigation.findNavController(requireView()).navigate(action);
    }

    @Override
    public void onRemoveClick(Meal meal, int position) {
        presenter.removeFromFavorites(meal);
    }


    @Override
    public void onResume() {
        super.onResume();
        if (presenter != null && !isShowingEmptyState) {
            presenter.loadFavorites();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        stopAllAnimations();
        if (lottieLoading != null) lottieLoading.cancelAnimation();

        if (presenter != null) {
            presenter.detach();
        }
    }
}
