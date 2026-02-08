package com.example.recipe_android_project.features.search.presentation.view;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import com.example.recipe_android_project.features.search.domain.model.FilterResult;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FilterResultAdapter extends RecyclerView.Adapter<FilterResultAdapter.VH> {

    // Colors for favorite button states
    private static final int COLOR_FAVORITE_BG = 0xFFE27036;
    private static final int COLOR_UNFAVORITE_BG = 0xFFFFF5F0;
    private static final int COLOR_FAVORITE_ICON = 0xFFFFFFFF;
    private static final int COLOR_UNFAVORITE_ICON = 0xFFE27036;

    public interface OnFilterResultClickListener {
        void onMealClick(FilterResult filterResult, int position);
        void onFavoriteClick(FilterResult filterResult, int position, boolean isFavorite);
    }

    private List<FilterResult> items = new ArrayList<>();
    private final Set<Integer> favoriteIds = new HashSet<>();
    private final Set<Integer> loadingIds = new HashSet<>();
    private final OnFilterResultClickListener listener;
    private String filterTag = "";

    public FilterResultAdapter(OnFilterResultClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<FilterResult> items) {
        this.items = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setFilterTag(String filterTag) {
        this.filterTag = filterTag != null ? filterTag : "";
    }
    public void updateFavoriteStatus(int mealId, boolean isFavorite) {
        if (isFavorite) {
            favoriteIds.add(mealId);
        } else {
            favoriteIds.remove(mealId);
        }

        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getId() == mealId) {
                notifyItemChanged(i, "favorite");
                break;
            }
        }
    }
    public boolean isFavorite(int mealId) {
        return favoriteIds.contains(mealId);
    }
    public void showLoading(int mealId) {
        loadingIds.add(mealId);
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getId() == mealId) {
                notifyItemChanged(i, "loading");
                break;
            }
        }
    }
    public void hideLoading(int mealId) {
        loadingIds.remove(mealId);
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getId() == mealId) {
                notifyItemChanged(i, "loading");
                break;
            }
        }
    }
    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_filter_meal, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        FilterResult item = items.get(position);

        holder.tvTitle.setText(item.getName() != null ? item.getName() : "");

        holder.tvFilterTag.setText(filterTag.toUpperCase());

        holder.imgLoader.setVisibility(View.VISIBLE);
        holder.imgLoader.playAnimation();

        Glide.with(holder.itemView.getContext())
                .load(item.getThumbnailUrl())
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

        boolean isFavorite = favoriteIds.contains(item.getId());
        updateFavoriteIcon(holder, isFavorite);

        boolean isLoading = loadingIds.contains(item.getId());
        updateLoadingState(holder, isLoading);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMealClick(item, holder.getAdapterPosition());
            }
        });

        holder.btnFavorite.setOnClickListener(v -> {
            if (listener != null && !loadingIds.contains(item.getId())) {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    boolean currentlyFavorite = favoriteIds.contains(item.getId());
                    listener.onFavoriteClick(item, adapterPosition, currentlyFavorite);
                }
            }
        });
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position, @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position);
        } else {
            FilterResult item = items.get(position);
            for (Object payload : payloads) {
                if ("favorite".equals(payload)) {
                    boolean isFavorite = favoriteIds.contains(item.getId());
                    updateFavoriteIcon(holder, isFavorite);
                } else if ("loading".equals(payload)) {
                    boolean isLoading = loadingIds.contains(item.getId());
                    updateLoadingState(holder, isLoading);
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

    private void updateLoadingState(VH holder, boolean isLoading) {
        if (holder.favoriteLoading != null) {
            holder.favoriteLoading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            holder.icFavorite.setVisibility(isLoading ? View.INVISIBLE : View.VISIBLE);
            holder.btnFavorite.setEnabled(!isLoading);
        }
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
        TextView tvTitle, tvFilterTag;
        MaterialCardView btnFavorite;
        LottieAnimationView imgLoader;
        ProgressBar favoriteLoading;

        VH(@NonNull View itemView) {
            super(itemView);
            imgMeal = itemView.findViewById(R.id.imgMeal);
            tvTitle = itemView.findViewById(R.id.tvMealTitle);
            tvFilterTag = itemView.findViewById(R.id.tvFilterTag);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
            icFavorite = itemView.findViewById(R.id.icFavorite);
            imgLoader = itemView.findViewById(R.id.lottieImgLoading);
        }
    }
}
