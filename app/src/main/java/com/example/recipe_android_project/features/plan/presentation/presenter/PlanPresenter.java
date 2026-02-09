package com.example.recipe_android_project.features.plan.presentation.presenter;

import android.content.Context;

import com.example.recipe_android_project.core.helper.UserSessionManager;
import com.example.recipe_android_project.features.meal_detail.domain.model.MealPlan;
import com.example.recipe_android_project.features.plan.data.repository.MealPlanRepository;
import com.example.recipe_android_project.features.plan.domain.model.DayModel;
import com.example.recipe_android_project.features.plan.presentation.contract.PlanContract;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class PlanPresenter implements PlanContract.Presenter {

    private PlanContract.View view;
    private final MealPlanRepository repository;
    private final UserSessionManager sessionManager;
    private final CompositeDisposable disposables = new CompositeDisposable();

    private Disposable mealPlansDisposable;
    private Disposable syncDisposable;
    private Disposable removeDisposable;

    private int currentDay, currentMonth, currentYear;
    private String currentDateString;
    private MealPlan currentBreakfast;
    private MealPlan currentLunch;
    private MealPlan currentDinner;

    private boolean isFirstEmission = true;

    public PlanPresenter(Context context) {
        this.repository = new MealPlanRepository(context);
        this.sessionManager = UserSessionManager.getInstance(context);
        initializeCurrentDate();
    }


    private void initializeCurrentDate() {
        Calendar today = Calendar.getInstance();
        currentDay = today.get(Calendar.DAY_OF_MONTH);
        currentMonth = today.get(Calendar.MONTH);
        currentYear = today.get(Calendar.YEAR);
        updateCurrentDateString();
    }

    private void updateCurrentDateString() {
        currentDateString = String.format(Locale.US, "%04d-%02d-%02d",
                currentYear, currentMonth + 1, currentDay);
    }

    @Override
    public void attachView(PlanContract.View view) {
        this.view = view;
    }

    @Override
    public void detachView() {
        this.view = null;
    }

    @Override
    public void dispose() {
        cancelMealPlansSubscription();
        cancelSyncRequest();
        cancelRemoveRequest();
        disposables.clear();
    }

    private boolean isViewAttached() {
        return view != null;
    }

    private String getUserId() {
        return sessionManager.getCurrentUserIdOrNull();
    }

    @Override
    public boolean isUserLoggedIn() {
        return sessionManager.hasValidSession();
    }


    @Override
    public void onDateSelected(DayModel day) {
        if (day == null) return;
        onDateSelected(day.getDayNumber(), day.getMonth(), day.getYear());
    }

    @Override
    public void onDateSelected(int day, int month, int year) {
        this.currentDay = day;
        this.currentMonth = month;
        this.currentYear = year;
        updateCurrentDateString();

        if (isViewAttached()) {
            view.updateSelectedDateDisplay(day, month, year);
        }

        loadMealPlansForDate(currentDateString);
    }

    @Override
    public void loadMealPlansForCurrentDate() {
        loadMealPlansForDate(currentDateString);
    }

    @Override
    public void loadMealPlansForDate(String date) {
        if (!isViewAttached()) return;

        String userId = getUserId();
        if (userId == null || userId.isEmpty()) {
            showAllEmptyStates();
            return;
        }

        cancelMealPlansSubscription();

        isFirstEmission = true;
        view.showLoading();

        mealPlansDisposable = repository.getMealPlansByDate(userId, date)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        this::handleMealPlansLoaded,
                        this::handleError
                );
        disposables.add(mealPlansDisposable);

    }

    private void handleMealPlansLoaded(List<MealPlan> mealPlans) {
        if (!isViewAttached()) return;

        if (isFirstEmission) {
            view.hideLoading();
            isFirstEmission = false;
        }

        currentBreakfast = null;
        currentLunch = null;
        currentDinner = null;

        if (mealPlans == null || mealPlans.isEmpty()) {
            showAllEmptyStates();
            view.showEmptyState();
            return;
        }

        for (MealPlan plan : mealPlans) {
            String mealType = plan.getMealType();
            if (mealType == null) continue;

            switch (mealType.toLowerCase()) {
                case "breakfast":
                    currentBreakfast = plan;
                    break;
                case "lunch":
                    currentLunch = plan;
                    break;
                case "dinner":
                    currentDinner = plan;
                    break;
            }
        }

        updateBreakfastUI();
        updateLunchUI();
        updateDinnerUI();

        view.showMealPlansForDate(mealPlans);
    }

    private void updateBreakfastUI() {
        if (!isViewAttached()) return;

        if (currentBreakfast != null) {
            view.showBreakfast(currentBreakfast);
        } else {
            view.showBreakfastEmpty();
        }
    }

    private void updateLunchUI() {
        if (!isViewAttached()) return;

        if (currentLunch != null) {
            view.showLunch(currentLunch);
        } else {
            view.showLunchEmpty();
        }
    }

    private void updateDinnerUI() {
        if (!isViewAttached()) return;

        if (currentDinner != null) {
            view.showDinner(currentDinner);
        } else {
            view.showDinnerEmpty();
        }
    }

    private void showAllEmptyStates() {
        if (!isViewAttached()) return;

        view.hideLoading();
        view.showBreakfastEmpty();
        view.showLunchEmpty();
        view.showDinnerEmpty();
    }

    private void handleError(Throwable error) {
        if (!isViewAttached()) return;

        view.hideLoading();
        showAllEmptyStates();

        String message = getErrorMessage(error, "Failed to load meal plans");
        view.showError(message);
    }

    @Override
    public void onBreakfastClicked() {
        if (currentBreakfast != null && currentBreakfast.getMealId() != null) {
            if (isViewAttached()) {
                view.navigateToMealDetail(currentBreakfast.getMealId());
            }
        }
    }
    @Override
    public void onLunchClicked() {
        if (currentLunch != null && currentLunch.getMealId() != null) {
            if (isViewAttached()) {
                view.navigateToMealDetail(currentLunch.getMealId());
            }
        }
    }
    @Override
    public void onDinnerClicked() {
        if (currentDinner != null && currentDinner.getMealId() != null) {
            if (isViewAttached()) {
                view.navigateToMealDetail(currentDinner.getMealId());
            }
        }
    }
    @Override
    public void confirmRemoveMeal(String mealType) {
        if (!isViewAttached() || mealType == null) return;

        String userId = getUserId();
        if (userId == null || userId.isEmpty()) {
            view.showLoginRequired();
            return;
        }

        cancelRemoveRequest();

        view.showMealLoading(mealType);

        removeDisposable = repository.removeMealPlan(userId, currentDateString, mealType.toLowerCase())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> {
                            if (isViewAttached()) {
                                view.hideMealLoading(mealType);
                            }
                        },
                        error -> {
                            if (isViewAttached()) {
                                view.hideMealLoading(mealType);
                                view.showError(getErrorMessage(error, "Failed to remove meal"));
                            }
                        }
                );
        disposables.add(removeDisposable);
    }
    @Override
    public void onAddBreakfastClicked() {
        if (!isViewAttached()) return;

        if (!isUserLoggedIn()) {
            view.showLoginRequired();
            return;
        }

        view.navigateToAddMeal(currentDateString, "breakfast");
    }
    @Override
    public void onAddLunchClicked() {
        if (!isViewAttached()) return;

        if (!isUserLoggedIn()) {
            view.showLoginRequired();
            return;
        }
        view.navigateToAddMeal(currentDateString, "lunch");
    }
    @Override
    public void onAddDinnerClicked() {
        if (!isViewAttached()) return;

        if (!isUserLoggedIn()) {
            view.showLoginRequired();
            return;
        }

        view.navigateToAddMeal(currentDateString, "dinner");
    }
    @Override
    public String getCurrentDate() {
        return currentDateString;
    }
    public MealPlan getCurrentBreakfast() {
        return currentBreakfast;
    }

    public MealPlan getCurrentLunch() {
        return currentLunch;
    }

    public MealPlan getCurrentDinner() {
        return currentDinner;
    }

    private String getErrorMessage(Throwable throwable, String defaultMessage) {
        if (throwable == null) {
            return defaultMessage;
        }

        String message = throwable.getMessage();
        if (message == null || message.isEmpty()) {
            return defaultMessage;
        }

        if (throwable instanceof java.net.UnknownHostException) {
            return "No internet connection";
        } else if (throwable instanceof java.net.SocketTimeoutException) {
            return "Connection timed out";
        } else if (throwable instanceof java.io.IOException) {
            return "Network error occurred";
        }

        return message;
    }

    private void cancelMealPlansSubscription() {
        if (mealPlansDisposable != null && !mealPlansDisposable.isDisposed()) {
            mealPlansDisposable.dispose();
            mealPlansDisposable = null;
        }
    }

    private void cancelSyncRequest() {
        if (syncDisposable != null && !syncDisposable.isDisposed()) {
            syncDisposable.dispose();
            syncDisposable = null;
        }
    }

    private void cancelRemoveRequest() {
        if (removeDisposable != null && !removeDisposable.isDisposed()) {
            removeDisposable.dispose();
            removeDisposable = null;
        }
    }
}
