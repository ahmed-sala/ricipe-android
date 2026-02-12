package com.example.recipe_android_project.features.profile.presentation.view;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.recipe_android_project.R;
import com.example.recipe_android_project.core.helper.LocaleHelper;
import com.example.recipe_android_project.core.ui.AlertDialogHelper;
import com.example.recipe_android_project.features.auth.domain.model.User;
import com.example.recipe_android_project.features.auth.domain.model.ProfileData;
import com.example.recipe_android_project.features.auth.presentation.view.AuthActivity;
import com.example.recipe_android_project.features.profile.presentation.contract.ProfileContract;
import com.example.recipe_android_project.features.profile.presentation.presenter.ProfilePresenter;

import java.util.Locale;

public class ProfileFragment extends Fragment implements ProfileContract.View {

    private ImageView ivProfileImage, ivChangePhoto;
    private CardView cardChangePhoto;
    private TextView tvUserName, tvUserEmail, tvCurrentLanguage;
    private ConstraintLayout layoutChangeLanguage, layoutEditUserInfo,
            layoutChangePassword, layoutLogout;
    private ProgressBar progressBar;

    // Views needed for guest mode toggling
    private View divider1, divider2;
    private FrameLayout frameProfileImage;
    private ImageView ivLogoutIcon;
    private TextView tvLogoutLabel;
    private ImageView ivLogoutArrow;

    private ProfilePresenter presenter;

    private String currentLanguageCode = "en";
    private boolean isLoggedIn = false;

    private static final String PREFS_NAME = "user_prefs";
    private static final String KEY_LANGUAGE_CODE = "language_code";

