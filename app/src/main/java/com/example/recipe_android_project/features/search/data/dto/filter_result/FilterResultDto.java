package com.example.recipe_android_project.features.search.data.dto.filter_result;

import androidx.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class FilterResultDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @SerializedName("idMeal")
    @Expose
    @Nullable
    private String idMeal;

    @SerializedName("strMeal")
    @Expose
    @Nullable
    private String strMeal;

    @SerializedName("strMealThumb")
    @Expose
    @Nullable
    private String strMealThumb;

    public FilterResultDto() {
    }

    @Nullable
    public String getIdMeal() {
        return idMeal;
    }

    public void setIdMeal(@Nullable String idMeal) {
        this.idMeal = idMeal;
    }

    @Nullable
    public String getStrMeal() {
        return strMeal;
    }

    public void setStrMeal(@Nullable String strMeal) {
        this.strMeal = strMeal;
    }

    @Nullable
    public String getStrMealThumb() {
        return strMealThumb;
    }

    public void setStrMealThumb(@Nullable String strMealThumb) {
        this.strMealThumb = strMealThumb;
    }
}
