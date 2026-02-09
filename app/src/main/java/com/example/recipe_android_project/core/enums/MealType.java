package com.example.recipe_android_project.core.enums;

public enum MealType {
    BREAKFAST("breakfast"),
    LUNCH("lunch"),
    DINNER("dinner");

    private final String value;

    MealType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static MealType fromValue(String value) {
        for (MealType type : values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        return BREAKFAST;
    }
}
