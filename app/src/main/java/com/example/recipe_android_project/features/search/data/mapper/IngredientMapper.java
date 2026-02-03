package com.example.recipe_android_project.features.search.data.mapper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.recipe_android_project.features.search.data.dto.ingradient.IngredientDto;
import com.example.recipe_android_project.features.search.data.dto.ingradient.IngredientsResponseDto;
import com.example.recipe_android_project.features.search.domain.model.Ingredient;
import com.example.recipe_android_project.features.search.domain.model.IngredientList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class IngredientMapper {

    private IngredientMapper() {
    }


    @Nullable
    public static Ingredient toDomain(@Nullable IngredientDto dto) {
        if (dto == null) return null;

        String name = cleanText(dto.getStrIngredient());
        if (name == null) return null;

        Ingredient ingredient = new Ingredient();
        ingredient.setId(parseIdSafe(dto.getIdIngredient()));
        ingredient.setName(name);
        ingredient.setDescription(cleanTextOrEmpty(dto.getStrDescription()));
        ingredient.setThumbnailUrl(cleanTextOrEmpty(dto.getStrThumb()));
        ingredient.setType(cleanTextOrEmpty(dto.getStrType()));
        return ingredient;
    }

    @NonNull
    public static IngredientList toDomain(@Nullable IngredientsResponseDto dto) {
        IngredientList list = new IngredientList();

        if (dto == null || dto.getMeals() == null) {
            list.setIngredients(Collections.emptyList());
            return list;
        }

        List<Ingredient> ingredients = new ArrayList<>();
        for (IngredientDto item : dto.getMeals()) {
            Ingredient ingredient = toDomain(item);
            if (ingredient != null) {
                ingredients.add(ingredient);
            }
        }

        list.setIngredients(ingredients);
        return list;
    }

    @NonNull
    public static List<Ingredient> toDomainList(@Nullable IngredientsResponseDto dto) {
        return toDomain(dto).getIngredients();
    }


    @Nullable
    public static IngredientDto toDto(@Nullable Ingredient domain) {
        if (domain == null) return null;

        IngredientDto dto = new IngredientDto();
        dto.setIdIngredient(String.valueOf(domain.getId()));
        dto.setStrIngredient(domain.getName());
        dto.setStrDescription(domain.getDescription());
        dto.setStrThumb(domain.getThumbnailUrl());
        dto.setStrType(domain.getType());
        return dto;
    }

    @NonNull
    public static IngredientsResponseDto toDto(@Nullable IngredientList domain) {
        IngredientsResponseDto responseDto = new IngredientsResponseDto();

        if (domain == null || domain.getIngredients() == null) {
            responseDto.setMeals(Collections.emptyList());
            return responseDto;
        }

        List<IngredientDto> dtos = new ArrayList<>();
        for (Ingredient ingredient : domain.getIngredients()) {
            IngredientDto dto = toDto(ingredient);
            if (dto != null) {
                dtos.add(dto);
            }
        }

        responseDto.setMeals(dtos);
        return responseDto;
    }


    private static int parseIdSafe(@Nullable String id) {
        String cleaned = cleanText(id);
        if (cleaned == null) return 0;

        try {
            return Integer.parseInt(cleaned);
        } catch (Exception e) {
            return 0;
        }
    }

    @Nullable
    private static String cleanText(@Nullable String value) {
        if (value == null) return null;

        String v = value.trim();
        if (v.isEmpty() || "null".equalsIgnoreCase(v)) return null;

        return v;
    }

    @NonNull
    private static String cleanTextOrEmpty(@Nullable String value) {
        String v = cleanText(value);
        return v != null ? v : "";
    }
}
