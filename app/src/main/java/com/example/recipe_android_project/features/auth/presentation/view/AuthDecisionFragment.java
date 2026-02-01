package com.example.recipe_android_project.features.auth.presentation.view;

import android.content.Intent;
import android.graphics.Color;
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

import com.example.recipe_android_project.MainActivity;
import com.example.recipe_android_project.R;
import com.google.android.material.button.MaterialButton;

public class AuthDecisionFragment extends Fragment {

    private MaterialButton btnSignUpEmail;
    private MaterialButton btnSignIn;
    private MaterialButton btnGoogle;
    private TextView btnGuest;
    private TextView textTerms;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_auth_decision, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupTermsText();
        setupClickListeners();
    }

    private void initViews(View view) {
        btnSignUpEmail = view.findViewById(R.id.btnSignUpEmail);
        btnSignIn = view.findViewById(R.id.btnSignIn);
        btnGoogle = view.findViewById(R.id.btnGoogle);
        btnGuest = view.findViewById(R.id.textGuest);
        textTerms = view.findViewById(R.id.textTerms);
    }

    private void setupTermsText() {
        String termsWord = getString(R.string.terms);
        String privacyWord = getString(R.string.privacy_policy);

        String fullText = getString(R.string.terms_privacy_full, termsWord, privacyWord);

        SpannableString spannable = new SpannableString(fullText);
        int primaryColor = ContextCompat.getColor(requireContext(), R.color.primary);

        int termsStart = fullText.indexOf(termsWord);
        int termsEnd = termsStart + termsWord.length();

        int privacyStart = fullText.indexOf(privacyWord);
        int privacyEnd = privacyStart + privacyWord.length();

        if (termsStart == -1 || privacyStart == -1) {
            textTerms.setText(fullText);
            return;
        }

        ClickableSpan termsSpan = new ClickableSpan() {
            @Override public void onClick(@NonNull View widget) { openTerms(); }
            @Override public void updateDrawState(@NonNull TextPaint ds) {
                ds.setColor(primaryColor);
                ds.setUnderlineText(false);
                ds.setFakeBoldText(true);
            }
        };

        ClickableSpan privacySpan = new ClickableSpan() {
            @Override public void onClick(@NonNull View widget) { openPrivacy(); }
            @Override public void updateDrawState(@NonNull TextPaint ds) {
                ds.setColor(primaryColor);
                ds.setUnderlineText(false);
                ds.setFakeBoldText(true);
            }
        };

        spannable.setSpan(termsSpan, termsStart, termsEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(privacySpan, privacyStart, privacyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        textTerms.setText(spannable);
        textTerms.setMovementMethod(LinkMovementMethod.getInstance());
        textTerms.setHighlightColor(Color.TRANSPARENT);
    }
    private void openTerms() {

    }

    private void openPrivacy() {

    }

    private void setupClickListeners() {
        btnSignUpEmail.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_authDecisionFragment_to_registerFragment));

        btnSignIn.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_authDecisionFragment_to_loginFragment));

        btnGoogle.setOnClickListener(v -> {
        });

        btnGuest.setOnClickListener(v -> navigateToMain());
    }

    private void navigateToMain() {
        Intent intent = new Intent(requireActivity(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}
