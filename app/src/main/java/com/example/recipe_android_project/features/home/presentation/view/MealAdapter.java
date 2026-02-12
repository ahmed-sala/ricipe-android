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
import com.example.recipe_android_project.core.listeners.OnMealClickListener;
import com.example.recipe_android_project.features.home.model.Meal;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class MealAdapter extends RecyclerView.Adapter<MealAdapter.VH> {

    private List<Meal> items;
    private final OnMealClickListener listener;

    private static final int COLOR_FAVORITE_BG = 0xFFE27036;
    private static final int COLOR_UNFAVORITE_BG = 0xFFFFF5F0;
    private static final int COLOR_FAVORITE_ICON = 0xFFFFFFFF;
    private static final int COLOR_UNFAVORITE_ICON = 0xFFE27036;

    public MealAdapter(List<Meal> items, OnMealClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    public void setItems(List<Meal> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    public void updateMealFavoriteStatus(String mealId, boolean isFavorite) {
        if (items == null || mealId == null) return;

        for (int i = 0; i < items.size(); i++) {
            Meal meal = items.get(i);
            if (meal != null && mealId.equals(meal.getId())) {
                meal.setFavorite(isFavorite);
                notifyItemChanged(i, "favorite");
                break;
            }
        }
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
        if (meal == null) return;

        holder.tvTitle.setText(meal.getName() != null ? meal.getName() : "");

        String category = meal.getCategory();
        if (category != null && !category.isEmpty()) {
            holder.tvCategory.setText(category.toUpperCase());
            holder.tvCategory.setVisibility(View.VISIBLE);
        } else {
            holder.tvCategory.setVisibility(View.GONE);
        }
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

        updateFavoriteIcon(holder, meal.isFavorite());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMealClick(meal, holder.getAdapterPosition());
            }
        });

        holder.btnFavorite.setOnClickListener(v -> {
            if (listener != null) {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    listener.onFavoriteClick(meal, adapterPosition, meal.isFavorite());
                }
            }
        });
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position, @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position);
        } else {
            for (Object payload : payloads) {
                if ("favorite".equals(payload)) {
                    Meal meal = items.get(position);
                    if (meal != null) {
                        updateFavoriteIcon(holder, meal.isFavorite());
                    }
                }
            }
        }
    }

    private void updateFavoriteIcon(VH holder, boolean isFavorite) {
        if (isFavorite) {
            holder.icFavorite.setImageResource(R.drawable.favorite_icon);
            holder.btnFavorite.setCardBackgroundColor(COLOR_FAVORITE_BG);
            holder.icFavorite.setColorFilter(COLOR_FAVORITE_ICON);
        } else {
            holder.icFavorite.setImageResource(R.drawable.ic_favorite_border);
            holder.btnFavorite.setCardBackgroundColor(COLOR_UNFAVORITE_BG);
            holder.icFavorite.setColorFilter(COLOR_UNFAVORITE_ICON);
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
        TextView tvTitle, tvCategory;
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
