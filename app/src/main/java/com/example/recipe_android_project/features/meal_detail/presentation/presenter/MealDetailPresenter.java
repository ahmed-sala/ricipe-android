package com.example.recipe_android_project.features.meal_detail.presentation.presenter;

import android.content.Context;

import com.example.recipe_android_project.core.helper.BasePresenter;
import com.example.recipe_android_project.core.utils.InstructionParser;
import com.example.recipe_android_project.features.home.model.Meal;
import com.example.recipe_android_project.features.meal_detail.data.repository.MealDetailRepository;
import com.example.recipe_android_project.features.meal_detail.domain.model.InstructionStep;
import com.example.recipe_android_project.features.meal_detail.domain.model.MealPlan;
import com.example.recipe_android_project.features.meal_detail.presentation.contract.MealDetailContract;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MealDetailPresenter extends BasePresenter<MealDetailContract.View>
        implements MealDetailContract.Presenter {

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


    @Override
    public void loadMealDetails(String mealId) {
        if (!isViewAttached()) return;

        if (mealId == null || mealId.isEmpty()) {
            getView().showError("Invalid meal ID");
            return;
        }

        cancelMealDetailRequest();

        getView().showScreenLoading();

        mealDetailDisposable = repository.getMealByIdWithFavoriteStatus(mealId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        meal -> {
                            if (isViewAttached()) {
                                currentMeal = meal;
                                isFavorite = meal.isFavorite();

                                getView().hideScreenLoading();
                                getView().showMealDetails(meal);
                                getView().updateFavoriteStatus(isFavorite);

                                if (meal.hasIngredients()) {
                                    getView().showIngredients(meal.getIngredients());
                                }

                                List<InstructionStep> instructions =
                                        InstructionParser.parseInstructions(meal.getInstructions());
                                getView().showInstructions(instructions);

                                if (meal.hasYoutubeVideo()) {
                                    getView().showYoutubeVideo(meal.getYoutubeUrl());
                                } else {
                                    getView().hideYoutubeVideo();
                                }
                            }
                        },
                        throwable -> {
                            if (isViewAttached()) {
                                getView().hideScreenLoading();
                                getView().showError(getErrorMessage(throwable, "Failed to load meal details"));
                            }
                        }
                );
        disposables.add(mealDetailDisposable);
    }


    @Override
    public void onFavoriteClicked() {
        if (!isViewAttached() || currentMeal == null) return;

        if (!isUserLoggedIn()) {
            getView().showLoginRequired();
            return;
        }

        if (isFavorite) {
            getView().showRemoveFavoriteConfirmation(currentMeal);
        } else {
            addToFavorites();
        }
    }

    @Override
    public void addToFavorites() {
        if (!isViewAttached() || currentMeal == null) return;

        if (!isUserLoggedIn()) {
            getView().showLoginRequired();
            return;
        }

        cancelFavoriteRequest();

        getView().showFavoriteLoading();

        favoriteDisposable = repository.addToFavorites(currentMeal)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> {
                            if (isViewAttached()) {
                                isFavorite = true;
                                currentMeal.setFavorite(true);

                                getView().hideFavoriteLoading();
                                getView().updateFavoriteStatus(true);
                                getView().showFavoriteSuccess(true);
                            }
                        },
                        throwable -> {
                            if (isViewAttached()) {
                                getView().hideFavoriteLoading();
                                getView().showFavoriteError(
                                        getErrorMessage(throwable, "Failed to add to favorites")
                                );
                            }
                        }
                );
        disposables.add(favoriteDisposable);
    }

    @Override
    public void removeFromFavorites() {
        if (!isViewAttached() || currentMeal == null) return;

        if (!isUserLoggedIn()) {
            getView().showLoginRequired();
            return;
        }

        cancelFavoriteRequest();

        getView().showFavoriteLoading();

        favoriteDisposable = repository.removeFromFavorites(currentMeal)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> {
                            if (isViewAttached()) {
                                isFavorite = false;
                                currentMeal.setFavorite(false);

                                getView().hideFavoriteLoading();
                                getView().updateFavoriteStatus(false);
                                getView().showFavoriteSuccess(false);
                            }
                        },
                        throwable -> {
                            if (isViewAttached()) {
                                getView().hideFavoriteLoading();
                                getView().showFavoriteError(
                                        getErrorMessage(throwable, "Failed to remove from favorites")
                                );
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
            getView().showLoginRequired();
            return;
        }

        getView().showAddToPlanDialog(currentMeal);
    }

    @Override
    public void checkMealPlanExists(String date, String mealType) {
        if (!isViewAttached() || date == null || mealType == null) return;

        cancelCheckPlanRequest();

        checkPlanDisposable = repository.getMealPlan(date, mealType)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        mealPlan -> {
                            if (isViewAttached()) {
                                existingPlan = mealPlan;
                                getView().updatePlanExistsWarning(true, mealPlan.getMealName());
                            }
                        },
                        throwable -> {
                            if (isViewAttached()) {
                                existingPlan = null;
                                getView().updatePlanExistsWarning(false, null);
                            }
                        }
                );
        disposables.add(checkPlanDisposable);
    }

    @Override
    public void addToPlan(String date, String mealType) {
        if (!isViewAttached() || currentMeal == null) return;

        if (!isUserLoggedIn()) {
            getView().showLoginRequired();
            return;
        }

        if (date == null || date.isEmpty() || mealType == null || mealType.isEmpty()) {
            getView().showPlanError("Please select date and meal type");
            return;
        }

        if (existingPlan != null) {
            getView().showPlanExistsDialog(existingPlan, currentMeal.getName());
            return;
        }

        performAddToPlan(date, mealType, false);
    }

    @Override
    public void replacePlan(String date, String mealType) {
        if (!isViewAttached() || currentMeal == null) return;

        performAddToPlan(date, mealType, true);
    }

    private void performAddToPlan(String date, String mealType, boolean isReplacing) {
        cancelMealPlanRequest();

        getView().showPlanLoading();

        mealPlanDisposable = repository.addMealToPlan(currentMeal, date, mealType)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> {
                            if (isViewAttached()) {
                                getView().hidePlanLoading();
                                existingPlan = null;

                                if (isReplacing) {
                                    getView().showPlanUpdatedSuccess(mealType, date);
                                } else {
                                    getView().showPlanAddedSuccess(mealType, date);
                                }
                            }
                        },
                        throwable -> {
                            if (isViewAttached()) {
                                getView().hidePlanLoading();
                                getView().showPlanError(
                                        getErrorMessage(throwable, "Failed to add meal to plan")
                                );
                            }
                        }
                );
        disposables.add(mealPlanDisposable);
    }
    @Override
    public void confirmRemovePlan(String date, String mealType) {
        if (!isViewAttached()) return;

        cancelMealPlanRequest();

        getView().showPlanLoading();

        mealPlanDisposable = repository.removeMealPlan(date, mealType)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> {
                            if (isViewAttached()) {
                                getView().hidePlanLoading();
                                existingPlan = null;
                                getView().showPlanRemovedSuccess(mealType, date);
                            }
                        },
                        throwable -> {
                            if (isViewAttached()) {
                                getView().hidePlanLoading();
                                getView().showPlanError(
                                        getErrorMessage(throwable, "Failed to remove meal plan")
                                );
                            }
                        }
                );
        disposables.add(mealPlanDisposable);
    }
    @Override
    public void onBackClicked() {
        if (isViewAttached()) {
            getView().navigateBack();
        }
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

    private void cancelMealDetailRequest() {
        if (mealDetailDisposable != null && !mealDetailDisposable.isDisposed()) {
            mealDetailDisposable.dispose();
        }
    }

    private void cancelFavoriteRequest() {
        if (favoriteDisposable != null && !favoriteDisposable.isDisposed()) {
            favoriteDisposable.dispose();
        }
    }

    private void cancelMealPlanRequest() {
        if (mealPlanDisposable != null && !mealPlanDisposable.isDisposed()) {
            mealPlanDisposable.dispose();
        }
    }

    private void cancelCheckPlanRequest() {
        if (checkPlanDisposable != null && !checkPlanDisposable.isDisposed()) {
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
