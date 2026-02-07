package com.example.recipe_android_project.core.config;

import com.example.recipe_android_project.features.home.data.datasource.remote.MealApiService;
import com.example.recipe_android_project.features.meal_detail.data.datasource.remote.MealDetailApiService;
import com.example.recipe_android_project.features.search.data.datasource.remote.SearchApiService;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public final class RetrofitClient {

    private static final String BASE_URL = "https://www.themealdb.com/api/json/v1/1/";

    private static volatile Retrofit retrofit;
    private static volatile MealApiService mealApiService;
    private static volatile SearchApiService searchApiService;
    private static volatile MealDetailApiService mealDetailApiService;

    private RetrofitClient() {
    }

    private static Retrofit getInstance() {
        if (retrofit == null) {
            synchronized (RetrofitClient.class) {
                if (retrofit == null) {

                    HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                    logging.setLevel(HttpLoggingInterceptor.Level.BODY);

                    OkHttpClient okHttpClient = new OkHttpClient.Builder()
                            .connectTimeout(20, TimeUnit.SECONDS)
                            .readTimeout(30, TimeUnit.SECONDS)
                            .writeTimeout(30, TimeUnit.SECONDS)
                            .addInterceptor(logging)
                            .build();

                    retrofit = new Retrofit.Builder()
                            .baseUrl(BASE_URL)
                            .client(okHttpClient)
                            .addConverterFactory(GsonConverterFactory.create())
                            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                            .build();
                }
            }
        }
        return retrofit;
    }
    public static MealApiService getMealApiService() {
        if (mealApiService == null) {
            synchronized (RetrofitClient.class) {
                if (mealApiService == null) {
                    mealApiService = getInstance().create(MealApiService.class);
                }
            }
        }
        return mealApiService;
    }
    public static SearchApiService getSearchApiService() {
        if (searchApiService == null) {
            synchronized (RetrofitClient.class) {
                if (searchApiService == null) {
                    searchApiService = getInstance().create(SearchApiService.class);
                }
            }
        }
        return searchApiService;
    }
    public static MealDetailApiService getMealDetailApiService() {
        if (mealDetailApiService == null) {
            synchronized (RetrofitClient.class) {
                if (mealDetailApiService == null) {
                    mealDetailApiService = getInstance().create(MealDetailApiService.class);
                }
            }
        }
        return mealDetailApiService;
    }
}
