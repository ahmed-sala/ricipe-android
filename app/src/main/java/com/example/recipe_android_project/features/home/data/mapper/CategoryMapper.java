package com.example.recipe_android_project.features.home.data.mapper;


import com.example.recipe_android_project.features.home.data.dto.category.CategoryDto;
import com.example.recipe_android_project.features.home.model.Category;

import java.util.ArrayList;
import java.util.List;

public class CategoryMapper {

    private CategoryMapper() {
    }

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






}
