package com.example.recipe_android_project.features.home.presentation.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipe_android_project.R;
import com.example.recipe_android_project.features.home.model.test.CategoryItem;
import com.example.recipe_android_project.features.home.model.test.MealItem;

import java.util.ArrayList;

public class HomeFragment extends Fragment {

    private RecyclerView rvCategories, rvMeals;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        rvCategories = view.findViewById(R.id.rvCategories);
        rvMeals = view.findViewById(R.id.rvMeals);

        setupCategories();
        setupMeals();

        return view;
    }

    private void setupCategories() {
        ArrayList<CategoryItem> categories = new ArrayList<>();
        categories.add(new CategoryItem("Breakfast", R.drawable.location_icon));
        categories.add(new CategoryItem("Vegan", R.drawable.location_icon));
        categories.add(new CategoryItem("Italian", R.drawable.location_icon));
        categories.add(new CategoryItem("Quick", R.drawable.location_icon));
        categories.add(new CategoryItem("Dessert", R.drawable.location_icon));

        rvCategories.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        );

        CategoryAdapter adapter = new CategoryAdapter(categories, (item, position) -> {
            Toast.makeText(requireContext(), "Selected: " + item.title, Toast.LENGTH_SHORT).show();
        });

        rvCategories.setAdapter(adapter);
    }

    private void setupMeals() {
        ArrayList<MealItem> meals = new ArrayList<>();
        meals.add(new MealItem("Spaghetti Carbonara", "Italian", "Pasta", R.drawable.onboarding_3));
        meals.add(new MealItem("Grilled Salmon", "French", "Seafood", R.drawable.onboarding_3));
        meals.add(new MealItem("Chicken Tikka Masala", "Indian", "Curry", R.drawable.onboarding_3));
        meals.add(new MealItem("Caesar Salad", "American", "Salad", R.drawable.onboarding_3));

        rvMeals.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvMeals.setNestedScrollingEnabled(false);

        MealAdapter adapter = new MealAdapter(meals, new MealAdapter.OnMealClickListener() {
            @Override
            public void onMealClick(MealItem meal, int position) {
                Toast.makeText(requireContext(), "Opening: " + meal.title, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFavoriteClick(MealItem meal, int position, boolean isFavorite) {
                String msg = isFavorite ? "Added to favorites" : "Removed from favorites";
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });

        rvMeals.setAdapter(adapter);
    }
}
