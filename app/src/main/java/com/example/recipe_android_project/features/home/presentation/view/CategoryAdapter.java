package com.example.recipe_android_project.features.home.presentation.view;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipe_android_project.R;
import com.example.recipe_android_project.features.home.model.test.CategoryItem;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.VH> {

    private final List<CategoryItem> items;
    private int selectedIndex = 0;
    private OnCategoryClickListener listener;

    // Colors
    private static final int COLOR_PRIMARY = R.color.primary;
    private static final int COLOR_WHITE = R.color.white;
    private static final int COLOR_TEXT_DARK = R.color.dark_black;

    public interface OnCategoryClickListener {
        void onCategoryClick(CategoryItem item, int position);
    }

    public CategoryAdapter(List<CategoryItem> items) {
        this.items = items;
    }

    public CategoryAdapter(List<CategoryItem> items, OnCategoryClickListener listener) {
        this.items = items;
        this.listener = listener;
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
        CategoryItem item = items.get(position);
        holder.tv.setText(item.title);
        holder.img.setImageResource(item.iconRes);

        boolean isSelected = position == selectedIndex;

        if (isSelected) {
            holder.container.setBackgroundResource(R.drawable.bg_category_selected);
            holder.iconContainer.setBackgroundResource(R.drawable.bg_icon_circle_selected);
            holder.img.setImageTintList(ColorStateList.valueOf(COLOR_PRIMARY));
            holder.tv.setTextColor(COLOR_WHITE);

            holder.itemView.setElevation(8f);
        } else {
            holder.container.setBackgroundResource(R.drawable.bg_category_unselected);
            holder.iconContainer.setBackgroundResource(R.drawable.bg_icon_circle_unselected);
            holder.img.setImageTintList(ColorStateList.valueOf(COLOR_PRIMARY));
            holder.tv.setTextColor(COLOR_TEXT_DARK);

            holder.itemView.animate()
                    .scaleX(isSelected ? 1.05f : 1f)
                    .scaleY(isSelected ? 1.05f : 1f)
                    .setDuration(200)
                    .start();        }

        holder.itemView.setOnClickListener(v -> {
            int oldIndex = selectedIndex;
            int newIndex = holder.getAdapterPosition();

            if (newIndex != RecyclerView.NO_POSITION && oldIndex != newIndex) {
                selectedIndex = newIndex;
                notifyItemChanged(oldIndex);
                notifyItemChanged(selectedIndex);

                if (listener != null) {
                    listener.onCategoryClick(item, newIndex);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setSelectedIndex(int index) {
        int oldIndex = selectedIndex;
        selectedIndex = index;
        notifyItemChanged(oldIndex);
        notifyItemChanged(selectedIndex);
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView img;
        TextView tv;
        LinearLayout container;
        FrameLayout iconContainer;

        VH(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.imgCat);
            tv = itemView.findViewById(R.id.tvCat);
            container = itemView.findViewById(R.id.containerCategory);
            iconContainer = itemView.findViewById(R.id.iconContainer);
        }
    }
}
