package com.example.recipe_android_project.features.meal_detail.presentation.presenter;

import com.example.recipe_android_project.core.helper.BasePresenter;
import com.example.recipe_android_project.core.utils.InstructionParser;
import com.example.recipe_android_project.features.home.model.Meal;
import com.example.recipe_android_project.features.meal_detail.data.datasource.repository.MealDetailRepository;
import com.example.recipe_android_project.features.meal_detail.domain.model.InstructionStep;
import com.example.recipe_android_project.features.meal_detail.presentation.contract.MealDetailContract;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MealDetailPresenter extends BasePresenter<MealDetailContract.View> implements MealDetailContract.Presenter {

    private final MealDetailRepository repository;
    private final CompositeDisposable disposables = new CompositeDisposable();

    private Disposable mealDetailDisposable;
    private Disposable favoriteDisposable;

    private Meal currentMeal;
    private boolean isFavorite = false;

    public MealDetailPresenter() {
        this.repository = new MealDetailRepository();
    }

    public MealDetailPresenter(MealDetailRepository repository) {
        this.repository = repository;
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

        mealDetailDisposable = repository.getMealById(mealId)
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

                                List<InstructionStep> instructions = InstructionParser.parseInstructions(meal.getInstructions());
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

        isFavorite = !isFavorite;
        currentMeal.setFavorite(isFavorite);

        getView().updateFavoriteStatus(isFavorite);
        getView().showFavoriteSuccess(isFavorite);

    }

    @Override
    public void onBackClicked() {
        if (isViewAttached()) {
            getView().navigateBack();
        }
    }

    @Override
    public void onYoutubeVideoClicked() {
    }

    @Override
    public void onAddToWeeklyPlanClicked() {
        if (!isViewAttached() || currentMeal == null) return;


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

    private void cancelAllRequests() {
        cancelMealDetailRequest();
        cancelFavoriteRequest();
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

    public Meal getCurrentMeal() {
        return currentMeal;
    }

    public boolean isFavorite() {
        return isFavorite;
    }
}
