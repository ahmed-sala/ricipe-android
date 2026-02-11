package com.example.recipe_android_project.features.profile.presentation.view;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.recipe_android_project.R;
import com.example.recipe_android_project.core.ui.AlertDialogHelper;
import com.example.recipe_android_project.core.ui.LoadingDialog;
import com.example.recipe_android_project.features.profile.presentation.contract.ChangePasswordContract;
import com.example.recipe_android_project.features.profile.presentation.presenter.ChangePasswordPresenter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class ChangePasswordFragment extends Fragment implements ChangePasswordContract.View {

    private TextInputLayout oldPasswordTil, newPasswordTil, confirmPasswordTil;
    private TextInputEditText oldPasswordEt, newPasswordEt, confirmPasswordEt;
    private MaterialButton btnChangePassword;
    private MaterialCardView btnBack;
    private TextView tvOfflineInfo;

    private ChangePasswordPresenter presenter;
    private LoadingDialog loadingDialog;

    public ChangePasswordFragment() {}


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter = new ChangePasswordPresenter(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_change_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        presenter.attachView(this);
        setupTextWatchers();
        setupClickListeners();
    }

    private void initViews(View view) {
        oldPasswordTil = view.findViewById(R.id.oldPasswordTil);
        newPasswordTil = view.findViewById(R.id.newPasswordTil);
        confirmPasswordTil = view.findViewById(R.id.confirmPasswordTil);

        oldPasswordEt = view.findViewById(R.id.oldPasswordEt);
        newPasswordEt = view.findViewById(R.id.newPasswordEt);
        confirmPasswordEt = view.findViewById(R.id.confirmPasswordEt);

        btnChangePassword = view.findViewById(R.id.btnChangePassword);
        btnBack = view.findViewById(R.id.btnBack);
        tvOfflineInfo = view.findViewById(R.id.tv_offline_info);

        loadingDialog = new LoadingDialog(requireContext());
    }

    private void setupTextWatchers() {
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                validateForm();
            }
        };

        oldPasswordEt.addTextChangedListener(textWatcher);
        newPasswordEt.addTextChangedListener(textWatcher);
        confirmPasswordEt.addTextChangedListener(textWatcher);
    }

    private void validateForm() {
        String oldPassword = oldPasswordEt.getText() != null ?
                oldPasswordEt.getText().toString() : "";
        String newPassword = newPasswordEt.getText() != null ?
                newPasswordEt.getText().toString() : "";
        String confirmPassword = confirmPasswordEt.getText() != null ?
                confirmPasswordEt.getText().toString() : "";

        presenter.validatePasswords(oldPassword, newPassword, confirmPassword);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> handleBackPress());

        btnChangePassword.setOnClickListener(v -> {
            hideKeyboard();

            String oldPassword = oldPasswordEt.getText() != null ?
                    oldPasswordEt.getText().toString().trim() : "";
            String newPassword = newPasswordEt.getText() != null ?
                    newPasswordEt.getText().toString().trim() : "";
            String confirmPassword = confirmPasswordEt.getText() != null ?
                    confirmPasswordEt.getText().toString().trim() : "";

            presenter.changePassword(oldPassword, newPassword, confirmPassword);
        });
    }

    private void handleBackPress() {
        if (getActivity() != null) {
            hideKeyboard();
            Navigation.findNavController(requireView()).navigateUp();
        }
    }

    private void hideKeyboard() {
        if (getActivity() != null && getView() != null) {
            InputMethodManager imm = (InputMethodManager) getActivity()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
            }
        }
    }

    @Override
    public void showLoading() {
        if (loadingDialog != null && !loadingDialog.isShowing()) {
            loadingDialog.show(getString(R.string.changing_password));
        }
        btnChangePassword.setEnabled(false);
    }

    @Override
    public void hideLoading() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
        validateForm();
    }

    @Override
    public void showOldPasswordError(String error) {
        if (oldPasswordTil != null) {
            oldPasswordTil.setError(error);
        }
    }

    @Override
    public void showNewPasswordError(String error) {
        if (newPasswordTil != null) {
            newPasswordTil.setError(error);
        }
    }

    @Override
    public void showConfirmPasswordError(String error) {
        if (confirmPasswordTil != null) {
            confirmPasswordTil.setError(error);
        }
    }

    @Override
    public void clearErrors() {
        if (oldPasswordTil != null) oldPasswordTil.setError(null);
        if (newPasswordTil != null) newPasswordTil.setError(null);
        if (confirmPasswordTil != null) confirmPasswordTil.setError(null);
    }

    @Override
    public void showSuccess(String message) {
        if (getContext() != null) {
            AlertDialogHelper.showSuccessDialog(
                    getContext(),
                    getString(R.string.success),
                    message,
                    this::navigateBack
            );
        }
    }

    @Override
    public void showError(String message) {
        if (getContext() != null) {
            AlertDialogHelper.showErrorDialog(getContext(), message);
        }
    }

    @Override
    public void showOfflineMessage() {
        if (tvOfflineInfo != null) {
            tvOfflineInfo.setVisibility(View.VISIBLE);
            tvOfflineInfo.setText(R.string.password_will_sync_when_online);
        }
    }

    @Override
    public void navigateBack() {
        if (getView() != null) {
            Navigation.findNavController(getView()).navigateUp();
        }
    }

    @Override
    public void setChangeButtonEnabled(boolean enabled) {
        if (btnChangePassword != null) {
            btnChangePassword.setEnabled(enabled);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }

        if (presenter != null) {
            presenter.detachView();
        }
    }
}
