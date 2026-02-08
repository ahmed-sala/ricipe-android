package com.example.recipe_android_project.features.home.data.mapper;

import com.example.recipe_android_project.features.home.data.entities.FavoriteMealEntity;
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
    public static Meal toDomain(FavoriteMealEntity entity) {
        if (entity == null) return null;

        Meal meal = new Meal();
        meal.setId(entity.getMealId()); // Changed from getId() to getMealId()
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
        meal.setFavorite(true);
        meal.setCreatedAt(entity.getCreatedAt());

        return meal;
    }
    public static List<Meal> toDomainListFromEntities(List<FavoriteMealEntity> entities) {
        List<Meal> meals = new ArrayList<>();
        if (entities != null) {
            for (FavoriteMealEntity entity : entities) {
                Meal meal = toDomain(entity);
                if (meal != null) {
                    meals.add(meal);
                }
            }
        }
        return meals;
    }
    public static FavoriteMealEntity toEntity(Meal meal, String userId) {
        if (meal == null || userId == null || userId.isEmpty()) return null;

        FavoriteMealEntity entity = new FavoriteMealEntity();
        entity.setMealId(meal.getId() != null ? meal.getId() : "");
        entity.setUserId(userId);
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
        entity.setCreatedAt(System.currentTimeMillis());

        return entity;
    }
    public static List<FavoriteMealEntity> toEntityList(List<Meal> meals, String userId) {
        List<FavoriteMealEntity> entities = new ArrayList<>();
        if (meals != null && userId != null && !userId.isEmpty()) {
            for (Meal meal : meals) {
                FavoriteMealEntity entity = toEntity(meal, userId);
                if (entity != null) {
                    entities.add(entity);
                }
            }
        }
        return entities;
    }
    public static FavoriteMealEntity toEntity(MealDto dto, String userId) {
        if (dto == null || userId == null || userId.isEmpty()) return null;

        FavoriteMealEntity entity = new FavoriteMealEntity();
        entity.setMealId(dto.getIdMeal() != null ? dto.getIdMeal() : "");
        entity.setUserId(userId);
        entity.setName(dto.getStrMeal());
        entity.setAlternateName(dto.getStrMealAlternate());
        entity.setCategory(dto.getStrCategory());
        entity.setArea(dto.getStrArea());
        entity.setInstructions(dto.getStrInstructions());
        entity.setThumbnailUrl(dto.getStrMealThumb());
        entity.setTags(dto.getStrTags());
        entity.setYoutubeUrl(dto.getStrYoutube());
        entity.setSourceUrl(dto.getStrSource());
        entity.setImageSource(dto.getStrImageSource());
        entity.setCreativeCommonsConfirmed(dto.getStrCreativeCommonsConfirmed());
        entity.setDateModified(dto.getDateModified());
        entity.setIngredientsJson(ingredientsToJson(extractIngredients(dto)));
        entity.setCreatedAt(System.currentTimeMillis());

        return entity;
    }

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
    public static List<String> getMealIds(List<FavoriteMealEntity> entities) {
        List<String> ids = new ArrayList<>();
        if (entities != null) {
            for (FavoriteMealEntity entity : entities) {
                if (entity.getMealId() != null && !entity.getMealId().isEmpty()) {
                    ids.add(entity.getMealId());
                }
            }
        }
        return ids;
    }

    public static boolean isMealInFavorites(String mealId, List<FavoriteMealEntity> favorites) {
        if (mealId == null || favorites == null) return false;
        for (FavoriteMealEntity entity : favorites) {
            if (mealId.equals(entity.getMealId())) {
                return true;
            }
        }
        return false;
    }

    public static List<Meal> markFavorites(List<Meal> meals, List<FavoriteMealEntity> favorites) {
        if (meals == null) return new ArrayList<>();
        if (favorites == null || favorites.isEmpty()) return meals;

        List<String> favoriteIds = getMealIds(favorites);
        for (Meal meal : meals) {
            meal.setFavorite(favoriteIds.contains(meal.getId()));
        }
        return meals;
    }

    public static Meal markFavorite(Meal meal, List<FavoriteMealEntity> favorites) {
        if (meal == null) return null;
        meal.setFavorite(isMealInFavorites(meal.getId(), favorites));
        return meal;
    }
}
