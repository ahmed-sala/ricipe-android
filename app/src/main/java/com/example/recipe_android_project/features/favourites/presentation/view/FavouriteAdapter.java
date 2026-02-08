package com.example.recipe_android_project.features.favourites.presentation.view;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
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

import java.util.ArrayList;
import java.util.List;

public class FavouriteAdapter extends RecyclerView.Adapter<FavouriteAdapter.VH> {

    public interface OnFavouriteItemListener {
        void onItemClick(Meal meal, int position);
        void onRemoveClick(Meal meal, int position);
    }

    private List<Meal> items = new ArrayList<>();
    private final OnFavouriteItemListener listener;

    private static final int COLOR_FAVORITE_BG = 0xFFFF7A1A;
    private static final int COLOR_FAVORITE_ICON = 0xFFFFFFFF;

    public FavouriteAdapter(OnFavouriteItemListener listener) {
        this.listener = listener;
    }

    public void setItems(List<Meal> newItems) {
        if (newItems == null) {
            newItems = new ArrayList<>();
        }

        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new MealDiffCallback(items, newItems));
        items = new ArrayList<>(newItems);
        diffResult.dispatchUpdatesTo(this);
    }

    public void setItemsImmediate(List<Meal> newItems) {
        if (newItems == null) {
            newItems = new ArrayList<>();
        }
        items = new ArrayList<>(newItems);
        notifyDataSetChanged();
    }
    public void clearItemsImmediate() {
        items.clear();
        notifyDataSetChanged();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public int getSize() {
        return items.size();
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

        holder.icFavorite.setImageResource(R.drawable.favorite_icon);
        holder.btnFavorite.setCardBackgroundColor(COLOR_FAVORITE_BG);
        holder.icFavorite.setColorFilter(COLOR_FAVORITE_ICON);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    listener.onItemClick(meal, adapterPosition);
                }
            }
        });

        holder.btnFavorite.setOnClickListener(v -> {
            if (listener != null) {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    listener.onRemoveClick(meal, adapterPosition);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
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

    private static class MealDiffCallback extends DiffUtil.Callback {
        private final List<Meal> oldList;
        private final List<Meal> newList;

        MealDiffCallback(List<Meal> oldList, List<Meal> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            Meal oldMeal = oldList.get(oldItemPosition);
            Meal newMeal = newList.get(newItemPosition);

            if (oldMeal == null || newMeal == null) return false;
            if (oldMeal.getId() == null || newMeal.getId() == null) return false;

            return oldMeal.getId().equals(newMeal.getId());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            Meal oldMeal = oldList.get(oldItemPosition);
            Meal newMeal = newList.get(newItemPosition);

            if (oldMeal == null || newMeal == null) return false;

            return oldMeal.equals(newMeal);
        }
    }
}
