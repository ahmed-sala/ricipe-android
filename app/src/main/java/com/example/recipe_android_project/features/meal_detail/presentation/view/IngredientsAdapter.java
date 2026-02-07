package com.example.recipe_android_project.features.meal_detail.presentation.view;

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
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.recipe_android_project.R;
import com.example.recipe_android_project.features.home.model.Ingredient;

import java.util.List;

public class IngredientsAdapter extends RecyclerView.Adapter<IngredientsAdapter.ViewHolder> {

    private List<Ingredient> ingredients;

    public IngredientsAdapter(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ingredient, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Ingredient ingredient = ingredients.get(position);

        holder.tvIngredientName.setText(ingredient.getName());

        holder.tvIngredientMeasure.setText(ingredient.getMeasure());

        holder.showLoading();

        Glide.with(holder.itemView.getContext())
                .load(ingredient.getImageUrl())
                .transition(DrawableTransitionOptions.withCrossFade(300))
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                Target<Drawable> target, boolean isFirstResource) {
                        holder.hideLoading();
                        holder.ivIngredient.setVisibility(View.VISIBLE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model,
                                                   Target<Drawable> target, DataSource dataSource,
                                                   boolean isFirstResource) {
                        holder.hideLoading();
                        holder.ivIngredient.setVisibility(View.VISIBLE);
                        return false;
                    }
                })
                .error(R.drawable.ic_error)
                .into(holder.ivIngredient);
    }

    @Override
    public int getItemCount() {
        return ingredients != null ? ingredients.size() : 0;
    }

    public void updateData(List<Ingredient> newIngredients) {
        this.ingredients = newIngredients;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIngredient;
        TextView tvIngredientName;
        TextView tvIngredientMeasure;
        LottieAnimationView lottieIngredientLoading;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIngredient = itemView.findViewById(R.id.ivIngredient);
            tvIngredientName = itemView.findViewById(R.id.tvIngredientName);
            tvIngredientMeasure = itemView.findViewById(R.id.tvIngredientMeasure);
            lottieIngredientLoading = itemView.findViewById(R.id.lottieIngredientLoading);
        }

        void showLoading() {
            if (lottieIngredientLoading != null) {
                lottieIngredientLoading.setVisibility(View.VISIBLE);
                lottieIngredientLoading.playAnimation();
            }
            if (ivIngredient != null) {
                ivIngredient.setVisibility(View.INVISIBLE);
            }
        }

        void hideLoading() {
            if (lottieIngredientLoading != null) {
                lottieIngredientLoading.cancelAnimation();
                lottieIngredientLoading.setVisibility(View.GONE);
            }
        }
    }
}
