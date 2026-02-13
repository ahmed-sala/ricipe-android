package com.example.recipe_android_project.core.ui;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.recipe_android_project.R;
import com.example.recipe_android_project.features.home.model.Meal;
import com.example.recipe_android_project.features.meal_detail.presentation.view.DialogWeekPagerAdapter;
import com.example.recipe_android_project.features.plan.domain.model.DayModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.Calendar;
import java.util.Locale;

public class AddToPlanDialogHelper {

    public interface OnPlanActionListener {
        void onAddToPlan(String date, String mealType);
        void onCancel();
        void onDateOrMealTypeChanged(String date, String mealType);
    }

    private static final String[] MEAL_TYPE_KEYS = {"breakfast", "lunch", "dinner"};

    private Context context;
    private String[] mealTypeLabels;
    private String[] monthNames;

    private Dialog dialog;
    private DialogWeekPagerAdapter weekPagerAdapter;
    private ViewPager2 viewPagerWeeks;
    private TextView tvCurrentMonth;
    private TextView tvSelectedDate;
    private AutoCompleteTextView actvMealType;
    private LinearLayout layoutExistingPlan;
    private TextView tvExistingPlanMessage;
    private MaterialButton btnConfirmPlan;
    private ImageView btnPrevMonth;

    private int selectedDay, selectedMonth, selectedYear;
    private int todayDay, todayMonth, todayYear;
    private String selectedMealType = "";
    private String selectedDateString = "";

    private OnPlanActionListener listener;

    private boolean isUpdatingFromPageChange = false;

    private void initStringResources(Context context) {
        this.context = context;

        mealTypeLabels = new String[]{
                context.getString(R.string.meal_type_breakfast),
                context.getString(R.string.meal_type_lunch),
                context.getString(R.string.meal_type_dinner)
        };

        monthNames = new String[]{
                context.getString(R.string.month_january),
                context.getString(R.string.month_february),
                context.getString(R.string.month_march),
                context.getString(R.string.month_april),
                context.getString(R.string.month_may),
                context.getString(R.string.month_june),
                context.getString(R.string.month_july),
                context.getString(R.string.month_august),
                context.getString(R.string.month_september),
                context.getString(R.string.month_october),
                context.getString(R.string.month_november),
                context.getString(R.string.month_december)
        };
    }

