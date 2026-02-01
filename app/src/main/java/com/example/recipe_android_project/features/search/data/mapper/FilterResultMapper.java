package com.example.recipe_android_project.features.search.data.mapper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.recipe_android_project.features.search.data.dto.filter_result.FilterResultDto;
import com.example.recipe_android_project.features.search.data.dto.filter_result.FilterResultResponseDto;
import com.example.recipe_android_project.features.search.domain.model.FilterResult;
import com.example.recipe_android_project.features.search.domain.model.FilterResultList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class FilterResultMapper {

    private FilterResultMapper() {
    }

    // ==================== DTO -> DOMAIN ====================

    @Nullable
    public static FilterResult toDomain(@Nullable FilterResultDto dto) {
        if (dto == null) return null;

        int id = parseIdSafe(dto.getIdMeal());
        if (id <= 0) return null;

        String name = cleanText(dto.getStrMeal());
        if (name == null) return null;

        FilterResult result = new FilterResult();
        result.setId(id);
        result.setName(name);
        result.setThumbnailUrl(cleanTextOrEmpty(dto.getStrMealThumb()));
        return result;
    }

    @NonNull
    public static FilterResultList toDomain(@Nullable FilterResultResponseDto dto) {
        FilterResultList list = new FilterResultList();

        if (dto == null || dto.getMeals() == null) {
            list.setMeals(Collections.emptyList());
            return list;
        }

        List<FilterResult> results = new ArrayList<>();
        for (FilterResultDto item : dto.getMeals()) {
            FilterResult r = toDomain(item);
            if (r != null) {
                results.add(r);
            }
        }

        list.setMeals(results);
        return list;
    }

    @NonNull
    public static List<FilterResult> toDomainList(@Nullable FilterResultResponseDto dto) {
        return toDomain(dto).getMeals();
    }

    // ==================== DOMAIN -> DTO ====================

    @Nullable
    public static FilterResultDto toDto(@Nullable FilterResult domain) {
        if (domain == null) return null;

        FilterResultDto dto = new FilterResultDto();
        dto.setIdMeal(String.valueOf(domain.getId()));
        dto.setStrMeal(domain.getName());
        dto.setStrMealThumb(domain.getThumbnailUrl());
        return dto;
    }

    @NonNull
    public static FilterResultResponseDto toDto(@Nullable FilterResultList domain) {
        FilterResultResponseDto responseDto = new FilterResultResponseDto();

        if (domain == null || domain.getMeals() == null) {
            responseDto.setMeals(Collections.emptyList());
            return responseDto;
        }

        List<FilterResultDto> dtos = new ArrayList<>();
        for (FilterResult item : domain.getMeals()) {
            FilterResultDto dto = toDto(item);
            if (dto != null) {
                dtos.add(dto);
            }
        }

        responseDto.setMeals(dtos);
        return responseDto;
    }

    // ==================== HELPERS ====================

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
