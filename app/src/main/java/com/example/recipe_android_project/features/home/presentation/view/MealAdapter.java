package com.example.recipe_android_project.features.home.presentation.view;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.recipe_android_project.R;
import com.example.recipe_android_project.features.home.model.Meal;
import com.google.android.material.card.MaterialCardView;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MealAdapter extends RecyclerView.Adapter<MealAdapter.VH> {

    public interface OnMealClickListener {
        void onMealClick(Meal meal, int position);
        void onFavoriteClick(Meal meal, int position, boolean isFavorite);
    }

    private List<Meal> items;
    private final Set<Integer> favoritePositions = new HashSet<>();
    private final OnMealClickListener listener;

    public MealAdapter(List<Meal> items, OnMealClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    public void setItems(List<Meal> items) {
        this.items = items;
        favoritePositions.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_meal, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Meal meal = items.get(position);

        holder.tvTitle.setText(meal.getName());
        holder.tvCategory.setText(meal.getCategory() != null ? meal.getCategory().toUpperCase() : "");

        holder.imgLoader.setVisibility(View.VISIBLE);
        holder.imgLoader.playAnimation();

        Glide.with(holder.itemView.getContext())
                .load(meal.getThumbnailUrl())
                .centerCrop()
                .error(R.drawable.ic_error)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e,
                                                Object model,
                                                Target<Drawable> target,
                                                boolean isFirstResource) {
                        holder.imgLoader.cancelAnimation();
                        holder.imgLoader.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource,
                                                   Object model,
                                                   Target<Drawable> target,
                                                   DataSource dataSource,
                                                   boolean isFirstResource) {
                        holder.imgLoader.cancelAnimation();
                        holder.imgLoader.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(holder.imgMeal);

        boolean isFavorite = favoritePositions.contains(position);
        updateFavoriteIcon(holder, isFavorite);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onMealClick(meal, position);
        });



        holder.btnFavorite.setOnClickListener(v -> {
            boolean newState;
            if (favoritePositions.contains(position)) {
                favoritePositions.remove(position);
                newState = false;
            } else {
                favoritePositions.add(position);
                newState = true;
            }
            updateFavoriteIcon(holder, newState);

            if (listener != null) listener.onFavoriteClick(meal, position, newState);
        });
    }

    private void updateFavoriteIcon(VH holder, boolean isFavorite) {
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
        return items != null ? items.size() : 0;
    }

    @Override
    public void onViewRecycled(@NonNull VH holder) {
        super.onViewRecycled(holder);
        holder.imgLoader.cancelAnimation();
        holder.imgLoader.setVisibility(View.GONE);
        Glide.with(holder.itemView.getContext()).clear(holder.imgMeal);
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView imgMeal, icFavorite;
        TextView tvTitle,  tvCategory;
        MaterialCardView btnFavorite;
        LottieAnimationView imgLoader;

        VH(@NonNull View itemView) {
            super(itemView);
            imgMeal = itemView.findViewById(R.id.imgMeal);
            tvTitle = itemView.findViewById(R.id.tvMealTitle);
            tvCategory = itemView.findViewById(R.id.tvMealCategory);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);

            icFavorite = itemView.findViewById(R.id.icFavorite);
            imgLoader = itemView.findViewById(R.id.lottieImgLoading);
        }
    }
}
