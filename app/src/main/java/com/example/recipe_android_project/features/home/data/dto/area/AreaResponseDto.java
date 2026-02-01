package com.example.recipe_android_project.features.home.data.dto.area;

import androidx.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class AreaResponseDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @SerializedName("meals")
    @Expose
    @Nullable
    private List<AreaDto> meals;

    public AreaResponseDto() {
    }

    @Nullable
    public List<AreaDto> getMeals() {
        return meals;
    }

    public void setMeals(@Nullable List<AreaDto> meals) {
        this.meals = meals;
    }
}
