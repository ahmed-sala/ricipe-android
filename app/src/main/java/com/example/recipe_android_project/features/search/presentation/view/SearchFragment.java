package com.example.recipe_android_project.features.search.presentation.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.recipe_android_project.R;
import com.example.recipe_android_project.features.home.model.Area;
import com.example.recipe_android_project.features.home.model.Meal;
import com.example.recipe_android_project.features.home.presentation.view.MealAdapter;
import com.example.recipe_android_project.features.search.data.repository.SearchRepository;
import com.example.recipe_android_project.features.search.domain.model.Ingredient;
import com.example.recipe_android_project.features.search.presentation.contract.SearchContract;
import com.example.recipe_android_project.features.search.presentation.presenter.SearchPresenter;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment implements
        SearchContract.View,
        MealAdapter.OnMealClickListener {

    private static final int VOICE_SEARCH_REQUEST_CODE = 100;

    private static final int TAB_MEALS = 0;
    private static final int TAB_INGREDIENTS = 1;
    private static final int TAB_COUNTRY = 2;

    private SearchContract.Presenter presenter;

    private MaterialCardView searchCard;
    private EditText etSearch;
    private ImageView icSearch;
    private ImageView icMicrophone;
    private ImageView icClear;  // NEW: Clear button

    private ChipGroup chipGroup;
    private Chip chipMeals;
    private Chip chipIngredients;
    private Chip chipCountry;

    private FrameLayout contentContainer;
    private LinearLayout mealsContent;
    private LinearLayout searchPlaceholderContainer;
    private ImageView imgSearchPlaceholder;
    private RecyclerView rvMeals;
    private RecyclerView rvIngredients;
    private RecyclerView rvCountry;
    private LottieAnimationView lottieLoading;

    private LinearLayout emptyStateContainer;
    private LottieAnimationView lottieEmptyState;
    private TextView tvEmptyTitle;
    private TextView tvEmptyMessage;

    private MealAdapter mealAdapter;
    private IngredientAdapter ingredientAdapter;
    private AreaAdapter areaAdapter;

    private List<Meal> mealsList = new ArrayList<>();
    private List<Ingredient> ingredientsList = new ArrayList<>();
    private List<Area> areasList = new ArrayList<>();

    private int currentTab = TAB_MEALS;

    public SearchFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initPresenter();
        initViews(view);
        setupChips();
        setupSearch();
        setupRecyclerViews();

        presenter.attachView(this);
        presenter.loadInitialData();
    }

    private void initPresenter() {
        SearchRepository repository = new SearchRepository();
        presenter = new SearchPresenter(repository);
    }

    private void initViews(View view) {
        searchCard = view.findViewById(R.id.searchCard);
        etSearch = view.findViewById(R.id.etSearch);
        icSearch = view.findViewById(R.id.icSearch);
        icMicrophone = view.findViewById(R.id.icMicrophone);
        icClear = view.findViewById(R.id.icClear);  // NEW

        chipGroup = view.findViewById(R.id.chipGroup);
        chipMeals = view.findViewById(R.id.chipMeals);
        chipIngredients = view.findViewById(R.id.chipIngredients);
        chipCountry = view.findViewById(R.id.chipCountry);

        contentContainer = view.findViewById(R.id.contentContainer);
        mealsContent = view.findViewById(R.id.mealsContent);
        searchPlaceholderContainer = view.findViewById(R.id.searchPlaceholderContainer);
        imgSearchPlaceholder = view.findViewById(R.id.imgSearchPlaceholder);
        rvMeals = view.findViewById(R.id.rvMeals);
        rvIngredients = view.findViewById(R.id.rvIngredients);
        rvCountry = view.findViewById(R.id.rvCountry);
        lottieLoading = view.findViewById(R.id.lottieLoading);

        emptyStateContainer = view.findViewById(R.id.emptyStateContainer);
        lottieEmptyState = view.findViewById(R.id.lottieEmptyState);
        tvEmptyTitle = view.findViewById(R.id.tvEmptyTitle);
        tvEmptyMessage = view.findViewById(R.id.tvEmptyMessage);
    }

    private void setupChips() {
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            hideEmptyState();

            if (checkedIds.contains(R.id.chipMeals)) {
                currentTab = TAB_MEALS;
                showMealsContent();
                presenter.onTabChanged(TAB_MEALS);
            } else if (checkedIds.contains(R.id.chipIngredients)) {
                currentTab = TAB_INGREDIENTS;
                showIngredientsContent();
                presenter.onTabChanged(TAB_INGREDIENTS);
            } else if (checkedIds.contains(R.id.chipCountry)) {
                currentTab = TAB_COUNTRY;
                showCountryContent();
                presenter.onTabChanged(TAB_COUNTRY);
            }
        });
    }

    private void showMealsContent() {
        mealsContent.setVisibility(View.VISIBLE);
        rvIngredients.setVisibility(View.GONE);
        rvCountry.setVisibility(View.GONE);
    }

    private void showIngredientsContent() {
        mealsContent.setVisibility(View.GONE);
        rvIngredients.setVisibility(View.VISIBLE);
        rvCountry.setVisibility(View.GONE);
    }

    private void showCountryContent() {
        mealsContent.setVisibility(View.GONE);
        rvIngredients.setVisibility(View.GONE);
        rvCountry.setVisibility(View.VISIBLE);
    }

    private void setupSearch() {
        // Text change listener
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Toggle microphone/clear button based on text
                updateSearchIcons(s != null && s.length() > 0);
            }

            @Override
            public void afterTextChanged(Editable s) {
                String query = s != null ? s.toString() : "";
                presenter.onSearchQueryChanged(query);
            }
        });

        // Microphone click - start voice search
        icMicrophone.setOnClickListener(v -> startVoiceSearch());

        // Clear button click - clear the search field
        icClear.setOnClickListener(v -> clearSearch());

        // Search icon click - trigger search
        icSearch.setOnClickListener(v -> {
            String query = etSearch.getText().toString().trim();
            if (!query.isEmpty()) {
                presenter.onSearchQueryChanged(query);
            }
        });

        // Handle keyboard search action
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String query = etSearch.getText().toString().trim();
                if (!query.isEmpty()) {
                    presenter.onSearchQueryChanged(query);
                }
                return true;
            }
            return false;
        });
    }

    /**
     * Toggle between microphone and clear icons
     * @param hasText true if search field has text
     */
    private void updateSearchIcons(boolean hasText) {
        if (hasText) {
            // Show clear button, hide microphone
            icMicrophone.setVisibility(View.GONE);
            icClear.setVisibility(View.VISIBLE);
        } else {
            // Show microphone, hide clear button
            icMicrophone.setVisibility(View.VISIBLE);
            icClear.setVisibility(View.GONE);
        }
    }

    /**
     * Clear the search field and reset state
     */
    private void clearSearch() {
        etSearch.setText("");
        etSearch.clearFocus();

        // Optional: Hide keyboard
        // hideKeyboard();
    }

    /**
     * Optional: Hide the soft keyboard
     */
    private void hideKeyboard() {
        if (getActivity() != null) {
            android.view.inputmethod.InputMethodManager imm =
                    (android.view.inputmethod.InputMethodManager) getActivity()
                            .getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            if (imm != null && etSearch != null) {
                imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
            }
        }
    }

    private void setupRecyclerViews() {
        rvMeals.setLayoutManager(new LinearLayoutManager(requireContext()));
        mealAdapter = new MealAdapter(mealsList, this);
        rvMeals.setAdapter(mealAdapter);

        rvIngredients.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        ingredientAdapter = new IngredientAdapter(ingredientsList, this::onIngredientClicked);
        rvIngredients.setAdapter(ingredientAdapter);

        rvCountry.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        areaAdapter = new AreaAdapter(areasList, this::onAreaClicked);
        rvCountry.setAdapter(areaAdapter);
    }

    // ==================== SearchContract.View Implementation ====================

    @Override
    public void showLoading() {
        lottieLoading.setVisibility(View.VISIBLE);
        lottieLoading.playAnimation();
        hideEmptyState();
    }

    @Override
    public void hideLoading() {
        lottieLoading.setVisibility(View.GONE);
        lottieLoading.cancelAnimation();
    }

    @Override
    public void showMeals(List<Meal> meals) {
        hideEmptyState();
        hideSearchPlaceholder();
        this.mealsList = meals;
        mealAdapter.setItems(meals);
        rvMeals.setVisibility(View.VISIBLE);
    }

    @Override
    public void showIngredients(List<Ingredient> ingredients) {
        hideEmptyState();
        this.ingredientsList = ingredients;
        ingredientAdapter.updateItems(ingredients);
        rvIngredients.setVisibility(View.VISIBLE);
    }

    @Override
    public void showAreas(List<Area> areas) {
        hideEmptyState();
        this.areasList = areas;
        areaAdapter.updateItems(areas);
        rvCountry.setVisibility(View.VISIBLE);
    }

    @Override
    public void clearMeals() {
        mealsList.clear();
        mealAdapter.setItems(mealsList);
        rvMeals.setVisibility(View.GONE);
    }

    @Override
    public void showEmptyMeals() {
        rvMeals.setVisibility(View.GONE);
        hideSearchPlaceholder();
        showEmptyState("No meals found", "Try searching with different keywords");
    }

    @Override
    public void showEmptyIngredients() {
        rvIngredients.setVisibility(View.GONE);
        showEmptyState("No ingredients found", "Try searching with different keywords");
    }

    @Override
    public void showEmptyAreas() {
        rvCountry.setVisibility(View.GONE);
        showEmptyState("No countries found", "Try searching with different keywords");
    }

    @Override
    public void showError(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showSearchPlaceholder() {
        searchPlaceholderContainer.setVisibility(View.VISIBLE);
        rvMeals.setVisibility(View.GONE);
        hideEmptyState();
    }

    @Override
    public void hideSearchPlaceholder() {
        searchPlaceholderContainer.setVisibility(View.GONE);
    }

    @Override
    public int getCurrentTab() {
        return currentTab;
    }

    private void showEmptyState(String title, String message) {
        if (emptyStateContainer != null) {
            emptyStateContainer.setVisibility(View.VISIBLE);
            lottieEmptyState.playAnimation();
            if (tvEmptyTitle != null) {
                tvEmptyTitle.setText(title);
            }
            if (tvEmptyMessage != null) {
                tvEmptyMessage.setText(message);
            }
        }
    }

    @Override
    public void hideEmptyState() {
        if (emptyStateContainer != null) {
            emptyStateContainer.setVisibility(View.GONE);
            lottieEmptyState.cancelAnimation();
        }
    }

    // ==================== Click Listeners ====================

    @Override
    public void onMealClick(Meal meal, int position) {
        presenter.onMealClicked(meal);
        Toast.makeText(requireContext(), "Meal: " + meal.getName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFavoriteClick(Meal meal, int position, boolean isFavorite) {
        if (isFavorite) {
            Toast.makeText(requireContext(), meal.getName() + " added to favorites", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(), meal.getName() + " removed from favorites", Toast.LENGTH_SHORT).show();
        }
    }

    private void onIngredientClicked(Ingredient ingredient) {
        presenter.onIngredientClicked(ingredient);
        Toast.makeText(requireContext(), "Ingredient: " + ingredient.getName(), Toast.LENGTH_SHORT).show();
    }

    private void onAreaClicked(Area area) {
        presenter.onAreaClicked(area);
        Toast.makeText(requireContext(), "Country: " + area.getName(), Toast.LENGTH_SHORT).show();
    }

    // ==================== Voice Search ====================

    private void startVoiceSearch() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Search for meals...");
        try {
            startActivityForResult(intent, VOICE_SEARCH_REQUEST_CODE);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Voice search not supported", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VOICE_SEARCH_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results != null && !results.isEmpty()) {
                String voiceText = results.get(0);
                etSearch.setText(voiceText);
                etSearch.setSelection(voiceText.length());
                // Icons will be updated automatically via TextWatcher
            }
        }
    }

    // ==================== Lifecycle ====================

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        presenter.detachView();
        presenter.dispose();
    }
}
