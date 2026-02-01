package com.example.recipe_android_project.features.home.data.mapper;

import com.example.recipe_android_project.features.home.data.entities.MealEntity;
import com.example.recipe_android_project.features.home.data.dto.meal.MealDto;
import com.example.recipe_android_project.features.home.model.Ingredient;
import com.example.recipe_android_project.features.home.model.Meal;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MealMapper {

    private static final Gson gson = new Gson();

    private MealMapper() {
    }

    // ==================== DTO -> DOMAIN ====================

    public static Meal toDomain(MealDto dto) {
        if (dto == null) return null;

        Meal meal = new Meal();
        meal.setId(dto.getIdMeal());
        meal.setName(dto.getStrMeal());
        meal.setAlternateName(dto.getStrMealAlternate());
        meal.setCategory(dto.getStrCategory());
        meal.setArea(dto.getStrArea());
        meal.setInstructions(dto.getStrInstructions());
        meal.setThumbnailUrl(dto.getStrMealThumb());
        meal.setTags(dto.getStrTags());
        meal.setYoutubeUrl(dto.getStrYoutube());
        meal.setSourceUrl(dto.getStrSource());
        meal.setImageSource(dto.getStrImageSource());
        meal.setCreativeCommonsConfirmed(dto.getStrCreativeCommonsConfirmed());
        meal.setDateModified(dto.getDateModified());
        meal.setIngredients(extractIngredients(dto));
        meal.setFavorite(false);
        meal.setCreatedAt(System.currentTimeMillis());

        return meal;
    }

    public static List<Meal> toDomainList(List<MealDto> dtos) {
        List<Meal> meals = new ArrayList<>();
        if (dtos != null) {
            for (MealDto dto : dtos) {
                Meal meal = toDomain(dto);
                if (meal != null) {
                    meals.add(meal);
                }
            }
        }
        return meals;
    }

    // ==================== ENTITY -> DOMAIN ====================

    public static Meal toDomain(MealEntity entity) {
        if (entity == null) return null;

        Meal meal = new Meal();
        meal.setId(entity.getId());
        meal.setName(entity.getName());
        meal.setAlternateName(entity.getAlternateName());
        meal.setCategory(entity.getCategory());
        meal.setArea(entity.getArea());
        meal.setInstructions(entity.getInstructions());
        meal.setThumbnailUrl(entity.getThumbnailUrl());
        meal.setTags(entity.getTags());
        meal.setYoutubeUrl(entity.getYoutubeUrl());
        meal.setSourceUrl(entity.getSourceUrl());
        meal.setImageSource(entity.getImageSource());
        meal.setCreativeCommonsConfirmed(entity.getCreativeCommonsConfirmed());
        meal.setDateModified(entity.getDateModified());
        meal.setIngredients(jsonToIngredients(entity.getIngredientsJson()));
        meal.setFavorite(entity.isFavorite());
        meal.setCreatedAt(entity.getCreatedAt());

        return meal;
    }

    public static List<Meal> toDomainListFromEntities(List<MealEntity> entities) {
        List<Meal> meals = new ArrayList<>();
        if (entities != null) {
            for (MealEntity entity : entities) {
                Meal meal = toDomain(entity);
                if (meal != null) {
                    meals.add(meal);
                }
            }
        }
        return meals;
    }

    // ==================== DOMAIN -> ENTITY ====================

    public static MealEntity toEntity(Meal meal) {
        if (meal == null) return null;

        MealEntity entity = new MealEntity();
        entity.setId(meal.getId() != null ? meal.getId() : "");
        entity.setName(meal.getName());
        entity.setAlternateName(meal.getAlternateName());
        entity.setCategory(meal.getCategory());
        entity.setArea(meal.getArea());
        entity.setInstructions(meal.getInstructions());
        entity.setThumbnailUrl(meal.getThumbnailUrl());
        entity.setTags(meal.getTags());
        entity.setYoutubeUrl(meal.getYoutubeUrl());
        entity.setSourceUrl(meal.getSourceUrl());
        entity.setImageSource(meal.getImageSource());
        entity.setCreativeCommonsConfirmed(meal.getCreativeCommonsConfirmed());
        entity.setDateModified(meal.getDateModified());
        entity.setIngredientsJson(ingredientsToJson(meal.getIngredients()));
        entity.setFavorite(meal.isFavorite());
        entity.setCreatedAt(meal.getCreatedAt());

        return entity;
    }

    public static List<MealEntity> toEntityList(List<Meal> meals) {
        List<MealEntity> entities = new ArrayList<>();
        if (meals != null) {
            for (Meal meal : meals) {
                MealEntity entity = toEntity(meal);
                if (entity != null) {
                    entities.add(entity);
                }
            }
        }
        return entities;
    }

    // ==================== HELPER METHODS ====================

    private static List<Ingredient> extractIngredients(MealDto dto) {
        List<Ingredient> ingredients = new ArrayList<>();

        addIngredient(ingredients, dto.getStrIngredient1(), dto.getStrMeasure1());
        addIngredient(ingredients, dto.getStrIngredient2(), dto.getStrMeasure2());
        addIngredient(ingredients, dto.getStrIngredient3(), dto.getStrMeasure3());
        addIngredient(ingredients, dto.getStrIngredient4(), dto.getStrMeasure4());
        addIngredient(ingredients, dto.getStrIngredient5(), dto.getStrMeasure5());
        addIngredient(ingredients, dto.getStrIngredient6(), dto.getStrMeasure6());
        addIngredient(ingredients, dto.getStrIngredient7(), dto.getStrMeasure7());
        addIngredient(ingredients, dto.getStrIngredient8(), dto.getStrMeasure8());
        addIngredient(ingredients, dto.getStrIngredient9(), dto.getStrMeasure9());
        addIngredient(ingredients, dto.getStrIngredient10(), dto.getStrMeasure10());
        addIngredient(ingredients, dto.getStrIngredient11(), dto.getStrMeasure11());
        addIngredient(ingredients, dto.getStrIngredient12(), dto.getStrMeasure12());
        addIngredient(ingredients, dto.getStrIngredient13(), dto.getStrMeasure13());
        addIngredient(ingredients, dto.getStrIngredient14(), dto.getStrMeasure14());
        addIngredient(ingredients, dto.getStrIngredient15(), dto.getStrMeasure15());
        addIngredient(ingredients, dto.getStrIngredient16(), dto.getStrMeasure16());
        addIngredient(ingredients, dto.getStrIngredient17(), dto.getStrMeasure17());
        addIngredient(ingredients, dto.getStrIngredient18(), dto.getStrMeasure18());
        addIngredient(ingredients, dto.getStrIngredient19(), dto.getStrMeasure19());
        addIngredient(ingredients, dto.getStrIngredient20(), dto.getStrMeasure20());

        return ingredients;
    }

    private static void addIngredient(List<Ingredient> list, String name, String measure) {
        if (name != null && !name.trim().isEmpty()) {
            list.add(new Ingredient(name.trim(), measure != null ? measure.trim() : ""));
        }
    }

    private static String ingredientsToJson(List<Ingredient> ingredients) {
        if (ingredients == null || ingredients.isEmpty()) {
            return "[]";
        }
        return gson.toJson(ingredients);
    }

    private static List<Ingredient> jsonToIngredients(String json) {
        if (json == null || json.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            Type listType = new TypeToken<List<Ingredient>>() {}.getType();
            List<Ingredient> ingredients = gson.fromJson(json, listType);
            return ingredients != null ? ingredients : new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
