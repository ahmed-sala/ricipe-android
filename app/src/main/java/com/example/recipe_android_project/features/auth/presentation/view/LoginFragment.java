package com.example.recipe_android_project.features.auth.presentation.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextWatcher;
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
import com.example.recipe_android_project.core.ui.AlertDialogHelper;
import com.example.recipe_android_project.core.ui.LoadingDialog;
import com.example.recipe_android_project.core.ui.SnackbarHelper;
import com.example.recipe_android_project.features.auth.presentation.contract.LoginContract;
import com.example.recipe_android_project.features.auth.presentation.presenter.LoginPresenter;
import com.example.recipe_android_project.features.dashboard.view.DashboardActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginFragment extends Fragment implements LoginContract.View {

    private MaterialCardView btnClose;
    private MaterialButton btnLogin, btnGoogle;
    private TextView tvForgot, tvBottom;
    private TextInputLayout emailTil, passTil;
    private TextInputEditText emailEt, passEt;

    private LoginPresenter presenter;
    private LoadingDialog loadingDialog;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initPresenter();
        initViews(view);
        setupBottomText();
        setupClickListeners();
        setupTextWatchers();
    }

    private void initPresenter() {
        presenter = new LoginPresenter(requireContext());
        presenter.attachView(this);
        loadingDialog = new LoadingDialog(requireContext());
    }

    private void initViews(View view) {
        btnClose = view.findViewById(R.id.btnClose);
        btnLogin = view.findViewById(R.id.btnLogin);
        btnGoogle = view.findViewById(R.id.btnGoogle);
        tvForgot = view.findViewById(R.id.tvForgot);
        tvBottom = view.findViewById(R.id.tvBottom);

        emailTil = view.findViewById(R.id.emailTil);
        emailEt = view.findViewById(R.id.emailEt);
        passTil = view.findViewById(R.id.passTil);
        passEt = view.findViewById(R.id.passEt);
    }

    private void setupBottomText() {
        String full = "Don't have an account? Sign Up";
        String action = "Sign Up";

        SpannableString ss = new SpannableString(full);
        int primary = ContextCompat.getColor(requireContext(), R.color.primary);

        int start = full.indexOf(action);
        int end = start + action.length();

        if (start >= 0) {
            ClickableSpan signUpSpan = new ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    Navigation.findNavController(requireView())
                            .navigate(R.id.action_loginFragment_to_registerFragment);
                }

                @Override
                public void updateDrawState(@NonNull TextPaint ds) {
                    ds.setColor(primary);
                    ds.setUnderlineText(false);
                    ds.setFakeBoldText(true);
                }
            };
            ss.setSpan(signUpSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        tvBottom.setText(ss);
        tvBottom.setMovementMethod(LinkMovementMethod.getInstance());
        tvBottom.setHighlightColor(android.graphics.Color.TRANSPARENT);
    }

    private void setupClickListeners() {
        btnClose.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().finish();
            }
        });

        tvForgot.setOnClickListener(v -> navigateToForgotPassword());

        btnGoogle.setOnClickListener(v -> {
            // TODO: Google sign-in
            showErrorSnackbar("Google Sign-In coming soon!");
        });

        btnLogin.setOnClickListener(v -> {
            String email = getTextFromEditText(emailEt);
            String password = getTextFromEditText(passEt);
            presenter.login(email, password);
        });
    }

    private void setupTextWatchers() {
        emailEt.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                emailTil.setError(null);
            }
        });

        passEt.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                passTil.setError(null);
            }
        });
    }

    private String getTextFromEditText(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }

    // ==================== LoginContract.View Implementation ====================

    @Override
    public void showLoading(String message) {
        loadingDialog.show(message);
        setButtonsEnabled(false);
    }

    @Override
    public void hideLoading() {
        loadingDialog.dismiss();
        setButtonsEnabled(true);
    }

    private void setButtonsEnabled(boolean enabled) {
        btnLogin.setEnabled(enabled);
        btnGoogle.setEnabled(enabled);
        btnClose.setEnabled(enabled);
    }

    @Override
    public void showEmailError(String message) {
        emailTil.setError(message);
    }

    @Override
    public void showPasswordError(String message) {
        passTil.setError(message);
    }

    @Override
    public void clearErrors() {
        emailTil.setError(null);
        passTil.setError(null);
    }

    @Override
    public void showErrorDialog(String message) {
        AlertDialogHelper.showErrorDialog(requireContext(), message);
    }

    @Override
    public void showSuccessDialog(String message, Runnable onContinue) {
        AlertDialogHelper.showSuccessDialog(
                requireContext(),
                message,
                onContinue::run
        );
    }

    @Override
    public void showErrorSnackbar(String message) {
        SnackbarHelper.showError(requireView(), message);
    }

    @Override
    public void showSuccessSnackbar(String message) {
        SnackbarHelper.showSuccess(requireView(), message);
    }

    @Override
    public void navigateToHome() {
        Intent intent = new Intent(requireActivity(), DashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    @Override
    public void navigateToForgotPassword() {
        // TODO: Navigate to forgot password
        showErrorSnackbar("Forgot Password coming soon!");
    }

    // ==================== Lifecycle ====================

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (loadingDialog != null) {
            loadingDialog.dismiss();
        }
        if (presenter != null) {
            presenter.detachView();
        }
    }

    // ==================== Helper Class ====================

    private abstract static class SimpleTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
    }
}
