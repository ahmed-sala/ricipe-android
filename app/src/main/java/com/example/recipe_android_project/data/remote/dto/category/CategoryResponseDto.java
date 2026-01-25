package com.example.recipe_android_project.data.remote.dto.category;

import androidx.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class CategoryResponseDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @SerializedName("categories")
    @Expose
    @Nullable
    private List<CategoryDto> categories;

    public CategoryResponseDto() {
    }

    @Nullable
    public List<CategoryDto> getCategories() {
        return categories;
    }

    public void setCategories(@Nullable List<CategoryDto> categories) {
        this.categories = categories;
    }
}
