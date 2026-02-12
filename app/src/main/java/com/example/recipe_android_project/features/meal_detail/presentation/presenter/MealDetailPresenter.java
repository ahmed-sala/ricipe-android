package com.example.recipe_android_project.features.meal_detail.presentation.presenter;

import android.content.Context;

import com.example.recipe_android_project.core.utils.InstructionParser;
import com.example.recipe_android_project.features.home.model.Meal;
import com.example.recipe_android_project.features.meal_detail.data.repository.MealDetailRepository;
import com.example.recipe_android_project.features.meal_detail.domain.model.InstructionStep;
import com.example.recipe_android_project.features.meal_detail.domain.model.MealPlan;
import com.example.recipe_android_project.features.meal_detail.presentation.contract.MealDetailContract;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MealDetailPresenter implements MealDetailContract.Presenter {

    private MealDetailContract.View view;

    private final MealDetailRepository repository;
    private final CompositeDisposable disposables = new CompositeDisposable();

    private Disposable mealDetailDisposable;
    private Disposable favoriteDisposable;
    private Disposable mealPlanDisposable;
    private Disposable checkPlanDisposable;

    private Meal currentMeal;
    private boolean isFavorite = false;
    private MealPlan existingPlan = null;

    public MealDetailPresenter(Context context) {
        this.repository = new MealDetailRepository(context);
    }


    public void attachView(MealDetailContract.View view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    private boolean isViewAttached() {
        return view != null;
    }


    @Override
    public void loadMealDetails(String mealId) {
        if (!isViewAttached()) return;

        if (mealId == null || mealId.isEmpty()) {
            view.showError("Invalid meal ID");
            return;
        }

        cancelMealDetailRequest();
        view.showScreenLoading();

        mealDetailDisposable = repository
                .getMealByIdWithFavoriteStatus(mealId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        meal -> {
                            if (isViewAttached()) {
                                currentMeal = meal;
                                isFavorite = meal.isFavorite();

                                view.hideScreenLoading();
                                view.showMealDetails(meal);
                                view.updateFavoriteStatus(isFavorite);

                                if (meal.isOffline()) {
                                    view.showOfflineBanner();
                                }

                                if (meal.isLimitedData()) {
                                    view.showIngredients(new ArrayList<>());
                                    view.showLimitedDataMessage();
                                } else if (meal.hasIngredients()) {
                                    view.showIngredients(meal.getIngredients());
                                }

                                if (meal.isLimitedData()
                                        || meal.getInstructions() == null
                                        || meal.getInstructions().isEmpty()) {
                                    view.showInstructions(new ArrayList<>());
                                } else {
                                    List<InstructionStep> instructions =
                                            InstructionParser.parseInstructions(
                                                    meal.getInstructions());
                                    view.showInstructions(instructions);
                                }

                                if (!meal.isLimitedData()
                                        && meal.hasYoutubeVideo()) {
                                    view.showYoutubeVideo(meal.getYoutubeUrl());
                                } else {
                                    view.hideYoutubeVideo();
                                }
                            }
                        },
                        throwable -> {
                            if (isViewAttached()) {
                                view.hideScreenLoading();
                                view.showError(
                                        getErrorMessage(throwable,
                                                "Failed to load meal details"));
                            }
                        }
                );
        disposables.add(mealDetailDisposable);
    }


    @Override
    public void onFavoriteClicked() {
        if (!isViewAttached() || currentMeal == null) return;

        if (!isUserLoggedIn()) {
            view.showLoginRequired();
            return;
        }

        if (isFavorite) {
            view.showRemoveFavoriteConfirmation(currentMeal);
        } else {
            addToFavorites();
        }
    }

    @Override
    public void addToFavorites() {
        if (!isViewAttached() || currentMeal == null) return;

        if (!isUserLoggedIn()) {
            view.showLoginRequired();
            return;
        }

        cancelFavoriteRequest();

        view.showFavoriteLoading();

        favoriteDisposable = repository.addToFavorites(currentMeal)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> {
                            if (isViewAttached()) {
                                isFavorite = true;
                                currentMeal.setFavorite(true);

                                view.hideFavoriteLoading();
                                view.updateFavoriteStatus(true);
                                view.showFavoriteSuccess(true);
                            }
                        },
                        throwable -> {
                            if (isViewAttached()) {
                                view.hideFavoriteLoading();
                                view.showFavoriteError(
                                        getErrorMessage(throwable,
                                                "Failed to add to favorites"));
                            }
                        }
                );
        disposables.add(favoriteDisposable);
    }

    @Override
    public void removeFromFavorites() {
        if (!isViewAttached() || currentMeal == null) return;

        if (!isUserLoggedIn()) {
            view.showLoginRequired();
            return;
        }

        cancelFavoriteRequest();

        view.showFavoriteLoading();

        favoriteDisposable = repository
                .removeFromFavorites(currentMeal)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> {
                            if (isViewAttached()) {
                                isFavorite = false;
                                currentMeal.setFavorite(false);

                                view.hideFavoriteLoading();
                                view.updateFavoriteStatus(false);
                                view.showFavoriteSuccess(false);
                            }
                        },
                        throwable -> {
                            if (isViewAttached()) {
                                view.hideFavoriteLoading();
                                view.showFavoriteError(
                                        getErrorMessage(throwable,
                                                "Failed to remove from favorites"));
                            }
                        }
                );
        disposables.add(favoriteDisposable);
    }

    @Override
    public void confirmRemoveFromFavorites() {
        removeFromFavorites();
    }

    @Override
    public boolean isUserLoggedIn() {
        return repository.isUserAuthenticated();
    }



    @Override
    public void onAddToWeeklyPlanClicked() {
        if (!isViewAttached() || currentMeal == null) return;

        if (!isUserLoggedIn()) {
            view.showLoginRequired();
            return;
        }

        view.showAddToPlanDialog(currentMeal);
    }

    @Override
    public void checkMealPlanExists(String date, String mealType) {
        if (!isViewAttached()
                || date == null || mealType == null) return;

        cancelCheckPlanRequest();

        checkPlanDisposable = repository
                .getMealPlan(date, mealType)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        mealPlan -> {
                            if (isViewAttached()) {
                                existingPlan = mealPlan;
                                view.updatePlanExistsWarning(
                                        true,
                                        mealPlan.getMealName());
                            }
                        },
                        throwable -> {
                            if (isViewAttached()) {
                                existingPlan = null;
                                view.updatePlanExistsWarning(
                                        false, null);
                            }
                        }
                );
        disposables.add(checkPlanDisposable);
    }

    @Override
    public void addToPlan(String date, String mealType) {
        if (!isViewAttached() || currentMeal == null) return;

        if (!isUserLoggedIn()) {
            view.showLoginRequired();
            return;
        }

        if (date == null || date.isEmpty()
                || mealType == null || mealType.isEmpty()) {
            view.showPlanError(
                    "Please select date and meal type");
            return;
        }

        if (existingPlan != null) {
            view.showPlanExistsDialog(
                    existingPlan, currentMeal.getName());
            return;
        }

        performAddToPlan(date, mealType, false);
    }

    @Override
    public void replacePlan(String date, String mealType) {
        if (!isViewAttached() || currentMeal == null) return;

        performAddToPlan(date, mealType, true);
    }

    private void performAddToPlan(String date,
                                  String mealType,
                                  boolean isReplacing) {
        cancelMealPlanRequest();

        view.showPlanLoading();

        mealPlanDisposable = repository
                .addMealToPlan(currentMeal, date, mealType)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> {
                            if (isViewAttached()) {
                                view.hidePlanLoading();
                                existingPlan = null;

                                if (isReplacing) {
                                    view.showPlanUpdatedSuccess(
                                            mealType, date);
                                } else {
                                    view.showPlanAddedSuccess(
                                            mealType, date);
                                }
                            }
                        },
                        throwable -> {
                            if (isViewAttached()) {
                                view.hidePlanLoading();
                                view.showPlanError(
                                        getErrorMessage(throwable,
                                                "Failed to add meal to plan"));
                            }
                        }
                );
        disposables.add(mealPlanDisposable);
    }

    @Override
    public void confirmRemovePlan(String date, String mealType) {
        if (!isViewAttached()) return;

        cancelMealPlanRequest();

        view.showPlanLoading();

        mealPlanDisposable = repository
                .removeMealPlan(date, mealType)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> {
                            if (isViewAttached()) {
                                view.hidePlanLoading();
                                existingPlan = null;
                                view.showPlanRemovedSuccess(
                                        mealType, date);
                            }
                        },
                        throwable -> {
                            if (isViewAttached()) {
                                view.hidePlanLoading();
                                view.showPlanError(
                                        getErrorMessage(throwable,
                                                "Failed to remove meal plan"));
                            }
                        }
                );
        disposables.add(mealPlanDisposable);
    }


    @Override
    public void onBackClicked() {
        if (isViewAttached()) {
            view.navigateBack();
        }
    }


    private String getErrorMessage(Throwable throwable,
                                   String defaultMessage) {
        if (throwable == null) return defaultMessage;

        String message = throwable.getMessage();
        if (message == null || message.isEmpty())
            return defaultMessage;

        if (throwable instanceof java.net.UnknownHostException) {
            return "No internet connection";
        } else if (throwable instanceof
                java.net.SocketTimeoutException) {
            return "Connection timed out";
        } else if (throwable instanceof java.io.IOException) {
            return "Network error occurred";
        }

        return message;
    }



    private void cancelMealDetailRequest() {
        if (mealDetailDisposable != null
                && !mealDetailDisposable.isDisposed()) {
            mealDetailDisposable.dispose();
        }
    }

    private void cancelFavoriteRequest() {
        if (favoriteDisposable != null
                && !favoriteDisposable.isDisposed()) {
            favoriteDisposable.dispose();
        }
    }

    private void cancelMealPlanRequest() {
        if (mealPlanDisposable != null
                && !mealPlanDisposable.isDisposed()) {
            mealPlanDisposable.dispose();
        }
    }

    private void cancelCheckPlanRequest() {
        if (checkPlanDisposable != null
                && !checkPlanDisposable.isDisposed()) {
            checkPlanDisposable.dispose();
        }
    }

    private void cancelAllRequests() {
        cancelMealDetailRequest();
        cancelFavoriteRequest();
        cancelMealPlanRequest();
        cancelCheckPlanRequest();
    }



    @Override
    public void detach() {
        dispose();
        detachView();
    }

    public void dispose() {
        cancelAllRequests();
        disposables.clear();
    }
}
