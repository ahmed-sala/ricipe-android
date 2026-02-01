package com.example.recipe_android_project.features.search.domain.model;

import java.io.Serializable;

public class FilterResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private String name;
    private String thumbnailUrl;

    public FilterResult() {
    }

    // ==================== HELPER METHODS ====================

    public boolean isValid() {
        return id > 0 && name != null && !name.trim().isEmpty();
    }

    public boolean hasThumbnail() {
        return thumbnailUrl != null && !thumbnailUrl.trim().isEmpty();
    }

    // ==================== GETTERS & SETTERS ====================

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    // ==================== OVERRIDES ====================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FilterResult that = (FilterResult) o;
        return id != 0 && id == that.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return "FilterResult{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
