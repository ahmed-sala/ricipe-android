package com.example.recipe_android_project.features.meals.data.remote.dto.category;

import androidx.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class CategoryDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @SerializedName("idCategory")
    @Expose
    @Nullable
    private String idCategory;

    @SerializedName("strCategory")
    @Expose
    @Nullable
    private String strCategory;

    @SerializedName("strCategoryThumb")
    @Expose
    @Nullable
    private String strCategoryThumb;

    @SerializedName("strCategoryDescription")
    @Expose
    @Nullable
    private String strCategoryDescription;

    public CategoryDto() {
    }

    @Nullable
    public String getIdCategory() {
        return idCategory;
    }

    public void setIdCategory(@Nullable String idCategory) {
        this.idCategory = idCategory;
    }

    @Nullable
    public String getStrCategory() {
        return strCategory;
    }

    public void setStrCategory(@Nullable String strCategory) {
        this.strCategory = strCategory;
    }

    @Nullable
    public String getStrCategoryThumb() {
        return strCategoryThumb;
    }

    public void setStrCategoryThumb(@Nullable String strCategoryThumb) {
        this.strCategoryThumb = strCategoryThumb;
    }

    @Nullable
    public String getStrCategoryDescription() {
        return strCategoryDescription;
    }

    public void setStrCategoryDescription(@Nullable String strCategoryDescription) {
        this.strCategoryDescription = strCategoryDescription;
    }
}
