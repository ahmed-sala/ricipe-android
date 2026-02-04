package com.example.recipe_android_project.features.search.presentation.view;

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
import com.example.recipe_android_project.features.home.model.Area;
import com.example.recipe_android_project.features.search.presentation.view.listeners.OnAreaClickListener;
import com.google.android.material.card.MaterialCardView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.graphics.drawable.Drawable;

public class AreaAdapter extends RecyclerView.Adapter<AreaAdapter.AreaViewHolder> {

    private List<Area> items;
    private final OnAreaClickListener listener;

    private static final Map<String, String> COUNTRY_CODES = new HashMap<>();
    static {
        COUNTRY_CODES.put("Algerian", "dz");
        COUNTRY_CODES.put("American", "us");
        COUNTRY_CODES.put("Argentinian", "ar");
        COUNTRY_CODES.put("Australian", "au");
        COUNTRY_CODES.put("British", "gb");
        COUNTRY_CODES.put("Canadian", "ca");
        COUNTRY_CODES.put("Chinese", "cn");
        COUNTRY_CODES.put("Croatian", "hr");
        COUNTRY_CODES.put("Dutch", "nl");
        COUNTRY_CODES.put("Egyptian", "eg");
        COUNTRY_CODES.put("Filipino", "ph");
        COUNTRY_CODES.put("French", "fr");
        COUNTRY_CODES.put("Greek", "gr");
        COUNTRY_CODES.put("Indian", "in");
        COUNTRY_CODES.put("Irish", "ie");
        COUNTRY_CODES.put("Italian", "it");
        COUNTRY_CODES.put("Jamaican", "jm");
        COUNTRY_CODES.put("Japanese", "jp");
        COUNTRY_CODES.put("Kenyan", "ke");
        COUNTRY_CODES.put("Malaysian", "my");
        COUNTRY_CODES.put("Mexican", "mx");
        COUNTRY_CODES.put("Moroccan", "ma");
        COUNTRY_CODES.put("Norwegian", "no");
        COUNTRY_CODES.put("Polish", "pl");
        COUNTRY_CODES.put("Portuguese", "pt");
        COUNTRY_CODES.put("Russian", "ru");
        COUNTRY_CODES.put("Saudi Arabian", "sa");
        COUNTRY_CODES.put("Slovakian", "sk");
        COUNTRY_CODES.put("Spanish", "es");
        COUNTRY_CODES.put("Syrian", "sy");
        COUNTRY_CODES.put("Thai", "th");
        COUNTRY_CODES.put("Tunisian", "tn");
        COUNTRY_CODES.put("Turkish", "tr");
        COUNTRY_CODES.put("Ukrainian", "ua");
        COUNTRY_CODES.put("Uruguayan", "uy");
        COUNTRY_CODES.put("Venezuelan", "ve");
        COUNTRY_CODES.put("Venezulan", "ve");
        COUNTRY_CODES.put("Vietnamese", "vn");
    }



    public AreaAdapter(List<Area> items, OnAreaClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AreaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_grid_card, parent, false);
        return new AreaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AreaViewHolder holder, int position) {
        Area item = items.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public void updateItems(List<Area> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    static class AreaViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardItem;
        private final ImageView imgItem;
        private final TextView tvTitle;
        private final LottieAnimationView lottieLoadingThumb;

        public AreaViewHolder(@NonNull View itemView) {
            super(itemView);
            cardItem = itemView.findViewById(R.id.cardItem);
            imgItem = itemView.findViewById(R.id.imgItem);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            lottieLoadingThumb = itemView.findViewById(R.id.lottieLoadingThumb);
        }

        public void bind(Area item, OnAreaClickListener listener) {
            tvTitle.setText(item.getName());

            lottieLoadingThumb.setVisibility(View.VISIBLE);
            lottieLoadingThumb.playAnimation();
            imgItem.setVisibility(View.INVISIBLE);

            String countryCode = COUNTRY_CODES.get(item.getName());
            if (countryCode != null) {
                String flagUrl = "https://flagcdn.com/w160/" + countryCode + ".png";

                Glide.with(itemView.getContext())
                        .load(flagUrl)
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
            } else {
                lottieLoadingThumb.setVisibility(View.GONE);
                lottieLoadingThumb.cancelAnimation();
                imgItem.setVisibility(View.VISIBLE);
                imgItem.setImageResource(R.drawable.ic_error);
            }

            cardItem.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAreaClick(item);
                }
            });
        }
    }
}
