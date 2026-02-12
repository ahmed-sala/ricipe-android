package com.example.recipe_android_project.features.search.presentation.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.recipe_android_project.R;
import com.example.recipe_android_project.core.ui.AlertDialogHelper;
import com.example.recipe_android_project.core.ui.SnackbarHelper;
import com.example.recipe_android_project.features.auth.presentation.view.AuthActivity;
import com.example.recipe_android_project.features.search.data.repository.SearchRepository;
import com.example.recipe_android_project.features.search.domain.model.FilterParams;
import com.example.recipe_android_project.features.search.domain.model.FilterResult;
import com.example.recipe_android_project.features.search.presentation.contract.FilterResultContract;
import com.example.recipe_android_project.features.search.presentation.presenter.FilterResultPresenter;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;

public class FilterResultFragment extends Fragment implements FilterResultContract.View {

    private MaterialToolbar toolbar;
    private RecyclerView rvFilterResults;
    private LottieAnimationView lottieLoading;
    private LinearLayout emptyStateContainer;
    private TextView tvEmptyMessage;

    private FilterResultPresenter presenter;
    private FilterResultAdapter adapter;

    private FilterParams filterParams;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            FilterResultFragmentArgs args = FilterResultFragmentArgs.fromBundle(getArguments());
            filterParams = args.getFilterParams();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_filter_result, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupToolbar();
        setupRecyclerView();
        setupPresenter();

        loadData();
    }

    private void initViews(View view) {
        toolbar = view.findViewById(R.id.toolbar);
        rvFilterResults = view.findViewById(R.id.rvFilterResults);
        lottieLoading = view.findViewById(R.id.lottieLoading);
        emptyStateContainer = view.findViewById(R.id.emptyStateContainer);
        tvEmptyMessage = view.findViewById(R.id.tvEmptyMessage);
    }

    private void setupToolbar() {
        String title = "Filter Results";
        if (filterParams != null && filterParams.getFilterTitle() != null) {
            title = filterParams.getFilterTitle();
        }
        toolbar.setTitle(title);
        toolbar.setNavigationOnClickListener(v -> navigateBack());
    }

    private void setupRecyclerView() {
        adapter = new FilterResultAdapter(new FilterResultAdapter.OnFilterResultClickListener() {
            @Override
            public void onMealClick(FilterResult filterResult, int position) {
                presenter.onMealClicked(filterResult);
            }

            @Override
            public void onFavoriteClick(FilterResult filterResult, int position, boolean isFavorite) {
                handleFavoriteClick(filterResult, isFavorite);
            }
        });

        String filterTag = "";
        if (filterParams != null && filterParams.getFilterValue() != null) {
            filterTag = filterParams.getFilterValue();
        }
        adapter.setFilterTag(filterTag);

        rvFilterResults.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvFilterResults.setAdapter(adapter);
    }

    private void setupPresenter() {
        SearchRepository repository = new SearchRepository(requireContext());
        presenter = new FilterResultPresenter(repository);
        presenter.attachView(this);
    }

    private void loadData() {
        if (filterParams != null && filterParams.isValid()) {
            presenter.loadFilterResults(filterParams.getFilterType(), filterParams.getFilterValue());
        } else {
            showError("Invalid filter parameters");
        }
    }
    private void handleFavoriteClick(FilterResult filterResult, boolean currentlyFavorite) {
        if (filterResult == null) return;

        if (!presenter.isUserLoggedIn()) {
            showLoginRequired();
            return;
        }

        if (currentlyFavorite) {
            showRemoveFavoriteDialog(filterResult);
        } else {
            presenter.addToFavorites(filterResult);
        }
    }
    private void showRemoveFavoriteDialog(FilterResult filterResult) {
        String mealName = filterResult.getName() != null ? filterResult.getName() : "this meal";

        AlertDialogHelper.showRemoveFavoriteDialog(
                requireContext(),
                mealName,
                new AlertDialogHelper.OnConfirmDialogListener() {
                    @Override
                    public void onConfirm() {
                        presenter.removeFromFavorites(filterResult);
                    }

                    @Override
                    public void onCancel() {
                        adapter.updateFavoriteStatus(filterResult.getId(), true);
                    }
                }
        );
    }

    @Override
    public void showLoading() {
        lottieLoading.setVisibility(View.VISIBLE);
        lottieLoading.playAnimation();
        rvFilterResults.setVisibility(View.GONE);
    }

    @Override
    public void hideLoading() {
        lottieLoading.cancelAnimation();
        lottieLoading.setVisibility(View.GONE);
        rvFilterResults.setVisibility(View.VISIBLE);
    }

    @Override
    public void showFilterResults(List<FilterResult> results) {
        adapter.setItems(results);
    }

    @Override
    public void showEmptyState(String message) {
        emptyStateContainer.setVisibility(View.VISIBLE);
        tvEmptyMessage.setText(message);
        rvFilterResults.setVisibility(View.GONE);
    }

    @Override
    public void hideEmptyState() {
        emptyStateContainer.setVisibility(View.GONE);
    }

    @Override
    public void showError(String message) {
        if (getView() != null) {
            SnackbarHelper.showError(getView(), message);
        }
    }

    @Override
    public void navigateBack() {
        NavController navController = Navigation.findNavController(requireView());
        navController.popBackStack();
    }

    @Override
    public void navigateToMealDetail(String mealId) {
        if (mealId == null) return;

        FilterResultFragmentDirections.ActionFilterResultFragmentToMealDetailFragment action =
                FilterResultFragmentDirections.actionFilterResultFragmentToMealDetailFragment(mealId);

        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(action);
    }


    @Override
    public void onFavoriteAdded(FilterResult filterResult) {
        if (getView() != null) {
            SnackbarHelper.showSuccess(getView(), getString(R.string.added_to_favorites));
        }
    }

    @Override
    public void onFavoriteRemoved(FilterResult filterResult) {
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
    public void updateFilterResultFavoriteStatus(int mealId, boolean isFavorite) {
        adapter.updateFavoriteStatus(mealId, isFavorite);
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
    public void onDestroyView() {
        super.onDestroyView();
        if (presenter != null) {
            presenter.detachView();
            presenter.dispose();
        }
    }
}