    public Dialog showAddToPlanDialog(
            @NonNull Context context,
            @NonNull Meal meal,
            @NonNull OnPlanActionListener listener
    ) {
        this.listener = listener;
        initStringResources(context);

        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_add_to_plan, null);
        dialog.setContentView(view);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT
            );
        }

        Calendar today = Calendar.getInstance();
        todayDay = today.get(Calendar.DAY_OF_MONTH);
        todayMonth = today.get(Calendar.MONTH);
        todayYear = today.get(Calendar.YEAR);

        selectedDay = todayDay;
        selectedMonth = todayMonth;
        selectedYear = todayYear;
        updateSelectedDateString();

        initViews(view);
        setupMealPreview(view, context, meal);
        setupMonthNavigation(view);
        setupWeekPager(view, context);
        setupMealTypeDropdown(view, context);
        setupButtons(view);

        dialog.setCancelable(true);
        dialog.show();

        return dialog;
    }

    private void initViews(View view) {
        tvCurrentMonth = view.findViewById(R.id.tvCurrentMonth);
        tvSelectedDate = view.findViewById(R.id.tvSelectedDateDialog);
        viewPagerWeeks = view.findViewById(R.id.viewPagerWeeksDialog);
        actvMealType = view.findViewById(R.id.actvMealType);
        layoutExistingPlan = view.findViewById(R.id.layoutExistingPlan);
        tvExistingPlanMessage = view.findViewById(R.id.tvExistingPlanMessage);
        btnConfirmPlan = view.findViewById(R.id.btnConfirmPlan);
        btnPrevMonth = view.findViewById(R.id.btnPrevMonth);

        updateMonthDisplay();
        updateSelectedDateDisplay();
        updatePrevMonthButtonState();
    }

    private void setupMealPreview(View view, Context context, Meal meal) {
        ShapeableImageView imgMealPreview = view.findViewById(R.id.imgMealPreview);
        TextView tvMealPreviewName = view.findViewById(R.id.tvMealPreviewName);
        TextView tvMealPreviewCategory = view.findViewById(R.id.tvMealPreviewCategory);

        tvMealPreviewName.setText(meal.getName() != null ? meal.getName() : "");
        tvMealPreviewCategory.setText(
                meal.getCategory() != null ? meal.getCategory() : ""
        );

        if (meal.getThumbnailUrl() != null && !meal.getThumbnailUrl().isEmpty()) {
            Glide.with(context)
                    .load(meal.getThumbnailUrl())
                    .centerCrop()
                    .placeholder(R.drawable.onboarding_1)
                    .into(imgMealPreview);
        }
    }

    private void setupMonthNavigation(View view) {
        btnPrevMonth = view.findViewById(R.id.btnPrevMonth);
        ImageView btnNextMonth = view.findViewById(R.id.btnNextMonth);

        btnPrevMonth.setOnClickListener(v -> {
            if (canNavigateToPreviousMonth()) {
                navigateMonth(-1);
            }
        });

        btnNextMonth.setOnClickListener(v -> navigateMonth(1));
    }

    private boolean canNavigateToPreviousMonth() {
        Calendar cal =Calendar.getInstance();
        cal.set(selectedYear, selectedMonth, 1);
        cal.add(Calendar.MONTH, -1);

        int prevMonth = cal.get(Calendar.MONTH);
        int prevYear = cal.get(Calendar.YEAR);
        return !(prevYear < todayYear || (prevYear == todayYear && prevMonth < todayMonth));
    }

    private void updatePrevMonthButtonState() {
        if (btnPrevMonth == null) return;

        if (canNavigateToPreviousMonth()) {
            btnPrevMonth.setAlpha(1f);
            btnPrevMonth.setEnabled(true);
        } else {
            btnPrevMonth.setAlpha(0.3f);
            btnPrevMonth.setEnabled(false);
        }
    }

    private void navigateMonth(int direction) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(selectedYear, selectedMonth, 1);
        cal.add(java.util.Calendar.MONTH, direction);

        int newMonth = cal.get(java.util.Calendar.MONTH);
        int newYear = cal.get(java.util.Calendar.YEAR);

        if (newYear < todayYear || (newYear == todayYear && newMonth < todayMonth)) {
            return;
        }
        selectedMonth = newMonth;
        selectedYear = newYear;

        if (selectedMonth == todayMonth && selectedYear == todayYear) {
            selectedDay = todayDay;
        } else {
            selectedDay = 1;
        }

        updateMonthDisplay();
        updateSelectedDateString();
        updateSelectedDateDisplay();
        updatePrevMonthButtonState();

        int position = weekPagerAdapter.getPositionForDate(
                selectedDay, selectedMonth, selectedYear
        );
        viewPagerWeeks.setCurrentItem(position, true);
        weekPagerAdapter.setSelectedDate(selectedDay, selectedMonth, selectedYear);

        notifyDateOrMealTypeChanged();
    }

    private void setupWeekPager(View view, Context context) {
        weekPagerAdapter = new DialogWeekPagerAdapter(
                new DialogWeekPagerAdapter.OnDaySelectedListener() {
                    @Override
                    public void onDaySelected(DayModel day) {
                        if (day.isPast()) {
                            return;
                        }

                        selectedDay = day.getDayNumber();
                        selectedMonth = day.getMonth();
                        selectedYear = day.getYear();

                        updateSelectedDateString();
                        updateSelectedDateDisplay();
                        updateMonthDisplay();
                        updatePrevMonthButtonState();

                        notifyDateOrMealTypeChanged();
                    }

                    @Override
                    public void onWeekChanged(
                            int month, int year,
                            int dominantMonth, int dominantYear
                    ) {
                        if (!isUpdatingFromPageChange) {
                            if (dominantYear < todayYear ||
                                    (dominantYear == todayYear
                                            && dominantMonth < todayMonth)) {
                                dominantMonth = todayMonth;
                                dominantYear = todayYear;
                            }
                            selectedMonth = dominantMonth;
                            selectedYear = dominantYear;
                            updateMonthDisplay();
                            updatePrevMonthButtonState();
                        }
                    }
                });

        viewPagerWeeks.setAdapter(weekPagerAdapter);
        viewPagerWeeks.setCurrentItem(
                DialogWeekPagerAdapter.CENTER_POSITION, false
        );
        viewPagerWeeks.setOverScrollMode(View.OVER_SCROLL_NEVER);

        viewPagerWeeks.registerOnPageChangeCallback(
                new ViewPager2.OnPageChangeCallback() {
                    @Override
                    public void onPageSelected(int position) {
                        super.onPageSelected(position);

                        int minPosition = weekPagerAdapter.getMinPosition();
                        if (position < minPosition) {
                            viewPagerWeeks.setCurrentItem(minPosition, true);
                            return;
                        }

                        isUpdatingFromPageChange = true;

                        int[] dominant =
                                weekPagerAdapter.getDominantMonthYear(position);
                        int dominantMonth = dominant[0];
                        int dominantYear = dominant[1];

                        if (dominantYear < todayYear ||
                                (dominantYear == todayYear
                                        && dominantMonth < todayMonth)) {
                            dominantMonth = todayMonth;
                            dominantYear = todayYear;
                        }

                        selectedMonth = dominantMonth;
                        selectedYear = dominantYear;
                        updateMonthDisplay();
                        updatePrevMonthButtonState();

                        weekPagerAdapter.notifyWeekChanged(position);

                        isUpdatingFromPageChange = false;
                    }
                });
    }

    private void setupMealTypeDropdown(View view, Context context) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                context,
                android.R.layout.simple_dropdown_item_1line,
                mealTypeLabels
        );
        actvMealType.setAdapter(adapter);

        actvMealType.setOnItemClickListener((parent, v, position, id) -> {
            selectedMealType = MEAL_TYPE_KEYS[position];
            notifyDateOrMealTypeChanged();
        });
    }

    private void setupButtons(View view) {
        ImageView btnCloseDialog = view.findViewById(R.id.btnCloseDialog);
        MaterialButton btnCancelPlan = view.findViewById(R.id.btnCancelPlan);

        btnCloseDialog.setOnClickListener(v -> {
            dialog.dismiss();
            if (listener != null) {
                listener.onCancel();
            }
        });

        btnCancelPlan.setOnClickListener(v -> {
            dialog.dismiss();
            if (listener != null) {
                listener.onCancel();
            }
        });

        btnConfirmPlan.setOnClickListener(v -> {
            if (selectedMealType.isEmpty()) {
                actvMealType.setError(
                        context.getString(R.string.error_select_meal_type)
                );
                return;
            }

            if (isDateInPast(selectedDay, selectedMonth, selectedYear)) {
                return;
            }

            if (listener != null) {
                listener.onAddToPlan(selectedDateString, selectedMealType);
            }
        });
    }

    private boolean isDateInPast(int day, int month, int year) {
        if (year < todayYear) return true;
        if (year > todayYear) return false;
        if (month < todayMonth) return true;
        if (month > todayMonth) return false;
        return day < todayDay;
    }

    private void updateMonthDisplay() {
        if (tvCurrentMonth != null) {
            String monthYear = context.getString(
                    R.string.month_year_format,
                    monthNames[selectedMonth],
                    selectedYear
            );
            tvCurrentMonth.setText(monthYear);
        }
    }

    private void updateSelectedDateDisplay() {
        if (tvSelectedDate != null) {
            String monthName = monthNames[selectedMonth];
            String suffix = getDaySuffix(selectedDay);
            String display = context.getString(
                    R.string.date_format_display,
                    selectedDay,
                    suffix,
                    monthName,
                    selectedYear
            );
            tvSelectedDate.setText(display);
        }
    }

    private void updateSelectedDateString() {
        selectedDateString = String.format(
                Locale.US, "%04d-%02d-%02d",
                selectedYear, selectedMonth + 1, selectedDay
        );
    }

    private String getDaySuffix(int day) {
        if (day >= 11 && day <= 13) {
            return context.getString(R.string.day_suffix_th);
        }
        switch (day % 10) {
            case 1:
                return context.getString(R.string.day_suffix_st);
            case 2:
                return context.getString(R.string.day_suffix_nd);
            case 3:
                return context.getString(R.string.day_suffix_rd);
            default:
                return context.getString(R.string.day_suffix_th);
        }
    }

    private void notifyDateOrMealTypeChanged() {
        if (listener != null
                && !selectedDateString.isEmpty()
                && !selectedMealType.isEmpty()) {
            listener.onDateOrMealTypeChanged(selectedDateString, selectedMealType);
        }
    }

    public void updateExistingPlanWarning(
            boolean exists, String existingMealName
    ) {
        if (layoutExistingPlan == null) return;

        if (exists && existingMealName != null) {
            layoutExistingPlan.setVisibility(View.VISIBLE);
            tvExistingPlanMessage.setText(
                    context.getString(
                            R.string.existing_plan_message,
                            existingMealName
                    )
            );
            btnConfirmPlan.setText(context.getString(R.string.btn_replace));
        } else {
            layoutExistingPlan.setVisibility(View.GONE);
            btnConfirmPlan.setText(context.getString(R.string.btn_add_to_plan));
        }
    }

    public void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    public boolean isShowing() {
        return dialog != null && dialog.isShowing();
    }

    public String getSelectedDate() {
        return selectedDateString;
    }

    public String getSelectedMealType() {
        return selectedMealType;
    }
}
