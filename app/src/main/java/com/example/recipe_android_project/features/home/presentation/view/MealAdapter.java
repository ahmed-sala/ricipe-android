package com.example.recipe_android_project.features.home.presentation.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipe_android_project.R;
import com.example.recipe_android_project.features.home.model.test.MealItem;
import com.google.android.material.card.MaterialCardView;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MealAdapter extends RecyclerView.Adapter<MealAdapter.MealViewHolder> {

    private final List<MealItem> items;
    private final Set<Integer> favoritePositions = new HashSet<>();
    private OnMealClickListener listener;

    public interface OnMealClickListener {
        void onMealClick(MealItem meal, int position);
        void onFavoriteClick(MealItem meal, int position, boolean isFavorite);
    }

    public MealAdapter(List<MealItem> items) {
        this.items = items;
    }

    public MealAdapter(List<MealItem> items, OnMealClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_meal, parent, false);
        return new MealViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MealViewHolder holder, int position) {
        MealItem meal = items.get(position);

        holder.tvTitle.setText(meal.title);
        holder.tvCountry.setText(meal.country + " Cuisine");
        holder.tvCategory.setText(meal.category.toUpperCase());
        holder.imgMeal.setImageResource(meal.imageRes);

        boolean isFavorite = favoritePositions.contains(position);
        updateFavoriteIcon(holder, isFavorite);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMealClick(meal, position);
            }
        });

        holder.btnView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMealClick(meal, position);
            }
        });

        holder.btnFavorite.setOnClickListener(v -> {
            boolean newFavoriteState;
            if (favoritePositions.contains(position)) {
                favoritePositions.remove(position);
                newFavoriteState = false;
            } else {
                favoritePositions.add(position);
                newFavoriteState = true;
            }
            updateFavoriteIcon(holder, newFavoriteState);

            holder.btnFavorite.animate()
                    .scaleX(0.8f)
                    .scaleY(0.8f)
                    .setDuration(100)
                    .withEndAction(() ->
                            holder.btnFavorite.animate()
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .setDuration(100)
                                    .start()
                    ).start();

            if (listener != null) {
                listener.onFavoriteClick(meal, position, newFavoriteState);
            }
        });
    }

    private void updateFavoriteIcon(MealViewHolder holder, boolean isFavorite) {
        if (isFavorite) {
            holder.icFavorite.setImageResource(R.drawable.favorite_icon);
            holder.btnFavorite.setCardBackgroundColor(0xFFFF7A1A);
            holder.icFavorite.setColorFilter(0xFFFFFFFF);
        } else {
            holder.icFavorite.setImageResource(R.drawable.ic_favorite_border);
            holder.btnFavorite.setCardBackgroundColor(0xFFFFF5F0);
            holder.icFavorite.setColorFilter(0xFFFF7A1A);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class MealViewHolder extends RecyclerView.ViewHolder {
        ImageView imgMeal, icFavorite;
        TextView tvTitle, tvCountry, tvCategory;
        MaterialCardView btnFavorite, btnView;

        MealViewHolder(@NonNull View itemView) {
            super(itemView);
            imgMeal = itemView.findViewById(R.id.imgMeal);
            tvTitle = itemView.findViewById(R.id.tvMealTitle);
            tvCountry = itemView.findViewById(R.id.tvMealCountry);
            tvCategory = itemView.findViewById(R.id.tvMealCategory);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
            btnView = itemView.findViewById(R.id.btnView);
            icFavorite = itemView.findViewById(R.id.icFavorite);
        }
    }
}
