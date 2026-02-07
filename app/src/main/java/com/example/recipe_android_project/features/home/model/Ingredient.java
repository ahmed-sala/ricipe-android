package com.example.recipe_android_project.features.home.model;

import java.io.Serializable;

public class Ingredient implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private String measure;

    public Ingredient() {
    }

    public Ingredient(String name, String measure) {
        this.name = name;
        this.measure = measure;
    }


    public String getFormatted() {
        if (measure == null || measure.trim().isEmpty()) {
            return name != null ? name : "";
        }
        return measure.trim() + " " + (name != null ? name.trim() : "");
    }

    public String getImageUrl() {
        if (name == null || name.isEmpty()) {
            return "";
        }
        return "https://www.themealdb.com/images/ingredients/" + name + ".png";
    }

    public String getSmallImageUrl() {
        if (name == null || name.isEmpty()) {
            return "";
        }
        return "https://www.themealdb.com/images/ingredients/" + name + "-Small.png";
    }

    public boolean isValid() {
        return name != null && !name.trim().isEmpty();
    }

    // ==================== GETTERS & SETTERS ====================

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMeasure() {
        return measure;
    }

    public void setMeasure(String measure) {
        this.measure = measure;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ingredient that = (Ingredient) o;
        return name != null && name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    @Override
    public String toString() {
        return getFormatted();
    }
}