    public ProfileFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter = new ProfilePresenter(requireContext());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        presenter.attachView(this);
        loadLanguagePreference();
        setClickListeners();
        presenter.loadUserData();
    }

    private void initViews(View view) {
        ivProfileImage = view.findViewById(R.id.iv_profile_image);
        ivChangePhoto = view.findViewById(R.id.iv_change_photo);
        cardChangePhoto = view.findViewById(R.id.card_change_photo);
        tvUserName = view.findViewById(R.id.tv_user_name);
        tvUserEmail = view.findViewById(R.id.tv_user_email);
        tvCurrentLanguage = view.findViewById(R.id.tv_current_language);
        progressBar = view.findViewById(R.id.progress_bar);

        layoutChangeLanguage = view.findViewById(R.id.layout_change_language);
        layoutEditUserInfo = view.findViewById(R.id.layout_edit_user_info);
        layoutChangePassword = view.findViewById(R.id.layout_change_password);
        layoutLogout = view.findViewById(R.id.layout_logout);

        divider1 = view.findViewById(R.id.divider_1);
        divider2 = view.findViewById(R.id.divider_2);

        ivLogoutIcon = view.findViewById(R.id.iv_logout_icon);
        tvLogoutLabel = view.findViewById(R.id.tv_logout_label);
        ivLogoutArrow = view.findViewById(R.id.iv_logout_arrow);
    }

    private void loadLanguagePreference() {
        if (getContext() != null) {
            SharedPreferences prefs = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            currentLanguageCode = prefs.getString(KEY_LANGUAGE_CODE, "en");
            updateLanguageDisplay();
        }
    }

    private void updateLanguageDisplay() {
        if (tvCurrentLanguage != null) {
            tvCurrentLanguage.setText(currentLanguageCode.equals("en") ? "English" : "العربية");
        }
    }

    private void setClickListeners() {
        if (cardChangePhoto != null) {
            cardChangePhoto.setOnClickListener(v -> {
                if (isLoggedIn) {
                    navigateToEditProfile(v);
                } else {
                    navigateToLogin();
                }
            });
        } else if (ivChangePhoto != null) {
            ivChangePhoto.setOnClickListener(v -> {
                if (isLoggedIn) {
                    navigateToEditProfile(v);
                } else {
                    navigateToLogin();
                }
            });
        }

        layoutChangePassword.setOnClickListener(v -> navigateToChangePassword());
        layoutChangeLanguage.setOnClickListener(v -> showLanguageDialog());
        layoutEditUserInfo.setOnClickListener(this::navigateToEditProfile);

        layoutLogout.setOnClickListener(v -> {
            if (isLoggedIn) {
                showLogoutDialog();
            } else {
                navigateToLogin();
            }
        });
    }


    @Override
    public void showGuestMode() {
        isLoggedIn = false;

        if (tvUserName != null) {
            tvUserName.setText("Guest");
        }
        if (tvUserEmail != null) {
            tvUserEmail.setText("Sign in to access all features");
        }

        if (cardChangePhoto != null) {
            cardChangePhoto.setVisibility(View.GONE);
        }

        if (layoutEditUserInfo != null) {
            layoutEditUserInfo.setVisibility(View.GONE);
        }
        if (divider1 != null) {
            divider1.setVisibility(View.GONE);
        }

        if (layoutChangePassword != null) {
            layoutChangePassword.setVisibility(View.GONE);
        }
        if (divider2 != null) {
            divider2.setVisibility(View.GONE);
        }

        updateLoginButton();
    }

    private void showLoggedInMode() {
        isLoggedIn = true;

        if (cardChangePhoto != null) {
            cardChangePhoto.setVisibility(View.VISIBLE);
        }

        if (layoutEditUserInfo != null) {
            layoutEditUserInfo.setVisibility(View.VISIBLE);
        }
        if (divider1 != null) {
            divider1.setVisibility(View.VISIBLE);
        }

        if (layoutChangePassword != null) {
            layoutChangePassword.setVisibility(View.VISIBLE);
        }
        if (divider2 != null) {
            divider2.setVisibility(View.VISIBLE);
        }

        updateLogoutButton();
    }

    private void updateLoginButton() {
        if (tvLogoutLabel != null) {
            tvLogoutLabel.setText("Login");
            tvLogoutLabel.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary));
        }
        if (ivLogoutIcon != null) {
            ivLogoutIcon.setImageResource(R.drawable.ic_login);
            ivLogoutIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.primary));
        }
        if (ivLogoutArrow != null) {
            ivLogoutArrow.setColorFilter(ContextCompat.getColor(requireContext(), R.color.primary));
        }
    }

    private void updateLogoutButton() {
        if (tvLogoutLabel != null) {
            tvLogoutLabel.setText("Logout");
            tvLogoutLabel.setTextColor(ContextCompat.getColor(requireContext(), R.color.error));
        }
        if (ivLogoutIcon != null) {
            ivLogoutIcon.setImageResource(R.drawable.ic_logout);
            ivLogoutIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.error));
        }
        if (ivLogoutArrow != null) {
            ivLogoutArrow.setColorFilter(ContextCompat.getColor(requireContext(), R.color.error));
        }
    }

    // ==================== NAVIGATION ====================

    private void navigateToChangePassword() {
        Navigation.findNavController(requireView())
                .navigate(R.id.action_profileFragment_to_changePasswordFragment);
    }

    private void navigateToEditProfile(View view) {
        User currentUser = presenter.getCurrentUser();

        if (currentUser != null) {
            ProfileData profileData = new ProfileData(
                    currentUser.getFullName(),
                    currentUser.getEmail()
            );
            ProfileFragmentDirections.ActionProfileFragmentToEditProfileFragment action =
                    ProfileFragmentDirections.actionProfileFragmentToEditProfileFragment(profileData);

            Navigation.findNavController(view).navigate(action);
        } else {
            Toast.makeText(getContext(), "Unable to load profile data", Toast.LENGTH_SHORT).show();
        }
    }

    // ==================== CONTRACT METHODS ====================

    @Override
    public void showLoading() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void hideLoading() {
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void showUserData(User user) {
        showLoggedInMode();

        if (user != null) {
            if (tvUserName != null) {
                tvUserName.setText(user.getFullName() != null ? user.getFullName() : "User");
            }
            if (tvUserEmail != null) {
                tvUserEmail.setText(user.getEmail() != null ? user.getEmail() : "");
            }
        }
    }

    @Override
    public void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void navigateToLogin() {
        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), AuthActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().finish();
        }
    }

    @Override
    public void showLogoutSuccess() {
        if (getContext() != null) {
            Toast.makeText(getContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void showLanguageChanged(String languageCode) {
        currentLanguageCode = languageCode;
        saveLanguagePreference(languageCode);
        updateLanguageDisplay();
        applyLanguageChange(languageCode);

        String message = languageCode.equals("en")
                ? "Language changed to English"
                : "تم تغيير اللغة إلى العربية";
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    // ==================== DIALOGS ====================

    private void showLanguageDialog() {
        if (getContext() == null) return;

        AlertDialogHelper.showLanguageDialog(
                getContext(),
                currentLanguageCode,
                new AlertDialogHelper.OnLanguageSelectedListener() {
                    @Override
                    public void onLanguageSelected(String languageCode) {
                        presenter.changeLanguage(languageCode);
                    }

                    @Override
                    public void onCancel() {
                    }
                }
        );
    }
    private void showLogoutDialog() {
        if (getContext() == null) return;

        String userEmail = null;
        if (presenter != null && presenter.getCurrentUser() != null) {
            userEmail = presenter.getCurrentUser().getEmail();
        }

        AlertDialogHelper.showLogoutDialog(
                getContext(),
                userEmail,
                new AlertDialogHelper.OnConfirmDialogListener() {
                    @Override
                    public void onConfirm() {
                        presenter.logout();
                    }

                    @Override
                    public void onCancel() {
                    }
                }
        );
    }


    private void saveLanguagePreference(String languageCode) {
        if (getContext() != null) {
            SharedPreferences prefs = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            prefs.edit()
                    .putString(KEY_LANGUAGE_CODE, languageCode)
                    .apply();
        }
    }

    private void applyLanguageChange(String languageCode) {
        if (getContext() == null) return;

        LocaleHelper.setLanguage(getContext(), languageCode);

        if (getActivity() != null) {
            getActivity().recreate();
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        if (presenter != null) {
            presenter.loadUserData();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (presenter != null) {
            presenter.detachView();
        }
    }
}
