package com.example.recipe_android_project.core.ui;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.recipe_android_project.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class AlertDialogHelper {

    public interface OnDialogClickListener {
        void onClick();
    }

    public interface OnConfirmDialogListener {
        void onConfirm();
        void onCancel();
    }


    public static Dialog showErrorDialog(
            @NonNull Context context,
            @NonNull String title,
            @NonNull String message,
            OnDialogClickListener listener
    ) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        View view = LayoutInflater.from(context).inflate(R.layout.error_dialog, null);
        dialog.setContentView(view);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextView tvTitle = view.findViewById(R.id.tvErrorTitle);
        TextView tvMessage = view.findViewById(R.id.tvErrorMessage);
        MaterialButton btnOk = view.findViewById(R.id.btnOk);

        tvTitle.setText(title);
        tvMessage.setText(message);

        btnOk.setOnClickListener(v -> {
            dialog.dismiss();
            if (listener != null) {
                listener.onClick();
            }
        });

        dialog.setCancelable(true);
        dialog.show();

        return dialog;
    }

    public static Dialog showErrorDialog(
            @NonNull Context context,
            @NonNull String message
    ) {
        return showErrorDialog(context, context.getString(R.string.oops), message, null);
    }


    public static Dialog showSuccessDialog(
            @NonNull Context context,
            @NonNull String title,
            @NonNull String message,
            OnDialogClickListener listener
    ) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        View view = LayoutInflater.from(context).inflate(R.layout.success_dialog, null);
        dialog.setContentView(view);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextView tvTitle = view.findViewById(R.id.tvSuccessTitle);
        TextView tvMessage = view.findViewById(R.id.tvSuccessMessage);
        MaterialButton btnOk = view.findViewById(R.id.btnOk);

        tvTitle.setText(title);
        tvMessage.setText(message);

        btnOk.setOnClickListener(v -> {
            dialog.dismiss();
            if (listener != null) {
                listener.onClick();
            }
        });

        dialog.setCancelable(false);
        dialog.show();

        return dialog;
    }

    public static Dialog showSuccessDialog(
            @NonNull Context context,
            @NonNull String message,
            OnDialogClickListener listener
    ) {
        return showSuccessDialog(context, context.getString(R.string.success), message, listener);
    }


    public static Dialog showConfirmDialog(
            @NonNull Context context,
            @NonNull String title,
            @NonNull String message,
            @NonNull String confirmText,
            @NonNull String cancelText,
            OnConfirmDialogListener listener
    ) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        View view = LayoutInflater.from(context).inflate(R.layout.confirm_dialog, null);
        dialog.setContentView(view);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextView tvTitle = view.findViewById(R.id.tvConfirmTitle);
        TextView tvMessage = view.findViewById(R.id.tvConfirmMessage);
        MaterialButton btnConfirm = view.findViewById(R.id.btnConfirm);
        MaterialButton btnCancel = view.findViewById(R.id.btnCancel);

        tvTitle.setText(title);
        tvMessage.setText(message);
        btnConfirm.setText(confirmText);
        btnCancel.setText(cancelText);

        btnConfirm.setOnClickListener(v -> {
            dialog.dismiss();
            if (listener != null) {
                listener.onConfirm();
            }
        });

        btnCancel.setOnClickListener(v -> {
            dialog.dismiss();
            if (listener != null) {
                listener.onCancel();
            }
        });

        dialog.setCancelable(true);
        dialog.show();

        return dialog;
    }

    public static Dialog showConfirmDialog(
            @NonNull Context context,
            @NonNull String title,
            @NonNull String message,
            OnConfirmDialogListener listener
    ) {
        return showConfirmDialog(
                context,
                title,
                message,
                context.getString(R.string.confirm),
                context.getString(R.string.cancel),
                listener
        );
    }


    public static Dialog showRemoveFavoriteDialog(
            @NonNull Context context,
            @NonNull String mealName,
            @Nullable OnConfirmDialogListener listener
    ) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_remove_favorite, null);
        dialog.setContentView(view);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(
                    (int) (context.getResources().getDisplayMetrics().widthPixels * 0.9),
                    WindowManager.LayoutParams.WRAP_CONTENT
            );
            dialog.getWindow().setGravity(Gravity.CENTER);
        }

        TextView tvMessage = view.findViewById(R.id.tvMessage);
        TextView tvMealName = view.findViewById(R.id.tvMealName);
        MaterialButton btnRemove = view.findViewById(R.id.btnRemove);
        MaterialButton btnCancel = view.findViewById(R.id.btnCancel);
        MaterialCardView iconContainer = view.findViewById(R.id.iconContainer);

        tvMessage.setText(context.getString(R.string.remove_favorite_confirm_message));

        if (mealName != null && !mealName.isEmpty()) {
            tvMealName.setText(mealName);
            tvMealName.setVisibility(View.VISIBLE);
        } else {
            tvMealName.setVisibility(View.GONE);
        }

        if (iconContainer != null) {
            Animation pulseAnimation = AnimationUtils.loadAnimation(context, R.anim.pulse);
            iconContainer.startAnimation(pulseAnimation);
        }

        btnRemove.setOnClickListener(v -> {
            dialog.dismiss();
            if (listener != null) {
                listener.onConfirm();
            }
        });

        btnCancel.setOnClickListener(v -> {
            dialog.dismiss();
            if (listener != null) {
                listener.onCancel();
            }
        });

        dialog.setCancelable(true);
        dialog.show();

        return dialog;
    }

}
