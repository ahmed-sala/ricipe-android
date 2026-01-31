package com.example.recipe_android_project.features.home.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Meal implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private String alternateName;
    private String category;
    private String area;
    private String instructions;
    private String thumbnailUrl;
    private String tags;
    private String youtubeUrl;
    private String sourceUrl;
    private String imageSource;
    private String creativeCommonsConfirmed;
    private String dateModified;
    private List<Ingredient> ingredients;
    private boolean isFavorite;
    private long createdAt;

    public Meal() {
        this.ingredients = new ArrayList<>();
        this.createdAt = System.currentTimeMillis();
    }

    // ==================== HELPER METHODS ====================

    public List<String> getTagsList() {
        List<String> tagList = new ArrayList<>();
        if (tags != null && !tags.isEmpty()) {
            String[] tagArray = tags.split(",");
            for (String tag : tagArray) {
                if (tag != null && !tag.trim().isEmpty()) {
                    tagList.add(tag.trim());
                }
            }
        }
        return tagList;
    }

    public boolean hasIngredients() {
        return ingredients != null && !ingredients.isEmpty();
    }

    public int getIngredientsCount() {
        return ingredients != null ? ingredients.size() : 0;
    }

    public boolean hasYoutubeVideo() {
        return youtubeUrl != null && !youtubeUrl.isEmpty();
    }

    public boolean hasSource() {
        return sourceUrl != null && !sourceUrl.isEmpty();
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

    public String getAlternateName() {
        return alternateName;
    }

    public void setAlternateName(String alternateName) {
        this.alternateName = alternateName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getYoutubeUrl() {
        return youtubeUrl;
    }

    public void setYoutubeUrl(String youtubeUrl) {
        this.youtubeUrl = youtubeUrl;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public String getImageSource() {
        return imageSource;
    }

    public void setImageSource(String imageSource) {
        this.imageSource = imageSource;
    }

    public String getCreativeCommonsConfirmed() {
        return creativeCommonsConfirmed;
    }

    public void setCreativeCommonsConfirmed(String creativeCommonsConfirmed) {
        this.creativeCommonsConfirmed = creativeCommonsConfirmed;
    }

    public String getDateModified() {
        return dateModified;
    }

    public void setDateModified(String dateModified) {
        this.dateModified = dateModified;
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
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
        Meal meal = (Meal) o;
        return id != null && id.equals(meal.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Meal{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", area='" + area + '\'' +
                '}';
    }
}
