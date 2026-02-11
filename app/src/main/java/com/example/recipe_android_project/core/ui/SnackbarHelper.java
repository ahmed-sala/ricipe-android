package com.example.recipe_android_project.core.ui;

import android.graphics.Color;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.recipe_android_project.R;
import com.google.android.material.snackbar.Snackbar;

public class SnackbarHelper {

    private static final int UNDO_DURATION = 3000;

    public static void showError(@NonNull View view, @NonNull String message) {
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG);
        snackbar.setBackgroundTint(ContextCompat.getColor(view.getContext(), R.color.error));
        snackbar.setTextColor(ContextCompat.getColor(view.getContext(), R.color.white));
        snackbar.show();
    }

    public static void showError(@NonNull View view, @NonNull String message,
                                 String actionText, View.OnClickListener action) {
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG);
        snackbar.setBackgroundTint(ContextCompat.getColor(view.getContext(), R.color.error));
        snackbar.setTextColor(ContextCompat.getColor(view.getContext(), R.color.white));
        snackbar.setActionTextColor(ContextCompat.getColor(view.getContext(), R.color.white));
        snackbar.setAction(actionText, action);
        snackbar.show();
    }

    public static void showSuccess(@NonNull View view, @NonNull String message) {
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT);
        snackbar.setBackgroundTint(ContextCompat.getColor(view.getContext(), R.color.success));
        snackbar.setTextColor(ContextCompat.getColor(view.getContext(), R.color.white));
        snackbar.show();
    }

    public static void showWarning(@NonNull View view, @NonNull String message) {
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG);
        snackbar.setBackgroundTint(ContextCompat.getColor(view.getContext(), R.color.warning));
        snackbar.setTextColor(ContextCompat.getColor(view.getContext(), R.color.white));
        snackbar.show();
    }


    public static void showUndoSnackbar(@NonNull View view,
                                        @NonNull String message,
                                        @NonNull String undoText,
                                        @Nullable View.OnClickListener onUndoClick) {
        Snackbar snackbar = Snackbar.make(view, message, UNDO_DURATION);
        snackbar.setBackgroundTint(ContextCompat.getColor(view.getContext(), R.color.gray_700));
        snackbar.setTextColor(ContextCompat.getColor(view.getContext(), R.color.white));
        snackbar.setActionTextColor(ContextCompat.getColor(view.getContext(), R.color.primary));

        if (onUndoClick != null) {
            snackbar.setAction(undoText, onUndoClick);
        }

        snackbar.show();
    }
    public static void showInfo(View view, String message) {
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG);
        snackbar.setBackgroundTint(ContextCompat.getColor(view.getContext(), R.color.info));
        snackbar.setTextColor(Color.WHITE);
        snackbar.show();
    }
}
