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
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.Navigation;

import com.example.recipe_android_project.R;
import com.example.recipe_android_project.core.helper.GoogleSignInHelper;
import com.example.recipe_android_project.core.ui.AlertDialogHelper;
import com.example.recipe_android_project.core.ui.LoadingDialog;
import com.example.recipe_android_project.core.ui.SnackbarHelper;
import com.example.recipe_android_project.features.auth.data.repository.AuthRepository;
import com.example.recipe_android_project.features.dashboard.presentation.view.DashboardActivity;
import com.google.android.material.button.MaterialButton;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;

public class AuthDecisionFragment extends Fragment {

    private MaterialButton btnSignUpEmail;
    private MaterialButton btnSignIn;
    private MaterialButton btnGoogle;
    private TextView btnGuest;
    private TextView textTerms;

    private GoogleSignInHelper googleSignInHelper;
    private AuthRepository authRepository;
    private LoadingDialog loadingDialog;
    private CompositeDisposable disposables;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_auth_decision, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        googleSignInHelper = new GoogleSignInHelper(requireContext());
        authRepository = new AuthRepository(requireContext());
        loadingDialog = new LoadingDialog(requireContext());
        disposables = new CompositeDisposable();

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
            @Override
            public void onClick(@NonNull View widget) { openTerms(); }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                ds.setColor(primaryColor);
                ds.setUnderlineText(false);
                ds.setFakeBoldText(true);
            }
        };

        ClickableSpan privacySpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) { openPrivacy(); }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
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
                Navigation.findNavController(v)
                        .navigate(R.id.action_authDecisionFragment_to_registerFragment));

        btnSignIn.setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_authDecisionFragment_to_loginFragment));

        btnGoogle.setOnClickListener(v -> startGoogleSignIn());

        btnGuest.setOnClickListener(v -> navigateToMain());
    }

    private void startGoogleSignIn() {
        FragmentActivity activity = getActivity();
        if (activity == null) return;

        googleSignInHelper.signIn(activity, new GoogleSignInHelper.GoogleSignInCallback() {
            @Override
            public void onSuccess(String idToken) {
                activity.runOnUiThread(() -> handleGoogleSignIn(idToken));
            }

            @Override
            public void onError(String errorMessage) {
                activity.runOnUiThread(() ->
                        SnackbarHelper.showError(requireView(), errorMessage));
            }

            @Override
            public void onCancelled() {
            }
        });
    }

    private void handleGoogleSignIn(String idToken) {
        loadingDialog.show("Signing in with Google…");
        setButtonsEnabled(false);

        Disposable disposable = authRepository.signInWithGoogle(idToken)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        user -> {
                            loadingDialog.dismiss();
                            setButtonsEnabled(true);

                            String name = user.getFirstName() != null
                                    && !user.getFirstName().isEmpty()
                                    ? user.getFirstName()
                                    : "there";

                            AlertDialogHelper.showSuccessDialog(
                                    requireContext(),
                                    "Welcome, " + name + "!",
                                    this::navigateToDashboard
                            );
                        },
                        error -> {
                            loadingDialog.dismiss();
                            setButtonsEnabled(true);

                            AlertDialogHelper.showErrorDialog(
                                    requireContext(),
                                    "Google sign-in failed: " + error.getMessage()
                            );
                        }
                );

        disposables.add(disposable);
    }

    private void setButtonsEnabled(boolean enabled) {
        btnSignUpEmail.setEnabled(enabled);
        btnSignIn.setEnabled(enabled);
        btnGoogle.setEnabled(enabled);
        btnGuest.setEnabled(enabled);
    }

    private void navigateToDashboard() {
        Intent intent = new Intent(requireActivity(), DashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    private void navigateToMain() {
        Intent intent = new Intent(requireActivity(), DashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (googleSignInHelper != null) {
            googleSignInHelper.cancel();
        }
        if (loadingDialog != null) {
            loadingDialog.dismiss();
        }
        if (disposables != null) {
            disposables.clear();
        }
    }
}
