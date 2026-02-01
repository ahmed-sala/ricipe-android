package com.example.recipe_android_project.core.ui;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.recipe_android_project.R;
import com.google.android.material.button.MaterialButton;

public class AlertDialogHelper {

    public interface OnDialogClickListener {
        void onClick();
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
}
