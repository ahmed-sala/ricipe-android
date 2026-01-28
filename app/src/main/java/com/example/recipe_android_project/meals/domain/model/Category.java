package com.example.recipe_android_project.meals.domain.model;

import java.io.Serializable;

public class Category implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private String thumbnailUrl;
    private String description;
    private long createdAt;

    public Category() {
        this.createdAt = System.currentTimeMillis();
    }

    // ==================== HELPER METHODS ====================

    public boolean hasDescription() {
        return description != null && !description.isEmpty();
    }

    public String getShortDescription(int maxLength) {
        if (description == null || description.isEmpty()) {
            return "";
        }
        if (description.length() <= maxLength) {
            return description;
        }
        return description.substring(0, maxLength) + "...";
    }

    // ==================== GETTERS & SETTERS ====================

    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return id != null && id.equals(category.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Category{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
