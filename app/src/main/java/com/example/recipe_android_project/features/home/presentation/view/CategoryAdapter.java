package com.example.recipe_android_project.features.home.presentation.view;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.example.recipe_android_project.features.home.model.Category;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.VH> {

    private final List<Category> items;
    private int selectedIndex = 0;
    private OnCategoryClickListener listener;

    private static final int COLOR_PRIMARY = 0xFFFF7A1A;   // Orange
    private static final int COLOR_WHITE = 0xFFFFFFFF;
    private static final int COLOR_TEXT_DARK = 0xFF1A1A1A;

    public interface OnCategoryClickListener {
        void onCategoryClick(Category item, int position);
    }

    public CategoryAdapter(List<Category> items, OnCategoryClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    public void setItems(List<Category> newItems) {
        items.clear();
        items.addAll(newItems);
        selectedIndex = -1;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Category item = items.get(position);

        holder.tv.setText(item.getName());

        boolean isSelected = position == selectedIndex;

        if (isSelected) {
            holder.container.setBackgroundResource(R.drawable.bg_category_selected);
            holder.iconContainer.setBackgroundResource(R.drawable.bg_icon_circle_selected);
            holder.tv.setTextColor(COLOR_WHITE);
            holder.itemView.setElevation(8f);
        } else {
            holder.container.setBackgroundResource(R.drawable.bg_category_unselected);
            holder.iconContainer.setBackgroundResource(R.drawable.bg_icon_circle_unselected);
            holder.tv.setTextColor(COLOR_TEXT_DARK);
            holder.itemView.setElevation(0f);
        }

        holder.imgLoader.setVisibility(View.VISIBLE);
        holder.imgLoader.playAnimation();

        Glide.with(holder.img)
                .load(item.getThumbnailUrl())
                .centerCrop()
                .placeholder(R.drawable.category_icon)
                .error(R.drawable.ic_error)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                Target<Drawable> target, boolean isFirstResource) {
                        holder.imgLoader.cancelAnimation();
                        holder.imgLoader.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model,
                                                   Target<Drawable> target, DataSource dataSource,
                                                   boolean isFirstResource) {
                        holder.imgLoader.cancelAnimation();
                        holder.imgLoader.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(holder.img);


        holder.itemView.setOnClickListener(v -> {
            int oldIndex = selectedIndex;
            int newIndex = holder.getAdapterPosition();
            if (newIndex == RecyclerView.NO_POSITION) return;

            selectedIndex = newIndex;

            if (oldIndex != -1) notifyItemChanged(oldIndex);
            notifyItemChanged(selectedIndex);

            if (listener != null) listener.onCategoryClick(item, newIndex);
        });
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public void setSelectedIndex(int index) {
        int oldIndex = selectedIndex;
        selectedIndex = index;
        notifyItemChanged(oldIndex);
        notifyItemChanged(selectedIndex);
    }

    @Override
    public void onViewRecycled(@NonNull VH holder) {
        super.onViewRecycled(holder);

        holder.imgLoader.cancelAnimation();
        holder.imgLoader.setVisibility(View.GONE);

        Glide.with(holder.img).clear(holder.img);
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView img;
        TextView tv;
        LinearLayout container;
        FrameLayout iconContainer;
        LottieAnimationView imgLoader;

        VH(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.imgCat);
            tv = itemView.findViewById(R.id.tvCat);
            container = itemView.findViewById(R.id.containerCategory);
            iconContainer = itemView.findViewById(R.id.iconContainer);
            imgLoader = itemView.findViewById(R.id.lottieImgLoading);
        }
    }
}
