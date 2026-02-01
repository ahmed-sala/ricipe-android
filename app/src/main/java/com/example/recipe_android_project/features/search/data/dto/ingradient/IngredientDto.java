package com.example.recipe_android_project.features.search.data.dto.ingradient;

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

    @SerializedName("strThumb")
    @Expose
    @Nullable
    private String strThumb;

    @SerializedName("strType")
    @Expose
    @Nullable
    private String strType;

    public IngredientDto() {
    }

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
    public String getStrThumb() {
        return strThumb;
    }

    public void setStrThumb(@Nullable String strThumb) {
        this.strThumb = strThumb;
    }

    @Nullable
    public String getStrType() {
        return strType;
    }

    public void setStrType(@Nullable String strType) {
        this.strType = strType;
    }
}
