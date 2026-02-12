package com.example.recipe_android_project.features.profile.presentation.view;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.recipe_android_project.R;
import com.example.recipe_android_project.core.ui.AlertDialogHelper;
import com.example.recipe_android_project.core.ui.LoadingDialog;
import com.example.recipe_android_project.core.ui.SnackbarHelper;
import com.example.recipe_android_project.features.auth.domain.model.ProfileData;
import com.example.recipe_android_project.features.profile.presentation.contract.EditProfileContract;
import com.example.recipe_android_project.features.profile.presentation.presenter.EditProfilePresenter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class EditProfileFragment extends Fragment implements EditProfileContract.View {

    private MaterialCardView btnBack;

    private TextInputLayout fullNameTil, emailTil;
    private TextInputEditText fullNameEt, emailEt;
    private MaterialButton btnSave;
    private ImageView ivProfileImage;

    private EditProfilePresenter presenter;
    private LoadingDialog loadingDialog;

    private ProfileData profileData;

    public EditProfileFragment() {
    }

    public static EditProfileFragment newInstance() {
        return new EditProfileFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            EditProfileFragmentArgs args = EditProfileFragmentArgs.fromBundle(getArguments());
            profileData = args.getProfileData();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initPresenter();
        setupClickListeners();
        setupTextWatchers();
        setupBackPressHandler();

        if (profileData != null) {
            presenter.initWithProfileData(profileData);
        }
    }

    private void initPresenter() {
        presenter = new EditProfilePresenter(requireContext());
        presenter.attachView(this);
        loadingDialog = new LoadingDialog(requireContext());
    }

    private void initViews(View view) {
        btnBack = view.findViewById(R.id.btnBack);
        ivProfileImage = view.findViewById(R.id.iv_profile_image);
        fullNameTil = view.findViewById(R.id.fullNameTil);
        fullNameEt = view.findViewById(R.id.fullNameEt);
        emailTil = view.findViewById(R.id.emailTil);
        emailEt = view.findViewById(R.id.emailEt);
        btnSave = view.findViewById(R.id.btnSave);
        btnSave.setEnabled(false);
    }

    private void setupClickListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> handleBackPress());
        }

        btnSave.setOnClickListener(v -> {
            String fullName = getTextFromEditText(fullNameEt);
            String email = getTextFromEditText(emailEt);
            presenter.saveProfile(fullName, email);
        });

        if (ivProfileImage != null) {
            ivProfileImage.setOnClickListener(v -> {
                showErrorSnackbar("Change photo coming soon!");
            });
        }
    }

    private void setupTextWatchers() {
        fullNameEt.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                presenter.onFullNameChanged(s.toString());
            }
        });

        fullNameEt.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String fullName = getTextFromEditText(fullNameEt);
                if (!fullName.isEmpty()) {
                    presenter.validateFullName(fullName);
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
                }
            }
        });
    }

    private void setupBackPressHandler() {
        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        handleBackPress();
                    }
                }
        );
    }

    private void handleBackPress() {
        String currentFullName = getTextFromEditText(fullNameEt);
        String currentEmail = getTextFromEditText(emailEt);
        presenter.onBackPressed(currentFullName, currentEmail);
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
        setFormEnabled(false);
    }

    @Override
    public void hideLoading() {
        loadingDialog.dismiss();
        setFormEnabled(true);
    }

    private void setFormEnabled(boolean enabled) {
        if (fullNameEt != null) fullNameEt.setEnabled(enabled);
        if (emailEt != null) emailEt.setEnabled(enabled);
        if (btnSave != null) btnSave.setEnabled(enabled);
        if (btnBack != null) {
            btnBack.setEnabled(enabled);
            btnBack.setClickable(enabled);
        }
    }

    @Override
    public void displayUserData(String fullName, String email) {
        if (fullNameEt != null) fullNameEt.setText(fullName);
        if (emailEt != null) emailEt.setText(email);
    }

    @Override
    public void showFullNameError(String message) {
        fullNameTil.setError(message);
    }

    @Override
    public void showEmailError(String message) {
        emailTil.setError(message);
    }

    @Override
    public void clearFullNameError() {
        fullNameTil.setError(null);
    }

    @Override
    public void clearEmailError() {
        emailTil.setError(null);
    }

    @Override
    public void clearErrors() {
        fullNameTil.setError(null);
        emailTil.setError(null);
    }

    @Override
    public void setSaveButtonEnabled(boolean enabled) {
        btnSave.setEnabled(enabled);
    }

    @Override
    public void showErrorDialog(String message) {
        AlertDialogHelper.showErrorDialog(requireContext(), message);
    }

    @Override
    public void showSuccessDialog(String message, Runnable onDismiss) {
        AlertDialogHelper.showSuccessDialog(requireContext(), message, onDismiss::run);
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
    public void showOfflineUpdateSnackbar() {
        SnackbarHelper.showInfo(requireView(), "Saved offline. Will sync when online.");
    }

    @Override
    public void navigateBack() {
        if (getView() != null) {
            Navigation.findNavController(getView()).popBackStack();
        }
    }

    @Override
    public void showDiscardChangesDialog(Runnable onDiscard) {
        AlertDialogHelper.showConfirmDialog(
                requireContext(),
                "Discard Changes?",
                "You have unsaved changes. Are you sure you want to discard them?",
                "Discard",
                "Keep Editing",
                new AlertDialogHelper.OnConfirmDialogListener() {
                    @Override
                    public void onConfirm() {
                        onDiscard.run();
                    }

                    @Override
                    public void onCancel() {
                    }
                }
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (loadingDialog != null) loadingDialog.dismiss();
        if (presenter != null) presenter.onDestroy();
    }
}
