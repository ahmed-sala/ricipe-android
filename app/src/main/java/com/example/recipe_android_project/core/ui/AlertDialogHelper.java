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
    public interface OnPlanExistsDialogListener {
        void onReplace();
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
    public static Dialog showPlanExistsDialog(
            @NonNull Context context,
            @NonNull String existingMealName,
            @NonNull String newMealName,
            @NonNull String date,
            @NonNull String mealType,
            @Nullable OnPlanExistsDialogListener listener
    ) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_plan_exists, null);
        dialog.setContentView(view);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(
                    (int) (context.getResources().getDisplayMetrics().widthPixels * 0.9),
                    WindowManager.LayoutParams.WRAP_CONTENT
            );
            dialog.getWindow().setGravity(Gravity.CENTER);
        }

        TextView tvExistingMealName = view.findViewById(R.id.tvExistingMealName);
        TextView tvPlanInfo = view.findViewById(R.id.tvPlanInfo);
        MaterialButton btnReplace = view.findViewById(R.id.btnReplace);
        MaterialButton btnCancel = view.findViewById(R.id.btnCancel);
        MaterialCardView iconContainer = view.findViewById(R.id.iconContainer);

        tvExistingMealName.setText("\"" + existingMealName + "\"");

        String formattedMealType = capitalizeFirst(mealType);
        String formattedDate = formatDateForDisplay(date);
        tvPlanInfo.setText(formattedMealType + " • " + formattedDate);

        if (iconContainer != null) {
            Animation pulseAnimation = AnimationUtils.loadAnimation(context, R.anim.pulse);
            iconContainer.startAnimation(pulseAnimation);
        }

        btnReplace.setOnClickListener(v -> {
            dialog.dismiss();
            if (listener != null) {
                listener.onReplace();
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
    public static Dialog showRemovePlanDialog(
            @NonNull Context context,
            @NonNull String mealName,
            @NonNull String date,
            @NonNull String mealType,
            @Nullable OnConfirmDialogListener listener
    ) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_remove_plan, null);
        dialog.setContentView(view);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(
                    (int) (context.getResources().getDisplayMetrics().widthPixels * 0.9),
                    WindowManager.LayoutParams.WRAP_CONTENT
            );
            dialog.getWindow().setGravity(Gravity.CENTER);
        }

        TextView tvMealName = view.findViewById(R.id.tvMealName);
        TextView tvPlanInfo = view.findViewById(R.id.tvPlanInfo);
        MaterialButton btnRemove = view.findViewById(R.id.btnRemove);
        MaterialButton btnCancel = view.findViewById(R.id.btnCancel);
        MaterialCardView iconContainer = view.findViewById(R.id.iconContainer);

        if (mealName != null && !mealName.isEmpty()) {
            tvMealName.setText("\"" + mealName + "\"");
            tvMealName.setVisibility(View.VISIBLE);
        } else {
            tvMealName.setVisibility(View.GONE);
        }

        String formattedMealType = capitalizeFirst(mealType);
        String formattedDate = formatDateForDisplay(date);
        tvPlanInfo.setText(formattedMealType + " • " + formattedDate);

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
    private static String capitalizeFirst(String text) {
        if (text == null || text.isEmpty()) return text;
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }
    private static String formatDateForDisplay(String dateString) {
        if (dateString == null || dateString.isEmpty()) return "";

        try {
            String[] parts = dateString.split("-");
            if (parts.length != 3) return dateString;

            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]) - 1;
            int day = Integer.parseInt(parts[2]);

            String[] months = {"January", "February", "March", "April", "May", "June",
                    "July", "August", "September", "October", "November", "December"};

            String suffix = getDaySuffix(day);
            return day + suffix + " " + months[month] + " " + year;
        } catch (Exception e) {
            return dateString;
        }
    }

    private static String getDaySuffix(int day) {
        if (day >= 11 && day <= 13) {
            return "th";
        }
        switch (day % 10) {
            case 1: return "st";
            case 2: return "nd";
            case 3: return "rd";
            default: return "th";
        }
    }
}
