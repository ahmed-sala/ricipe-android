package com.example.recipe_android_project.features.home.data.dto.meal;

import androidx.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class IngredientDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @SerializedName("idIngredient")
    @Expose
    @Nullable
    private String idIngredient;

    @SerializedName("strIngredient")
    @Expose
    @Nullable
    private String strIngredient;

    @SerializedName("strDescription")
    @Expose
    @Nullable
    private String strDescription;

    @SerializedName("strType")
    @Expose
    @Nullable
    private String strType;

    // For meal ingredients (measure)
    @Nullable
    private String strMeasure;

    public IngredientDto() {
    }

    public IngredientDto(@Nullable String strIngredient, @Nullable String strMeasure) {
        this.strIngredient = strIngredient;
        this.strMeasure = strMeasure;
    }

    // ==================== GETTERS & SETTERS ====================

    @Nullable
    public String getIdIngredient() {
        return idIngredient;
    }

    public void setIdIngredient(@Nullable String idIngredient) {
        this.idIngredient = idIngredient;
    }

    @Nullable
    public String getStrIngredient() {
        return strIngredient;
    }

    public void setStrIngredient(@Nullable String strIngredient) {
        this.strIngredient = strIngredient;
    }

    @Nullable
    public String getStrDescription() {
        return strDescription;
    }

    public void setStrDescription(@Nullable String strDescription) {
        this.strDescription = strDescription;
    }

    @Nullable
    public String getStrType() {
        return strType;
    }

    public void setStrType(@Nullable String strType) {
        this.strType = strType;
    }

    @Nullable
    public String getStrMeasure() {
        return strMeasure;
    }

    public void setStrMeasure(@Nullable String strMeasure) {
        this.strMeasure = strMeasure;
    }
}
