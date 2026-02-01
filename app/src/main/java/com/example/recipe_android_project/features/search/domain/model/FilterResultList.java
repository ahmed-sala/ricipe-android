package com.example.recipe_android_project.features.search.domain.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FilterResultList implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<FilterResult> meals;

    public FilterResultList() {
        this.meals = new ArrayList<>();
    }

    // ==================== HELPER METHODS ====================

    public int size() {
        return meals != null ? meals.size() : 0;
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public boolean hasMeals() {
        return !isEmpty();
    }

    // ==================== GETTERS & SETTERS ====================

    public List<FilterResult> getMeals() {
        return meals;
    }

    public void setMeals(List<FilterResult> meals) {
        this.meals = meals != null ? meals : new ArrayList<>();
    }

    @Override
    public String toString() {
        return "FilterResultList{" +
                "meals=" + size() +
                '}';
    }
}
