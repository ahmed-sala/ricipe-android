package com.example.recipe_android_project.features.home.data.datasource.remote;
import com.example.recipe_android_project.core.config.RetrofitClient;
import com.example.recipe_android_project.features.home.data.dto.category.CategoryResponseDto;
import com.example.recipe_android_project.features.search.data.dto.filter_result.FilterResultResponseDto;
import com.example.recipe_android_project.features.home.data.dto.meal.MealResponseDto;
import retrofit2.Call;

public class HomeRemoteDatasource {

    private final MealApiService mealApiService;

    public HomeRemoteDatasource() {
        this.mealApiService = RetrofitClient.getMealApiService();
    }

    public Call<MealResponseDto> getMealOfTheDayCall() {
        return mealApiService.getRandomMeal();
    }

    public Call<CategoryResponseDto> getCategoriesCall() {
        return mealApiService.getCategories();
    }

    public Call<MealResponseDto> getMealsByFirstLetterCall(String firstLetter) {
        return mealApiService.getMealsByFirstLetter(firstLetter);
    }

    public Call<FilterResultResponseDto> getMealsByCategoryCall(String categoryName) {
        return mealApiService.filterByMealCategory(categoryName);
    }

    public Call<MealResponseDto> getMealByIdCall(String id) {
        return mealApiService.getMealById(id);
    }
}
