package com.example.recipe_android_project.features.home.model;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class AreaList {
    private final List<Area> areas;

    public AreaList(List<Area> areas) {
        this.areas = areas != null
                ? Collections.unmodifiableList(areas)
                : Collections.emptyList();
    }

    public List<Area> getAreas() {
        return areas;
    }

    public int size() {
        return areas.size();
    }

    public boolean isEmpty() {
        return areas.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AreaList areaList = (AreaList) o;
        return Objects.equals(areas, areaList.areas);
    }

    @Override
    public int hashCode() {
        return Objects.hash(areas);
    }

    @Override
    public String toString() {
        return "AreaList{" +
                "areas=" + areas +
                ", size=" + size() +
                '}';
    }
}
