package com.example.recipe_android_project.features.search.data.dto.ingradient;

import androidx.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class IngredientsResponseDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @SerializedName("meals")
    @Expose
    @Nullable
    private List<IngredientDto> meals;

    public IngredientsResponseDto() {
    }

    @Nullable
    public List<IngredientDto> getMeals() {
        return meals;
    }

    public void setMeals(@Nullable List<IngredientDto> meals) {
        this.meals = meals;
    }
}
