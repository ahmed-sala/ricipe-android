package com.example.recipe_android_project.features.home.data.entities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.example.recipe_android_project.features.auth.data.entities.UserEntity;

import java.io.Serializable;

@Entity(
        tableName = "favorite_meals",
        primaryKeys = {"meal_id", "user_id"},
        indices = {
                @Index(value = "user_id"),
                @Index(value = "meal_id")
        },
        foreignKeys = @ForeignKey(
                entity = UserEntity.class,
                parentColumns = "id",
                childColumns = "user_id",
                onDelete = ForeignKey.CASCADE
        )
)
public class FavoriteMealEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @NonNull
    @ColumnInfo(name = "meal_id")
    private String mealId;

    @NonNull
    @ColumnInfo(name = "user_id")
    private String userId;

    @Nullable
    @ColumnInfo(name = "name")
    private String name;

    @Nullable
    @ColumnInfo(name = "alternate_name")
    private String alternateName;

    @Nullable
    @ColumnInfo(name = "category")
    private String category;

    @Nullable
    @ColumnInfo(name = "area")
    private String area;

    @Nullable
    @ColumnInfo(name = "instructions")
    private String instructions;

    @Nullable
    @ColumnInfo(name = "thumbnail_url")
    private String thumbnailUrl;

    @Nullable
    @ColumnInfo(name = "tags")
    private String tags;

    @Nullable
    @ColumnInfo(name = "youtube_url")
    private String youtubeUrl;

    @Nullable
    @ColumnInfo(name = "source_url")
    private String sourceUrl;

    @Nullable
    @ColumnInfo(name = "image_source")
    private String imageSource;

    @Nullable
    @ColumnInfo(name = "creative_commons_confirmed")
    private String creativeCommonsConfirmed;

    @Nullable
    @ColumnInfo(name = "date_modified")
    private String dateModified;

    @Nullable
    @ColumnInfo(name = "ingredients_json")
    private String ingredientsJson;

    @ColumnInfo(name = "created_at")
    private long createdAt;

    // ==================== CONSTRUCTOR ====================

    public FavoriteMealEntity() {
        this.mealId = "";
        this.userId = "";
        this.createdAt = System.currentTimeMillis();
    }

    public FavoriteMealEntity(@NonNull String mealId, @NonNull String userId) {
        this.mealId = mealId;
        this.userId = userId;
        this.createdAt = System.currentTimeMillis();
    }

    // ==================== GETTERS & SETTERS ====================

    @NonNull
    public String getMealId() {
        return mealId;
    }

    public void setMealId(@NonNull String mealId) {
        this.mealId = mealId;
    }

    @NonNull
    public String getUserId() {
        return userId;
    }

    public void setUserId(@NonNull String userId) {
        this.userId = userId;
    }

    @Nullable
    public String getName() {
        return name;
    }

    public void setName(@Nullable String name) {
        this.name = name;
    }

    @Nullable
    public String getAlternateName() {
        return alternateName;
    }

    public void setAlternateName(@Nullable String alternateName) {
        this.alternateName = alternateName;
    }

    @Nullable
    public String getCategory() {
        return category;
    }

    public void setCategory(@Nullable String category) {
        this.category = category;
    }

    @Nullable
    public String getArea() {
        return area;
    }

    public void setArea(@Nullable String area) {
        this.area = area;
    }

    @Nullable
    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(@Nullable String instructions) {
        this.instructions = instructions;
    }

    @Nullable
    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(@Nullable String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    @Nullable
    public String getTags() {
        return tags;
    }

    public void setTags(@Nullable String tags) {
        this.tags = tags;
    }

    @Nullable
    public String getYoutubeUrl() {
        return youtubeUrl;
    }

    public void setYoutubeUrl(@Nullable String youtubeUrl) {
        this.youtubeUrl = youtubeUrl;
    }

    @Nullable
    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(@Nullable String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    @Nullable
    public String getImageSource() {
        return imageSource;
    }

    public void setImageSource(@Nullable String imageSource) {
        this.imageSource = imageSource;
    }

    @Nullable
    public String getCreativeCommonsConfirmed() {
        return creativeCommonsConfirmed;
    }

    public void setCreativeCommonsConfirmed(@Nullable String creativeCommonsConfirmed) {
        this.creativeCommonsConfirmed = creativeCommonsConfirmed;
    }

    @Nullable
    public String getDateModified() {
        return dateModified;
    }

    public void setDateModified(@Nullable String dateModified) {
        this.dateModified = dateModified;
    }

    @Nullable
    public String getIngredientsJson() {
        return ingredientsJson;
    }

    public void setIngredientsJson(@Nullable String ingredientsJson) {
        this.ingredientsJson = ingredientsJson;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
