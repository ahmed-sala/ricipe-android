package com.example.recipe_android_project.core.helper;

import java.lang.ref.WeakReference;

public abstract class BasePresenter<V> {

    private WeakReference<V> viewRef;

    public void attachView(V view) {
        viewRef = new WeakReference<>(view);
    }

    public void detachView() {
        if (viewRef != null) {
            viewRef.clear();
            viewRef = null;
        }
    }

    protected V getView() {
        return viewRef != null ? viewRef.get() : null;
    }

    protected boolean isViewAttached() {
        return viewRef != null && viewRef.get() != null;
    }
}
