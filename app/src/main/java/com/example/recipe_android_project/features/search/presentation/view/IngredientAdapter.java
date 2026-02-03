package com.example.recipe_android_project.features.search.presentation.view;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.recipe_android_project.R;
import com.example.recipe_android_project.features.search.domain.model.Ingredient;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class IngredientAdapter extends RecyclerView.Adapter<IngredientAdapter.IngredientViewHolder> {

    private static final String INGREDIENT_IMAGE_BASE_URL = "https://www.themealdb.com/images/ingredients/";

    private List<Ingredient> items;
    private final OnIngredientClickListener listener;

    public interface OnIngredientClickListener {
        void onIngredientClick(Ingredient ingredient);
    }

    public IngredientAdapter(List<Ingredient> items, OnIngredientClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public IngredientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_grid_card, parent, false);
        return new IngredientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IngredientViewHolder holder, int position) {
        Ingredient item = items.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public void updateItems(List<Ingredient> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    static class IngredientViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardItem;
        private final ImageView imgItem;
        private final TextView tvTitle;
        private final LottieAnimationView lottieLoadingThumb;

        public IngredientViewHolder(@NonNull View itemView) {
            super(itemView);
            cardItem = itemView.findViewById(R.id.cardItem);
            imgItem = itemView.findViewById(R.id.imgItem);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            lottieLoadingThumb = itemView.findViewById(R.id.lottieLoadingThumb);
        }

        public void bind(Ingredient item, OnIngredientClickListener listener) {
            tvTitle.setText(item.getName());

            lottieLoadingThumb.setVisibility(View.VISIBLE);
            lottieLoadingThumb.playAnimation();
            imgItem.setVisibility(View.INVISIBLE);

            String imageUrl = INGREDIENT_IMAGE_BASE_URL + item.getName() + ".png";

            Glide.with(itemView.getContext())
                    .load(imageUrl)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(GlideException e, Object model,
                                                    Target<Drawable> target, boolean isFirstResource) {
                            lottieLoadingThumb.setVisibility(View.GONE);
                            lottieLoadingThumb.cancelAnimation();
                            imgItem.setVisibility(View.VISIBLE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model,
                                                       Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            lottieLoadingThumb.setVisibility(View.GONE);
                            lottieLoadingThumb.cancelAnimation();
                            imgItem.setVisibility(View.VISIBLE);
                            return false;
                        }
                    })
                    .error(R.drawable.ic_error)
                    .into(imgItem);

            cardItem.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onIngredientClick(item);
                }
            });
        }
    }
}
