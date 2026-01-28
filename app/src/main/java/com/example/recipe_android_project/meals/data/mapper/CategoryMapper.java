package com.example.recipe_android_project.meals.data.mapper;

import com.example.recipe_android_project.meals.data.local.entities.CategoryEntity;
import com.example.recipe_android_project.meals.data.remote.dto.category.CategoryDto;
import com.example.recipe_android_project.meals.domain.model.Category;

import java.util.ArrayList;
import java.util.List;

public class CategoryMapper {

    private CategoryMapper() {
    }

    // ==================== DTO -> DOMAIN ====================

    public static Category toDomain(CategoryDto dto) {
        if (dto == null) return null;

        Category category = new Category();
        category.setId(dto.getIdCategory());
        category.setName(dto.getStrCategory());
        category.setThumbnailUrl(dto.getStrCategoryThumb());
        category.setDescription(dto.getStrCategoryDescription());
        category.setCreatedAt(System.currentTimeMillis());

        return category;
    }

    public static List<Category> toDomainList(List<CategoryDto> dtos) {
        List<Category> categories = new ArrayList<>();
        if (dtos != null) {
            for (CategoryDto dto : dtos) {
                Category category = toDomain(dto);
                if (category != null) {
                    categories.add(category);
                }
            }
        }
        return categories;
    }

    // ==================== ENTITY -> DOMAIN ====================

    public static Category toDomain(CategoryEntity entity) {
        if (entity == null) return null;

        Category category = new Category();
        category.setId(entity.getId());
        category.setName(entity.getName());
        category.setThumbnailUrl(entity.getThumbnailUrl());
        category.setDescription(entity.getDescription());
        category.setCreatedAt(entity.getCreatedAt());

        return category;
    }

    public static List<Category> toDomainListFromEntities(List<CategoryEntity> entities) {
        List<Category> categories = new ArrayList<>();
        if (entities != null) {
            for (CategoryEntity entity : entities) {
                Category category = toDomain(entity);
                if (category != null) {
                    categories.add(category);
                }
            }
        }
        return categories;
    }

    // ==================== DOMAIN -> ENTITY ====================

    public static CategoryEntity toEntity(Category category) {
        if (category == null) return null;

        CategoryEntity entity = new CategoryEntity();
        entity.setId(category.getId() != null ? category.getId() : "");
        entity.setName(category.getName());
        entity.setThumbnailUrl(category.getThumbnailUrl());
        entity.setDescription(category.getDescription());
        entity.setCreatedAt(category.getCreatedAt());

        return entity;
    }

    public static List<CategoryEntity> toEntityList(List<Category> categories) {
        List<CategoryEntity> entities = new ArrayList<>();
        if (categories != null) {
            for (Category category : categories) {
                CategoryEntity entity = toEntity(category);
                if (entity != null) {
                    entities.add(entity);
                }
            }
        }
        return entities;
    }
}
