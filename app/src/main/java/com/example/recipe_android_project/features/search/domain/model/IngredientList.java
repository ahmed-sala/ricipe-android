package com.example.recipe_android_project.features.search.domain.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class IngredientList implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<Ingredient> ingredients;

    public IngredientList() {
        this.ingredients = new ArrayList<>();
    }

    // ==================== HELPER METHODS ====================

    public int size() {
        return ingredients != null ? ingredients.size() : 0;
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public boolean hasIngredients() {
        return !isEmpty();
    }

    // ==================== GETTERS & SETTERS ====================

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = ingredients != null ? ingredients : new ArrayList<>();
    }

    @Override
    public String toString() {
        return "IngredientList{" +
                "ingredients=" + size() +
                '}';
    }
}
