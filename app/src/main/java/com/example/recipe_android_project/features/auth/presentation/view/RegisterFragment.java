package com.example.recipe_android_project.features.auth.presentation.view;

import android.content.Intent;
import android.content.res.ColorStateList;
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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
import com.example.recipe_android_project.core.utils.PasswordHasher.PasswordStrength;
import com.example.recipe_android_project.features.auth.presentation.contract.RegisterContract;
import com.example.recipe_android_project.features.auth.presentation.presenter.RegisterPresenter;
import com.example.recipe_android_project.features.dashboard.presentation.view.DashboardActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class RegisterFragment extends Fragment implements RegisterContract.View {

    private MaterialCardView btnClose;
    private MaterialButton btnRegister, btnGoogle;
    private TextView tvBottom;

    private TextInputLayout nameTil, emailTil, passTil;
    private TextInputEditText nameEt, emailEt, passEt;

    private LinearLayout passwordStrengthLayout;
    private ProgressBar passwordStrengthBar;
    private TextView passwordStrengthText;

    private RegisterPresenter presenter;
    private LoadingDialog loadingDialog;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
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
        presenter = new RegisterPresenter(requireContext());
        presenter.attachView(this);
        loadingDialog = new LoadingDialog(requireContext());
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


        btnRegister.setEnabled(false);
        if (passwordStrengthLayout != null) {
            passwordStrengthLayout.setVisibility(View.GONE);
        }
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
                    navigateToLogin();
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

    private void setupClickListeners() {
        btnClose.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().finish();
            }
        });

        btnGoogle.setOnClickListener(v ->
                showErrorSnackbar("Google Sign-Up coming soon!")
        );

        btnRegister.setOnClickListener(v -> {
            String name = getTextFromEditText(nameEt);
            String email = getTextFromEditText(emailEt);
            String password = getTextFromEditText(passEt);
            presenter.register(name, email, password);
        });
    }

    private void setupTextWatchers() {
        nameEt.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                presenter.onNameChanged(s.toString());
            }
        });

        nameEt.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String name = getTextFromEditText(nameEt);
                if (!name.isEmpty()) {
                    presenter.validateName(name);
                }
            }
        });

        emailEt.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                presenter.onEmailChanged(s.toString());
            }
        });

        emailEt.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String email = getTextFromEditText(emailEt);
                if (!email.isEmpty()) {
                    presenter.validateEmail(email);
                    presenter.checkEmailExists(email);
                }
            }
        });

        passEt.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                String password = s.toString();

                if (passwordStrengthLayout != null) {
                    passwordStrengthLayout.setVisibility(
                            password.isEmpty() ? View.GONE : View.VISIBLE
                    );
                }

                presenter.onPasswordChanged(password);
            }
        });

        passEt.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String password = getTextFromEditText(passEt);
                if (!password.isEmpty()) {
                    presenter.validatePassword(password);
                }
            }
        });
    }

    private abstract static class SimpleTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
    }

    private String getTextFromEditText(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }


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
        btnGoogle.setEnabled(enabled);
        btnClose.setEnabled(enabled);
    }

    @Override
    public void showNameError(String message) {
        nameTil.setError(message);
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
    public void clearNameError() {
        nameTil.setError(null);
    }

    @Override
    public void clearEmailError() {
        emailTil.setError(null);
    }

    @Override
    public void clearPasswordError() {
        passTil.setError(null);
    }

    @Override
    public void clearErrors() {
        nameTil.setError(null);
        emailTil.setError(null);
        passTil.setError(null);
    }

    @Override
    public void setRegisterButtonEnabled(boolean enabled) {
        btnRegister.setEnabled(enabled);
    }

    @Override
    public void showPasswordStrengthIndicator(PasswordStrength strength, String message) {
        if (passwordStrengthLayout == null || passwordStrengthBar == null || passwordStrengthText == null) {
            return;
        }

        passwordStrengthLayout.setVisibility(View.VISIBLE);

        int progress;
        int color;

        switch (strength) {
            case WEAK:
                progress = 33;
                color = ContextCompat.getColor(requireContext(), R.color.error);
                break;
            case MEDIUM:
                progress = 66;
                color = ContextCompat.getColor(requireContext(), R.color.warning);
                break;
            case STRONG:
                progress = 100;
                color = ContextCompat.getColor(requireContext(), R.color.success);
                break;
            default:
                progress = 0;
                color = ContextCompat.getColor(requireContext(), R.color.error);
        }

        passwordStrengthBar.setProgress(progress);
        passwordStrengthBar.setProgressTintList(ColorStateList.valueOf(color));
        passwordStrengthText.setText(message);
        passwordStrengthText.setTextColor(color);
    }

    @Override
    public void hidePasswordStrengthIndicator() {
        if (passwordStrengthLayout != null) {
            passwordStrengthLayout.setVisibility(View.GONE);
        }
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
    public void navigateToLogin() {
        Navigation.findNavController(requireView())
                .navigate(R.id.action_registerFragment_to_loginFragment);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (loadingDialog != null) {
            loadingDialog.dismiss();
        }
        if (presenter != null) {
            presenter.onDestroy();
        }
    }
}
