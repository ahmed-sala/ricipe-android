package com.example.recipe_android_project.features.auth.view;

import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.recipe_android_project.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class RegisterFragment extends Fragment {

    private MaterialCardView btnClose;
    private MaterialButton btnRegister, btnGoogle;
    private TextView tvBottom;

    private TextInputLayout nameTil, emailTil, passTil;
    private TextInputEditText nameEt, emailEt, passEt;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupBottomText();
        setupClickListeners(view);
    }

    private void initViews(View view) {
        btnClose = view.findViewById(R.id.btnClose);
        btnRegister = view.findViewById(R.id.btnRegister);
        btnGoogle = view.findViewById(R.id.btnGoogle);
        tvBottom = view.findViewById(R.id.tvBottom);

        nameTil = view.findViewById(R.id.nameTil);
        nameEt = view.findViewById(R.id.nameEt);

        emailTil = view.findViewById(R.id.emailTil);
        emailEt = view.findViewById(R.id.emailEt);

        passTil = view.findViewById(R.id.passTil);
        passEt = view.findViewById(R.id.passEt);
    }

    private void setupBottomText() {
        String full = "Already have an account? Log In";
        String action = "Log In";

        SpannableString ss = new SpannableString(full);
        int primary = ContextCompat.getColor(requireContext(), R.color.primary);

        int start = full.indexOf(action);
        int end = start + action.length();

        if (start >= 0) {
            ClickableSpan loginSpan = new ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    Navigation.findNavController(requireView())
                            .navigate(R.id.action_registerFragment_to_loginFragment);
                }

                @Override
                public void updateDrawState(@NonNull TextPaint ds) {
                    ds.setColor(primary);
                    ds.setUnderlineText(false);
                    ds.setFakeBoldText(true);
                }
            };
            ss.setSpan(loginSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        tvBottom.setText(ss);
        tvBottom.setMovementMethod(LinkMovementMethod.getInstance());
        tvBottom.setHighlightColor(android.graphics.Color.TRANSPARENT);
    }

    private void setupClickListeners(View view) {
        btnClose.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());

        btnGoogle.setOnClickListener(v -> {
            // TODO: Google sign-up (same as login)
        });

        btnRegister.setOnClickListener(v -> {
            String name = (nameEt.getText() != null) ? nameEt.getText().toString().trim() : "";
            String email = (emailEt.getText() != null) ? emailEt.getText().toString().trim() : "";
            String pass = (passEt.getText() != null) ? passEt.getText().toString() : "";

            if (name.isEmpty()) {
                nameTil.setError("Full name is required");
                return;
            } else {
                nameTil.setError(null);
            }

            if (email.isEmpty()) {
                emailTil.setError("Email is required");
                return;
            } else {
                emailTil.setError(null);
            }

            if (pass.isEmpty()) {
                passTil.setError("Password is required");
                return;
            } else {
                passTil.setError(null);
            }

            // TODO: your register logic
        });
    }
}
