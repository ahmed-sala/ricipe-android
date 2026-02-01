package com.example.recipe_android_project.features.home.data.dto.area;

import androidx.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class AreaDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @SerializedName("strArea")
    @Expose
    @Nullable
    private String strArea;

    public AreaDto() {
    }

    @Nullable
    public String getStrArea() {
        return strArea;
    }

    public void setStrArea(@Nullable String strArea) {
        this.strArea = strArea;
    }
}
