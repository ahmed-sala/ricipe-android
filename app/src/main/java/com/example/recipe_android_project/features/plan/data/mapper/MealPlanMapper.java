package com.example.recipe_android_project.features.plan.data.mapper;

import com.example.recipe_android_project.features.home.model.Meal;
import com.example.recipe_android_project.features.meal_detail.domain.model.MealPlan;
import com.example.recipe_android_project.features.plan.data.entity.MealPlanEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MealPlanMapper {

    private MealPlanMapper() {
    }

    public static Meal toMealDomain(MealPlanEntity entity) {
        if (entity == null) return null;

        Meal meal = new Meal();
        meal.setId(entity.getMealId());
        meal.setName(entity.getMealName());
        meal.setThumbnailUrl(entity.getMealThumbnail());
        meal.setCategory(entity.getMealCategory());
        meal.setArea(entity.getMealArea());
        meal.setFavorite(false);

        meal.setInstructions(null);
        meal.setYoutubeUrl(null);
        meal.setIngredients(new ArrayList<>());

        return meal;
    }
    public static MealPlan toDomain(MealPlanEntity entity) {
        if (entity == null) return null;

        MealPlan mealPlan = new MealPlan();
        mealPlan.setUserId(entity.getUserId());
        mealPlan.setDate(entity.getDate());
        mealPlan.setMealType(entity.getMealType());
        mealPlan.setMealId(entity.getMealId());
        mealPlan.setMealName(entity.getMealName());
        mealPlan.setMealThumbnail(entity.getMealThumbnail());
        mealPlan.setMealCategory(entity.getMealCategory());
        mealPlan.setMealArea(entity.getMealArea());
        mealPlan.setCreatedAt(entity.getCreatedAt());
        mealPlan.setUpdatedAt(entity.getUpdatedAt());
        mealPlan.setSynced(entity.isSynced());

        return mealPlan;
    }

    public static List<MealPlan> toDomainList(List<MealPlanEntity> entities) {
        List<MealPlan> mealPlans = new ArrayList<>();
        if (entities != null) {
            for (MealPlanEntity entity : entities) {
                MealPlan mealPlan = toDomain(entity);
                if (mealPlan != null) {
                    mealPlans.add(mealPlan);
                }
            }
        }
        return mealPlans;
    }
    public static MealPlanEntity mapToEntity(Map<String, Object> map) {
        if (map == null || map.isEmpty()) return null;

        MealPlanEntity entity = new MealPlanEntity();

        entity.setUserId(getStringValue(map, "userId", ""));
        entity.setDate(getStringValue(map, "date", ""));
        entity.setMealType(getStringValue(map, "mealType", ""));
        entity.setMealId(getStringValue(map, "mealId", ""));
        entity.setMealName(getStringValue(map, "mealName", null));
        entity.setMealThumbnail(getStringValue(map, "mealThumbnail", null));
        entity.setMealCategory(getStringValue(map, "mealCategory", null));
        entity.setMealArea(getStringValue(map, "mealArea", null));
        entity.setCreatedAt(getLongValue(map, "createdAt", System.currentTimeMillis()));
        entity.setUpdatedAt(getLongValue(map, "updatedAt", System.currentTimeMillis()));
        entity.setSynced(getBooleanValue(map, "synced", true));

        return entity;
    }
    public static Map<String, Object> entityToMap(MealPlanEntity entity) {
        if (entity == null) return null;

        Map<String, Object> map = new HashMap<>();
        map.put("userId", entity.getUserId());
        map.put("date", entity.getDate());
        map.put("mealType", entity.getMealType());
        map.put("mealId", entity.getMealId());
        map.put("mealName", entity.getMealName());
        map.put("mealThumbnail", entity.getMealThumbnail());
        map.put("mealCategory", entity.getMealCategory());
        map.put("mealArea", entity.getMealArea());
        map.put("createdAt", entity.getCreatedAt());
        map.put("updatedAt", entity.getUpdatedAt());
        return map;
    }
    private static String getStringValue(Map<String, Object> map, String key, String defaultValue) {
        Object value = map.get(key);
        if (value != null) {
            return String.valueOf(value);
        }
        return defaultValue;
    }

    private static long getLongValue(Map<String, Object> map, String key, long defaultValue) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    private static boolean getBooleanValue(Map<String, Object> map, String key, boolean defaultValue) {
        Object value = map.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        return defaultValue;
    }
    public static MealPlanEntity createNewMealPlanEntity(String userId, String date, String mealType,
                                                         String mealId, String mealName, String mealThumbnail,
                                                         String mealCategory, String mealArea) {
        MealPlanEntity entity = new MealPlanEntity();
        entity.setUserId(userId);
        entity.setDate(date);
        entity.setMealType(mealType);
        entity.setMealId(mealId);
        entity.setMealName(mealName);
        entity.setMealThumbnail(mealThumbnail);
        entity.setMealCategory(mealCategory);
        entity.setMealArea(mealArea);
        entity.setCreatedAt(System.currentTimeMillis());
        entity.setUpdatedAt(System.currentTimeMillis());
        entity.setSynced(false);
        return entity;
    }
    public static boolean isValidMealPlanEntity(MealPlanEntity entity) {
        if (entity == null) return false;
        return isNotEmpty(entity.getUserId()) &&
                isNotEmpty(entity.getDate()) &&
                isNotEmpty(entity.getMealType()) &&
                isNotEmpty(entity.getMealId());
    }
    private static boolean isNotEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }
    public static String generateDocumentId(MealPlanEntity entity) {
        if (entity == null) return null;
        return generateDocumentId(entity.getDate(), entity.getMealType());
    }

    public static String generateDocumentId(String date, String mealType) {
        if (date == null || mealType == null) return null;
        return date + "_" + mealType;
    }
}
