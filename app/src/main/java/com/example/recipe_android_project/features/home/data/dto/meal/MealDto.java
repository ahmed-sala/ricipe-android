package com.example.recipe_android_project.features.home.data.dto.meal;
import androidx.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class MealDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @SerializedName("idMeal")
    @Expose
    @Nullable
    private String idMeal;

    @SerializedName("strMeal")
    @Expose
    @Nullable
    private String strMeal;

    @SerializedName("strMealAlternate")
    @Expose
    @Nullable
    private String strMealAlternate;

    @SerializedName("strCategory")
    @Expose
    @Nullable
    private String strCategory;

    @SerializedName("strArea")
    @Expose
    @Nullable
    private String strArea;

    @SerializedName("strInstructions")
    @Expose
    @Nullable
    private String strInstructions;

    @SerializedName("strMealThumb")
    @Expose
    @Nullable
    private String strMealThumb;

    @SerializedName("strTags")
    @Expose
    @Nullable
    private String strTags;

    @SerializedName("strYoutube")
    @Expose
    @Nullable
    private String strYoutube;

    @SerializedName("strSource")
    @Expose
    @Nullable
    private String strSource;

    @SerializedName("strImageSource")
    @Expose
    @Nullable
    private String strImageSource;

    @SerializedName("strCreativeCommonsConfirmed")
    @Expose
    @Nullable
    private String strCreativeCommonsConfirmed;

    @SerializedName("dateModified")
    @Expose
    @Nullable
    private String dateModified;

    // Ingredients
    @SerializedName("strIngredient1")
    @Expose
    @Nullable
    private String strIngredient1;

    @SerializedName("strIngredient2")
    @Expose
    @Nullable
    private String strIngredient2;

    @SerializedName("strIngredient3")
    @Expose
    @Nullable
    private String strIngredient3;

    @SerializedName("strIngredient4")
    @Expose
    @Nullable
    private String strIngredient4;

    @SerializedName("strIngredient5")
    @Expose
    @Nullable
    private String strIngredient5;

    @SerializedName("strIngredient6")
    @Expose
    @Nullable
    private String strIngredient6;

    @SerializedName("strIngredient7")
    @Expose
    @Nullable
    private String strIngredient7;

    @SerializedName("strIngredient8")
    @Expose
    @Nullable
    private String strIngredient8;

    @SerializedName("strIngredient9")
    @Expose
    @Nullable
    private String strIngredient9;

    @SerializedName("strIngredient10")
    @Expose
    @Nullable
    private String strIngredient10;

    @SerializedName("strIngredient11")
    @Expose
    @Nullable
    private String strIngredient11;

    @SerializedName("strIngredient12")
    @Expose
    @Nullable
    private String strIngredient12;

    @SerializedName("strIngredient13")
    @Expose
    @Nullable
    private String strIngredient13;

    @SerializedName("strIngredient14")
    @Expose
    @Nullable
    private String strIngredient14;

    @SerializedName("strIngredient15")
    @Expose
    @Nullable
    private String strIngredient15;

    @SerializedName("strIngredient16")
    @Expose
    @Nullable
    private String strIngredient16;

    @SerializedName("strIngredient17")
    @Expose
    @Nullable
    private String strIngredient17;

    @SerializedName("strIngredient18")
    @Expose
    @Nullable
    private String strIngredient18;

    @SerializedName("strIngredient19")
    @Expose
    @Nullable
    private String strIngredient19;

    @SerializedName("strIngredient20")
    @Expose
    @Nullable
    private String strIngredient20;

    // Measures
    @SerializedName("strMeasure1")
    @Expose
    @Nullable
    private String strMeasure1;

    @SerializedName("strMeasure2")
    @Expose
    @Nullable
    private String strMeasure2;

    @SerializedName("strMeasure3")
    @Expose
    @Nullable
    private String strMeasure3;

    @SerializedName("strMeasure4")
    @Expose
    @Nullable
    private String strMeasure4;

    @SerializedName("strMeasure5")
    @Expose
    @Nullable
    private String strMeasure5;

    @SerializedName("strMeasure6")
    @Expose
    @Nullable
    private String strMeasure6;

    @SerializedName("strMeasure7")
    @Expose
    @Nullable
    private String strMeasure7;

    @SerializedName("strMeasure8")
    @Expose
    @Nullable
    private String strMeasure8;

    @SerializedName("strMeasure9")
    @Expose
    @Nullable
    private String strMeasure9;

    @SerializedName("strMeasure10")
    @Expose
    @Nullable
    private String strMeasure10;

    @SerializedName("strMeasure11")
    @Expose
    @Nullable
    private String strMeasure11;

    @SerializedName("strMeasure12")
    @Expose
    @Nullable
    private String strMeasure12;

    @SerializedName("strMeasure13")
    @Expose
    @Nullable
    private String strMeasure13;

    @SerializedName("strMeasure14")
    @Expose
    @Nullable
    private String strMeasure14;

    @SerializedName("strMeasure15")
    @Expose
    @Nullable
    private String strMeasure15;

    @SerializedName("strMeasure16")
    @Expose
    @Nullable
    private String strMeasure16;

    @SerializedName("strMeasure17")
    @Expose
    @Nullable
    private String strMeasure17;

    @SerializedName("strMeasure18")
    @Expose
    @Nullable
    private String strMeasure18;

    @SerializedName("strMeasure19")
    @Expose
    @Nullable
    private String strMeasure19;

    @SerializedName("strMeasure20")
    @Expose
    @Nullable
    private String strMeasure20;

    public MealDto() {
    }

    // ==================== GETTERS & SETTERS ====================

    @Nullable public String getIdMeal() { return idMeal; }
    public void setIdMeal(@Nullable String idMeal) { this.idMeal = idMeal; }

    @Nullable public String getStrMeal() { return strMeal; }
    public void setStrMeal(@Nullable String strMeal) { this.strMeal = strMeal; }

    @Nullable public String getStrMealAlternate() { return strMealAlternate; }
    public void setStrMealAlternate(@Nullable String strMealAlternate) { this.strMealAlternate = strMealAlternate; }

    @Nullable public String getStrCategory() { return strCategory; }
    public void setStrCategory(@Nullable String strCategory) { this.strCategory = strCategory; }

    @Nullable public String getStrArea() { return strArea; }
    public void setStrArea(@Nullable String strArea) { this.strArea = strArea; }

    @Nullable public String getStrInstructions() { return strInstructions; }
    public void setStrInstructions(@Nullable String strInstructions) { this.strInstructions = strInstructions; }

    @Nullable public String getStrMealThumb() { return strMealThumb; }
    public void setStrMealThumb(@Nullable String strMealThumb) { this.strMealThumb = strMealThumb; }

    @Nullable public String getStrTags() { return strTags; }
    public void setStrTags(@Nullable String strTags) { this.strTags = strTags; }

    @Nullable public String getStrYoutube() { return strYoutube; }
    public void setStrYoutube(@Nullable String strYoutube) { this.strYoutube = strYoutube; }

    @Nullable public String getStrSource() { return strSource; }
    public void setStrSource(@Nullable String strSource) { this.strSource = strSource; }

    @Nullable public String getStrImageSource() { return strImageSource; }
    public void setStrImageSource(@Nullable String strImageSource) { this.strImageSource = strImageSource; }

    @Nullable public String getStrCreativeCommonsConfirmed() { return strCreativeCommonsConfirmed; }
    public void setStrCreativeCommonsConfirmed(@Nullable String strCreativeCommonsConfirmed) { this.strCreativeCommonsConfirmed = strCreativeCommonsConfirmed; }

    @Nullable public String getDateModified() { return dateModified; }
    public void setDateModified(@Nullable String dateModified) { this.dateModified = dateModified; }

    // Ingredients
    @Nullable public String getStrIngredient1() { return strIngredient1; }
    public void setStrIngredient1(@Nullable String strIngredient1) { this.strIngredient1 = strIngredient1; }

    @Nullable public String getStrIngredient2() { return strIngredient2; }
    public void setStrIngredient2(@Nullable String strIngredient2) { this.strIngredient2 = strIngredient2; }

    @Nullable public String getStrIngredient3() { return strIngredient3; }
    public void setStrIngredient3(@Nullable String strIngredient3) { this.strIngredient3 = strIngredient3; }

    @Nullable public String getStrIngredient4() { return strIngredient4; }
    public void setStrIngredient4(@Nullable String strIngredient4) { this.strIngredient4 = strIngredient4; }

    @Nullable public String getStrIngredient5() { return strIngredient5; }
    public void setStrIngredient5(@Nullable String strIngredient5) { this.strIngredient5 = strIngredient5; }

    @Nullable public String getStrIngredient6() { return strIngredient6; }
    public void setStrIngredient6(@Nullable String strIngredient6) { this.strIngredient6 = strIngredient6; }

    @Nullable public String getStrIngredient7() { return strIngredient7; }
    public void setStrIngredient7(@Nullable String strIngredient7) { this.strIngredient7 = strIngredient7; }

    @Nullable public String getStrIngredient8() { return strIngredient8; }
    public void setStrIngredient8(@Nullable String strIngredient8) { this.strIngredient8 = strIngredient8; }

    @Nullable public String getStrIngredient9() { return strIngredient9; }
    public void setStrIngredient9(@Nullable String strIngredient9) { this.strIngredient9 = strIngredient9; }

    @Nullable public String getStrIngredient10() { return strIngredient10; }
    public void setStrIngredient10(@Nullable String strIngredient10) { this.strIngredient10 = strIngredient10; }

    @Nullable public String getStrIngredient11() { return strIngredient11; }
    public void setStrIngredient11(@Nullable String strIngredient11) { this.strIngredient11 = strIngredient11; }

    @Nullable public String getStrIngredient12() { return strIngredient12; }
    public void setStrIngredient12(@Nullable String strIngredient12) { this.strIngredient12 = strIngredient12; }

    @Nullable public String getStrIngredient13() { return strIngredient13; }
    public void setStrIngredient13(@Nullable String strIngredient13) { this.strIngredient13 = strIngredient13; }

    @Nullable public String getStrIngredient14() { return strIngredient14; }
    public void setStrIngredient14(@Nullable String strIngredient14) { this.strIngredient14 = strIngredient14; }

    @Nullable public String getStrIngredient15() { return strIngredient15; }
    public void setStrIngredient15(@Nullable String strIngredient15) { this.strIngredient15 = strIngredient15; }

    @Nullable public String getStrIngredient16() { return strIngredient16; }
    public void setStrIngredient16(@Nullable String strIngredient16) { this.strIngredient16 = strIngredient16; }

    @Nullable public String getStrIngredient17() { return strIngredient17; }
    public void setStrIngredient17(@Nullable String strIngredient17) { this.strIngredient17 = strIngredient17; }

    @Nullable public String getStrIngredient18() { return strIngredient18; }
    public void setStrIngredient18(@Nullable String strIngredient18) { this.strIngredient18 = strIngredient18; }

    @Nullable public String getStrIngredient19() { return strIngredient19; }
    public void setStrIngredient19(@Nullable String strIngredient19) { this.strIngredient19 = strIngredient19; }

    @Nullable public String getStrIngredient20() { return strIngredient20; }
    public void setStrIngredient20(@Nullable String strIngredient20) { this.strIngredient20 = strIngredient20; }

    // Measures
    @Nullable public String getStrMeasure1() { return strMeasure1; }
    public void setStrMeasure1(@Nullable String strMeasure1) { this.strMeasure1 = strMeasure1; }

    @Nullable public String getStrMeasure2() { return strMeasure2; }
    public void setStrMeasure2(@Nullable String strMeasure2) { this.strMeasure2 = strMeasure2; }

    @Nullable public String getStrMeasure3() { return strMeasure3; }
    public void setStrMeasure3(@Nullable String strMeasure3) { this.strMeasure3 = strMeasure3; }

    @Nullable public String getStrMeasure4() { return strMeasure4; }
    public void setStrMeasure4(@Nullable String strMeasure4) { this.strMeasure4 = strMeasure4; }

    @Nullable public String getStrMeasure5() { return strMeasure5; }
    public void setStrMeasure5(@Nullable String strMeasure5) { this.strMeasure5 = strMeasure5; }

    @Nullable public String getStrMeasure6() { return strMeasure6; }
    public void setStrMeasure6(@Nullable String strMeasure6) { this.strMeasure6 = strMeasure6; }

    @Nullable public String getStrMeasure7() { return strMeasure7; }
    public void setStrMeasure7(@Nullable String strMeasure7) { this.strMeasure7 = strMeasure7; }

    @Nullable public String getStrMeasure8() { return strMeasure8; }
    public void setStrMeasure8(@Nullable String strMeasure8) { this.strMeasure8 = strMeasure8; }

    @Nullable public String getStrMeasure9() { return strMeasure9; }
    public void setStrMeasure9(@Nullable String strMeasure9) { this.strMeasure9 = strMeasure9; }

    @Nullable public String getStrMeasure10() { return strMeasure10; }
    public void setStrMeasure10(@Nullable String strMeasure10) { this.strMeasure10 = strMeasure10; }

    @Nullable public String getStrMeasure11() { return strMeasure11; }
    public void setStrMeasure11(@Nullable String strMeasure11) { this.strMeasure11 = strMeasure11; }

    @Nullable public String getStrMeasure12() { return strMeasure12; }
    public void setStrMeasure12(@Nullable String strMeasure12) { this.strMeasure12 = strMeasure12; }

    @Nullable public String getStrMeasure13() { return strMeasure13; }
    public void setStrMeasure13(@Nullable String strMeasure13) { this.strMeasure13 = strMeasure13; }

    @Nullable public String getStrMeasure14() { return strMeasure14; }
    public void setStrMeasure14(@Nullable String strMeasure14) { this.strMeasure14 = strMeasure14; }

    @Nullable public String getStrMeasure15() { return strMeasure15; }
    public void setStrMeasure15(@Nullable String strMeasure15) { this.strMeasure15 = strMeasure15; }

    @Nullable public String getStrMeasure16() { return strMeasure16; }
    public void setStrMeasure16(@Nullable String strMeasure16) { this.strMeasure16 = strMeasure16; }

    @Nullable public String getStrMeasure17() { return strMeasure17; }
    public void setStrMeasure17(@Nullable String strMeasure17) { this.strMeasure17 = strMeasure17; }

    @Nullable public String getStrMeasure18() { return strMeasure18; }
    public void setStrMeasure18(@Nullable String strMeasure18) { this.strMeasure18 = strMeasure18; }

    @Nullable public String getStrMeasure19() { return strMeasure19; }
    public void setStrMeasure19(@Nullable String strMeasure19) { this.strMeasure19 = strMeasure19; }

    @Nullable public String getStrMeasure20() { return strMeasure20; }
    public void setStrMeasure20(@Nullable String strMeasure20) { this.strMeasure20 = strMeasure20; }
}
