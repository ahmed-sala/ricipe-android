package com.example.recipe_android_project.core.helper;

import android.content.Context;
import android.os.CancellationSignal;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialCancellationException;
import androidx.credentials.exceptions.GetCredentialException;
import androidx.credentials.exceptions.NoCredentialException;
import androidx.fragment.app.FragmentActivity;

import com.example.recipe_android_project.core.config.AuthConstants;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;

import java.util.concurrent.Executors;

public class GoogleSignInHelper {

    private static final String TAG = "GoogleSignInHelper";

    public interface GoogleSignInCallback {
        void onSuccess(String idToken);
        void onError(String errorMessage);
        void onCancelled();
    }

    private final CredentialManager credentialManager;
    private CancellationSignal cancellationSignal;

    public GoogleSignInHelper(Context context) {
        this.credentialManager = CredentialManager.create(context);
    }

    public void signIn(FragmentActivity activity, GoogleSignInCallback callback) {
        try {
            cancel();
            cancellationSignal = new CancellationSignal();

            GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(AuthConstants.GOOGLE_WEB_CLIENT_ID)
                    .setAutoSelectEnabled(false)
                    .build();

            GetCredentialRequest request = new GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build();

            Log.d(TAG, "Starting Google Sign-In (primary method)...");

            credentialManager.getCredentialAsync(
                    activity,
                    request,
                    cancellationSignal,
                    Executors.newSingleThreadExecutor(),
                    new CredentialManagerCallback<GetCredentialResponse,
                            GetCredentialException>() {
                        @Override
                        public void onResult(GetCredentialResponse result) {
                            Log.d(TAG, "Primary method returned result");
                            handleSignInResult(result, callback);
                        }

                        @Override
                        public void onError(@NonNull GetCredentialException e) {
                            Log.e(TAG, "Primary method failed: "
                                    + e.getClass().getSimpleName(), e);

                            if (e instanceof NoCredentialException) {
                                Log.d(TAG, "Falling back to "
                                        + "GetSignInWithGoogleOption...");
                                activity.runOnUiThread(() ->
                                        signInWithGoogleButton(activity, callback));
                            } else {
                                handleSignInError(e, callback);
                            }
                        }
                    }
            );
        } catch (Exception e) {
            Log.e(TAG, "Failed to start Google Sign-In", e);
            callback.onError("Failed to start Google Sign-In: " + e.getMessage());
        }
    }

    private void signInWithGoogleButton(FragmentActivity activity,
                                        GoogleSignInCallback callback) {
        try {
            cancel();
            cancellationSignal = new CancellationSignal();

            GetSignInWithGoogleOption signInOption =
                    new GetSignInWithGoogleOption.Builder(
                            AuthConstants.GOOGLE_WEB_CLIENT_ID)
                            .build();

            GetCredentialRequest request = new GetCredentialRequest.Builder()
                    .addCredentialOption(signInOption)
                    .build();

            Log.d(TAG, "Starting Google Sign-In (fallback method)...");

            credentialManager.getCredentialAsync(
                    activity,
                    request,
                    cancellationSignal,
                    Executors.newSingleThreadExecutor(),
                    new CredentialManagerCallback<GetCredentialResponse,
                            GetCredentialException>() {
                        @Override
                        public void onResult(GetCredentialResponse result) {
                            Log.d(TAG, "Fallback method returned result");
                            handleSignInResult(result, callback);
                        }

                        @Override
                        public void onError(@NonNull GetCredentialException e) {
                            Log.e(TAG, "Fallback method also failed", e);
                            handleSignInError(e, callback);
                        }
                    }
            );
        } catch (Exception e) {
            Log.e(TAG, "Fallback sign-in failed", e);
            callback.onError("Google Sign-In failed: " + e.getMessage());
        }
    }

    private void handleSignInResult(GetCredentialResponse response,
                                    GoogleSignInCallback callback) {
        try {
            Credential credential = response.getCredential();

            if (credential instanceof CustomCredential) {
                CustomCredential customCredential = (CustomCredential) credential;

                if (GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                        .equals(customCredential.getType())) {

                    GoogleIdTokenCredential googleCredential =
                            GoogleIdTokenCredential.createFrom(
                                    customCredential.getData());

                    String idToken = googleCredential.getIdToken();

                    if (idToken != null && !idToken.isEmpty()) {
                        Log.d(TAG, "Google ID token obtained successfully");
                        callback.onSuccess(idToken);
                    } else {
                        callback.onError("Failed to get Google ID token");
                    }
                } else {
                    callback.onError("Unexpected credential type: "
                            + customCredential.getType());
                }
            } else {
                callback.onError("Unexpected credential format");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing Google credential", e);
            callback.onError("Failed to parse Google credential: "
                    + e.getMessage());
        }
    }

    private void handleSignInError(GetCredentialException e,
                                   GoogleSignInCallback callback) {
        if (e instanceof GetCredentialCancellationException) {
            Log.d(TAG, "User cancelled Google Sign-In");
            callback.onCancelled();
        } else if (e instanceof NoCredentialException) {
            Log.e(TAG, "No credential available even after fallback");
            callback.onError(
                    "No Google accounts found. Please add a Google account "
                            + "in your device Settings → Accounts → Add Account → Google"
            );
        } else {
            String message = e.getMessage() != null
                    ? e.getMessage()
                    : "Google Sign-In failed";
            Log.e(TAG, "Sign-in error: " + message);
            callback.onError(message);
        }
    }

    public void cancel() {
        if (cancellationSignal != null && !cancellationSignal.isCanceled()) {
            cancellationSignal.cancel();
        }
    }
}
