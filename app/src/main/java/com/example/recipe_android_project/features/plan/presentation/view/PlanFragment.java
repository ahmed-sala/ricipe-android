package com.example.recipe_android_project.features.plan.presentation.view;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.viewpager2.widget.ViewPager2;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.example.recipe_android_project.R;
import com.example.recipe_android_project.core.ui.AlertDialogHelper;
import com.example.recipe_android_project.core.ui.SnackbarHelper;
import com.example.recipe_android_project.features.dashboard.presentation.view.TabNavigator;
import com.example.recipe_android_project.features.meal_detail.domain.model.MealPlan;
import com.example.recipe_android_project.features.plan.domain.model.DayModel;
import com.example.recipe_android_project.features.plan.presentation.contract.PlanContract;
import com.example.recipe_android_project.features.plan.presentation.presenter.PlanPresenter;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class PlanFragment extends Fragment implements
        PlanContract.View,
        WeekPagerAdapter.OnDaySelectedListener {
    private Spinner spinnerMonth, spinnerYear;
    private ViewPager2 viewPagerWeeks;
    private TextView tvSelectedDate;
    private TabNavigator tabNavigator;
    private FrameLayout breakfastContainer;
    private View breakfastEmpty, breakfastMeal;
    private ShapeableImageView imgBreakfast;
    private TextView tvBreakfastCategory, tvBreakfastTitle;
    private MaterialCardView btnRemoveBreakfast;
    private LottieAnimationView lottieBreakfastLoading;
    private FrameLayout lunchContainer;
    private View lunchEmpty, lunchMeal;
    private ShapeableImageView imgLunch;
    private TextView tvLunchCategory, tvLunchTitle;
    private MaterialCardView btnRemoveLunch;
    private LottieAnimationView lottieLunchLoading;
    private FrameLayout dinnerContainer;
    private View dinnerEmpty, dinnerMeal;
    private ShapeableImageView imgDinner;
    private TextView tvDinnerCategory, tvDinnerTitle;
    private MaterialCardView btnRemoveDinner;
    private LottieAnimationView lottieDinnerLoading;
    private View loadingOverlay;
    private WeekPagerAdapter weekPagerAdapter;
    private List<String> years;
    private PlanPresenter presenter;
    private final String[] months = {"January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"};

    private int todayDay, todayMonth, todayYear;
    private boolean isNavigatingFromSpinner = false;
    private boolean isUpdatingSpinners = false;
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof TabNavigator) {
            tabNavigator = (TabNavigator) context;
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_plan, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Calendar today = Calendar.getInstance();
        todayDay = today.get(Calendar.DAY_OF_MONTH);
        todayMonth = today.get(Calendar.MONTH);
        todayYear = today.get(Calendar.YEAR);

        initViews(view);
        setupPresenter();
        setupSpinners();
        setupViewPager();
        setupMealClickListeners();

        updateSelectedDateDisplay(todayDay, todayMonth, todayYear);
        presenter.onDateSelected(todayDay, todayMonth, todayYear);
    }

    private void initViews(View view) {
        spinnerMonth = view.findViewById(R.id.spinnerMonth);
        spinnerYear = view.findViewById(R.id.spinnerYear);
        viewPagerWeeks = view.findViewById(R.id.viewPagerWeeks);
        tvSelectedDate = view.findViewById(R.id.tvSelectedDate);

        loadingOverlay = view.findViewById(R.id.loadingOverlay);

        breakfastContainer = view.findViewById(R.id.breakfastContainer);
        breakfastEmpty = view.findViewById(R.id.breakfastEmpty);
        breakfastMeal = view.findViewById(R.id.breakfastMeal);

        if (breakfastMeal != null) {
            imgBreakfast = breakfastMeal.findViewById(R.id.imgPlanMeal);
            tvBreakfastCategory = breakfastMeal.findViewById(R.id.tvPlanMealCategory);
            tvBreakfastTitle = breakfastMeal.findViewById(R.id.tvPlanMealTitle);
            btnRemoveBreakfast = breakfastMeal.findViewById(R.id.btnRemovePlan);
            lottieBreakfastLoading = breakfastMeal.findViewById(R.id.lottieImgLoading);
        }

        lunchContainer = view.findViewById(R.id.lunchContainer);
        lunchEmpty = view.findViewById(R.id.lunchEmpty);
        lunchMeal = view.findViewById(R.id.lunchMeal);

        if (lunchMeal != null) {
            imgLunch = lunchMeal.findViewById(R.id.imgPlanMeal);
            tvLunchCategory = lunchMeal.findViewById(R.id.tvPlanMealCategory);
            tvLunchTitle = lunchMeal.findViewById(R.id.tvPlanMealTitle);
            btnRemoveLunch = lunchMeal.findViewById(R.id.btnRemovePlan);
            lottieLunchLoading = lunchMeal.findViewById(R.id.lottieImgLoading);
        }

        dinnerContainer = view.findViewById(R.id.dinnerContainer);
        dinnerEmpty = view.findViewById(R.id.dinnerEmpty);
        dinnerMeal = view.findViewById(R.id.dinnerMeal);

        if (dinnerMeal != null) {
            imgDinner = dinnerMeal.findViewById(R.id.imgPlanMeal);
            tvDinnerCategory = dinnerMeal.findViewById(R.id.tvPlanMealCategory);
            tvDinnerTitle = dinnerMeal.findViewById(R.id.tvPlanMealTitle);
            btnRemoveDinner = dinnerMeal.findViewById(R.id.btnRemovePlan);
            lottieDinnerLoading = dinnerMeal.findViewById(R.id.lottieImgLoading);
        }
    }

    private void setupPresenter() {
        presenter = new PlanPresenter(requireContext());
        presenter.attachView(this);
    }

    private void setupSpinners() {
        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                months
        );
        spinnerMonth.setAdapter(monthAdapter);

        years = new ArrayList<>();
        int currentYearIndex = 0;
        for (int i = todayYear - 50; i <= todayYear + 50; i++) {
            if (i == todayYear) {
                currentYearIndex = years.size();
            }
            years.add(String.valueOf(i));
        }
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                years
        );
        spinnerYear.setAdapter(yearAdapter);
        isUpdatingSpinners = true;
        spinnerMonth.setSelection(todayMonth);
        spinnerYear.setSelection(currentYearIndex);
        isUpdatingSpinners = false;

        setupSpinnerListeners();
    }

    private void setupSpinnerListeners() {
        spinnerMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!isUpdatingSpinners) {
                    navigateToMonthFromSpinner(position, getSelectedYear());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!isUpdatingSpinners) {
                    navigateToMonthFromSpinner(spinnerMonth.getSelectedItemPosition(),
                            Integer.parseInt(years.get(position)));
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupViewPager() {
        weekPagerAdapter = new WeekPagerAdapter(this);
        viewPagerWeeks.setAdapter(weekPagerAdapter);
        viewPagerWeeks.setCurrentItem(WeekPagerAdapter.CENTER_POSITION, false);
        viewPagerWeeks.setOverScrollMode(View.OVER_SCROLL_NEVER);

        viewPagerWeeks.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                if (!isNavigatingFromSpinner) {
                    weekPagerAdapter.notifyWeekChanged(position);
                }
            }
        });
    }

    private void setupMealClickListeners() {
        if (breakfastMeal != null) {
            breakfastMeal.setOnClickListener(v -> presenter.onBreakfastClicked());
        }
        if (btnRemoveBreakfast != null) {
            btnRemoveBreakfast.setOnClickListener(v -> showRemoveConfirmation("breakfast"));
        }
        if (breakfastEmpty != null) {
            breakfastEmpty.setOnClickListener(v -> presenter.onAddBreakfastClicked());
        }

        if (lunchMeal != null) {
            lunchMeal.setOnClickListener(v -> presenter.onLunchClicked());
        }
        if (btnRemoveLunch != null) {
            btnRemoveLunch.setOnClickListener(v -> showRemoveConfirmation("lunch"));
        }
        if (lunchEmpty != null) {
            lunchEmpty.setOnClickListener(v -> presenter.onAddLunchClicked());
        }

        if (dinnerMeal != null) {
            dinnerMeal.setOnClickListener(v -> presenter.onDinnerClicked());
        }
        if (btnRemoveDinner != null) {
            btnRemoveDinner.setOnClickListener(v -> showRemoveConfirmation("dinner"));
        }
        if (dinnerEmpty != null) {
            dinnerEmpty.setOnClickListener(v -> presenter.onAddDinnerClicked());
        }
    }

    private void showRemoveConfirmation(String mealType) {
        MealPlan mealPlan = null;
        switch (mealType.toLowerCase()) {
            case "breakfast":
                mealPlan = presenter.getCurrentBreakfast();
                break;
            case "lunch":
                mealPlan = presenter.getCurrentLunch();
                break;
            case "dinner":
                mealPlan = presenter.getCurrentDinner();
                break;
        }

        String mealName = mealPlan != null ? mealPlan.getMealName() : "";
        String date = presenter.getCurrentDate();

        AlertDialogHelper.showRemovePlanDialog(
                requireContext(),
                mealName,
                date,
                mealType,
                new AlertDialogHelper.OnConfirmDialogListener() {
                    @Override
                    public void onConfirm() {
                        presenter.confirmRemoveMeal(mealType);
                    }

                    @Override
                    public void onCancel() {
                    }
                }
        );
    }
    private int getSelectedYear() {
        return Integer.parseInt(years.get(spinnerYear.getSelectedItemPosition()));
    }
    private void navigateToMonthFromSpinner(int month, int year) {
        isNavigatingFromSpinner = true;

        int day = 1;
        if (month == todayMonth && year == todayYear) {
            day = todayDay;
        }
        weekPagerAdapter.setSelectedDate(day, month, year);
        int position = weekPagerAdapter.getPositionForDate(day, month, year);
        viewPagerWeeks.setCurrentItem(position, true);
        presenter.onDateSelected(day, month, year);
        viewPagerWeeks.postDelayed(() -> isNavigatingFromSpinner = false, 300);
    }
    @Override
    public void onDaySelected(DayModel day) {
        presenter.onDateSelected(day);
    }
    @Override
    public void onWeekChanged(int month, int year, int dominantMonth, int dominantYear) {
        updateSpinners(dominantMonth, dominantYear);
    }
    @Override
    public void showLoading() {
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(View.VISIBLE);
        }
    }
    @Override
    public void hideLoading() {
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(View.GONE);
        }
    }
    @Override
    public void showMealLoading(String mealType) {
        switch (mealType.toLowerCase()) {
            case "breakfast":
                if (lottieBreakfastLoading != null) {
                    lottieBreakfastLoading.setVisibility(View.VISIBLE);
                    lottieBreakfastLoading.playAnimation();
                }
                break;
            case "lunch":
                if (lottieLunchLoading != null) {
                    lottieLunchLoading.setVisibility(View.VISIBLE);
                    lottieLunchLoading.playAnimation();
                }
                break;
            case "dinner":
                if (lottieDinnerLoading != null) {
                    lottieDinnerLoading.setVisibility(View.VISIBLE);
                    lottieDinnerLoading.playAnimation();
                }
                break;
        }
    }
    @Override
    public void hideMealLoading(String mealType) {
        switch (mealType.toLowerCase()) {
            case "breakfast":
                if (lottieBreakfastLoading != null) {
                    lottieBreakfastLoading.cancelAnimation();
                    lottieBreakfastLoading.setVisibility(View.GONE);
                }
                break;
            case "lunch":
                if (lottieLunchLoading != null) {
                    lottieLunchLoading.cancelAnimation();
                    lottieLunchLoading.setVisibility(View.GONE);
                }
                break;
            case "dinner":
                if (lottieDinnerLoading != null) {
                    lottieDinnerLoading.cancelAnimation();
                    lottieDinnerLoading.setVisibility(View.GONE);
                }
                break;
        }
    }
    @Override
    public void showBreakfast(MealPlan mealPlan) {
        if (breakfastEmpty != null) breakfastEmpty.setVisibility(View.GONE);
        if (breakfastMeal != null) breakfastMeal.setVisibility(View.VISIBLE);

        if (mealPlan != null) {
            if (tvBreakfastTitle != null) {
                tvBreakfastTitle.setText(mealPlan.getMealName());
            }
            if (tvBreakfastCategory != null) {
                tvBreakfastCategory.setText(mealPlan.getMealCategory());
            }
            if (imgBreakfast != null && mealPlan.getMealThumbnail() != null) {
                Glide.with(this)
                        .load(mealPlan.getMealThumbnail())
                        .centerCrop()
                        .placeholder(R.drawable.onboarding_1)
                        .into(imgBreakfast);
            }
        }
    }
    @Override
    public void showLunch(MealPlan mealPlan) {
        if (lunchEmpty != null) lunchEmpty.setVisibility(View.GONE);
        if (lunchMeal != null) lunchMeal.setVisibility(View.VISIBLE);

        if (mealPlan != null) {
            if (tvLunchTitle != null) {
                tvLunchTitle.setText(mealPlan.getMealName());
            }
            if (tvLunchCategory != null) {
                tvLunchCategory.setText(mealPlan.getMealCategory());
            }
            if (imgLunch != null && mealPlan.getMealThumbnail() != null) {
                Glide.with(this)
                        .load(mealPlan.getMealThumbnail())
                        .centerCrop()
                        .placeholder(R.drawable.onboarding_1)
                        .into(imgLunch);
            }
        }
    }
    @Override
    public void showDinner(MealPlan mealPlan) {
        if (dinnerEmpty != null) dinnerEmpty.setVisibility(View.GONE);
        if (dinnerMeal != null) dinnerMeal.setVisibility(View.VISIBLE);

        if (mealPlan != null) {
            if (tvDinnerTitle != null) {
                tvDinnerTitle.setText(mealPlan.getMealName());
            }
            if (tvDinnerCategory != null) {
                tvDinnerCategory.setText(mealPlan.getMealCategory());
            }
            if (imgDinner != null && mealPlan.getMealThumbnail() != null) {
                Glide.with(this)
                        .load(mealPlan.getMealThumbnail())
                        .centerCrop()
                        .placeholder(R.drawable.onboarding_1)
                        .into(imgDinner);
            }
        }
    }
    @Override
    public void showBreakfastEmpty() {
        if (breakfastMeal != null) breakfastMeal.setVisibility(View.GONE);
        if (breakfastEmpty != null) breakfastEmpty.setVisibility(View.VISIBLE);
    }

    @Override
    public void showLunchEmpty() {
        if (lunchMeal != null) lunchMeal.setVisibility(View.GONE);
        if (lunchEmpty != null) lunchEmpty.setVisibility(View.VISIBLE);
    }

    @Override
    public void showDinnerEmpty() {
        if (dinnerMeal != null) dinnerMeal.setVisibility(View.GONE);
        if (dinnerEmpty != null) dinnerEmpty.setVisibility(View.VISIBLE);
    }

    @Override
    public void showMealPlansForDate(List<MealPlan> mealPlans) {
    }

    @Override
    public void showEmptyState() {
    }

    @Override
    public void showError(String message) {
        if (getView() != null) {
            SnackbarHelper.showError(getView(), message);
        }
    }

    @Override
    public void navigateToMealDetail(String mealId) {
        if (mealId != null && !mealId.isEmpty()) {
            PlanFragmentDirections.ActionPlanFragmentToMealDetailFragment action =
                    PlanFragmentDirections.actionPlanFragmentToMealDetailFragment(mealId);
            Navigation.findNavController(requireView()).navigate(action);
        }
    }

    @Override
    public void navigateToAddMeal(String date, String mealType) {
        if (tabNavigator != null) {
            tabNavigator.navigateToSearchTab();
        }
    }

    @Override
    public void showLoginRequired() {
        if (getView() != null) {
            SnackbarHelper.showWarning(getView(), getString(R.string.login_required));
        }
    }

    @Override
    public void updateSelectedDateDisplay(int day, int month, int year) {
        String monthName = months[month];
        String suffix = getDaySuffix(day);
        tvSelectedDate.setText(day + suffix + " " + monthName + " " + year);
    }

    @Override
    public void updateSpinners(int month, int year) {
        isUpdatingSpinners = true;

        spinnerMonth.setSelection(month, false);

        int yearIndex = year - (todayYear - 50);
        if (yearIndex >= 0 && yearIndex < years.size()) {
            spinnerYear.setSelection(yearIndex, false);
        }

        spinnerMonth.post(() -> isUpdatingSpinners = false);
    }


    private String getDaySuffix(int day) {
        if (day >= 11 && day <= 13) {
            return "th";
        }
        switch (day % 10) {
            case 1: return "st";
            case 2: return "nd";
            case 3: return "rd";
            default: return "th";
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        presenter.loadMealPlansForCurrentDate();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (imgBreakfast != null) Glide.with(this).clear(imgBreakfast);
        if (imgLunch != null) Glide.with(this).clear(imgLunch);
        if (imgDinner != null) Glide.with(this).clear(imgDinner);

        if (presenter != null) {
            presenter.dispose();
            presenter.detachView();
        }
    }
    @Override
    public void onDetach() {
        super.onDetach();
        tabNavigator = null;
    }
}
