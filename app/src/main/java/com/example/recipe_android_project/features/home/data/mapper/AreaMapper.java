package com.example.recipe_android_project.features.home.data.mapper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.recipe_android_project.features.home.data.dto.area.AreaDto;
import com.example.recipe_android_project.features.home.data.dto.area.AreaResponseDto;
import com.example.recipe_android_project.features.home.model.Area;
import com.example.recipe_android_project.features.home.model.AreaList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class AreaMapper {

    private AreaMapper() {
    }


    @Nullable
    public static Area toDomain(@Nullable AreaDto dto) {
        if (dto == null) return null;

        String name = dto.getStrArea();
        if (name == null) return null;

        name = name.trim();
        if (name.isEmpty() || "null".equalsIgnoreCase(name)) return null;

        return new Area(name);
    }

    @NonNull
    public static AreaList toDomain(@Nullable AreaResponseDto dto) {
        if (dto == null || dto.getMeals() == null) {
            return new AreaList(Collections.emptyList());
        }

        List<Area> areas = new ArrayList<>();
        for (AreaDto areaDto : dto.getMeals()) {
            Area area = toDomain(areaDto);
            if (area != null) {
                areas.add(area);
            }
        }

        return new AreaList(areas);
    }

    @NonNull
    public static List<Area> toDomainList(@Nullable AreaResponseDto dto) {
        return toDomain(dto).getAreas();
    }


    @Nullable
    public static AreaDto toDto(@Nullable Area domain) {
        if (domain == null) return null;

        AreaDto dto = new AreaDto();
        dto.setStrArea(domain.getName());
        return dto;
    }

    @NonNull
    public static AreaResponseDto toDto(@Nullable AreaList domain) {
        AreaResponseDto responseDto = new AreaResponseDto();

        if (domain == null || domain.getAreas() == null) {
            responseDto.setMeals(Collections.emptyList());
            return responseDto;
        }

        List<AreaDto> dtos = new ArrayList<>();
        for (Area area : domain.getAreas()) {
            AreaDto dto = toDto(area);
            if (dto != null) {
                dtos.add(dto);
            }
        }

        responseDto.setMeals(dtos);
        return responseDto;
    }
}
