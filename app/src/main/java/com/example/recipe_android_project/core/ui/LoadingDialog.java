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

public class LoadingDialog {

    private final Dialog dialog;
    private final TextView tvMessage;

    public LoadingDialog(@NonNull Context context) {
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        View view = LayoutInflater.from(context).inflate(R.layout.loading_dialog, null);
        dialog.setContentView(view);

        tvMessage = view.findViewById(R.id.tvLoadingMessage);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
    }

    public void show() {
        if (!dialog.isShowing()) {
            dialog.show();
        }
    }

    public void show(String message) {
        tvMessage.setText(message);
        show();
    }

    public void dismiss() {
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    public void setMessage(String message) {
        tvMessage.setText(message);
    }

    public boolean isShowing() {
        return dialog.isShowing();
    }
}
