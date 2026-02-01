package com.example.recipe_android_project.features.home.model;

import java.util.Objects;

public class Area {
    private final String name;

    public Area(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Area area = (Area) o;
        return Objects.equals(name, area.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "Area{" +
                "name='" + name + '\'' +
                '}';
    }
}
