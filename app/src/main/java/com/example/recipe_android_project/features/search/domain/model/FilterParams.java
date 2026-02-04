package com.example.recipe_android_project.features.search.domain.model;

import java.io.Serializable;

public class FilterParams implements Serializable {

    private static final long serialVersionUID = 1L;

    private String filterType;
    private String filterValue;
    private String filterTitle;

    public FilterParams() {
    }

    public FilterParams(String filterType, String filterValue, String filterTitle) {
        this.filterType = filterType;
        this.filterValue = filterValue;
        this.filterTitle = filterTitle;
    }

    public String getFilterType() {
        return filterType;
    }

    public void setFilterType(String filterType) {
        this.filterType = filterType;
    }

    public String getFilterValue() {
        return filterValue;
    }

    public void setFilterValue(String filterValue) {
        this.filterValue = filterValue;
    }

    public String getFilterTitle() {
        return filterTitle;
    }

    public void setFilterTitle(String filterTitle) {
        this.filterTitle = filterTitle;
    }

    public boolean isValid() {
        return filterType != null && !filterType.isEmpty()
                && filterValue != null && !filterValue.isEmpty();
    }

    @Override
    public String toString() {
        return "FilterParams{" +
                "filterType='" + filterType + '\'' +
                ", filterValue='" + filterValue + '\'' +
                ", filterTitle='" + filterTitle + '\'' +
                '}';
    }
}
