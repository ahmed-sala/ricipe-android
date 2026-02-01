package com.example.recipe_android_project.features.search.data.dto.filter_result;

import androidx.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class FilterResultResponseDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @SerializedName("meals")
    @Expose
    @Nullable
    private List<FilterResultDto> meals;

    public FilterResultResponseDto() {
    }

    @Nullable
    public List<FilterResultDto> getMeals() {
        return meals;
    }

    public void setMeals(@Nullable List<FilterResultDto> meals) {
        this.meals = meals;
    }
}
