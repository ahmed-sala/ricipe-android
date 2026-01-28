package com.example.recipe_android_project.meals.data.remote.dto.meal;

import androidx.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class MealResponseDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @SerializedName("meals")
    @Expose
    @Nullable
    private List<MealDto> meals;

    public MealResponseDto() {
    }

    @Nullable
    public List<MealDto> getMeals() {
        return meals;
    }

    public void setMeals(@Nullable List<MealDto> meals) {
        this.meals = meals;
    }
}
