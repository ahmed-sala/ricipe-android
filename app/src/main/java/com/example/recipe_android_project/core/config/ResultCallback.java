package com.example.recipe_android_project.core.config;

public interface ResultCallback<T> {
    void onSuccess(T result);
    void onError(Exception e);
}
