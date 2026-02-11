package com.example.recipe_android_project.core.helper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.recipe_android_project.features.auth.data.repository.AuthRepository;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SyncManager {

    private static final String TAG = "SyncManager";
    private static final int SYNC_DELAY_MS = 3000;
    private static final int MAX_RETRY_ATTEMPTS = 3;

    private static volatile SyncManager instance;

    private AuthRepository authRepository;
    private final CompositeDisposable compositeDisposable;
    private final Context context;
    private final ConnectivityManager connectivityManager;
    private final Handler mainHandler;

    private ConnectivityManager.NetworkCallback networkCallback;
    private volatile boolean isSyncing = false;
    private volatile boolean isCallbackRegistered = false;
    private int retryCount = 0;
    private Runnable pendingSyncRunnable;

    private SyncManager(Context context) {
        Log.e(TAG, "★★★ SyncManager CONSTRUCTOR ★★★");

        this.context = context.getApplicationContext();
        this.compositeDisposable = new CompositeDisposable();
        this.connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        this.mainHandler = new Handler(Looper.getMainLooper());

        Log.e(TAG, "★★★ SyncManager CREATED ★★★");
    }

    public static SyncManager getInstance(Context context) {
        if (instance == null) {
            synchronized (SyncManager.class) {
                if (instance == null) {
                    instance = new SyncManager(context);
                }
            }
        }
        return instance;
    }

    private AuthRepository getAuthRepository() {
        if (authRepository == null) {
            synchronized (this) {
                if (authRepository == null) {
                    authRepository = new AuthRepository(context);
                }
            }
        }
        return authRepository;
    }

    public void startListening() {
        Log.e(TAG, "★★★ startListening() ★★★");
        registerNetworkCallback();

        if (isNetworkReallyAvailable()) {
            Log.e(TAG, "★★★ Network already available on start ★★★");
            scheduleSyncWithDelay();
        }
    }

    public void stopListening() {
        Log.e(TAG, "★★★ stopListening() ★★★");
        unregisterNetworkCallback();
        cancelPendingSync();
    }

    private void registerNetworkCallback() {
        Log.e(TAG, "★★★ registerNetworkCallback() - isRegistered: " + isCallbackRegistered + " ★★★");

        if (isCallbackRegistered) {
            Log.e(TAG, "Already registered, skipping");
            return;
        }

        if (connectivityManager == null) {
            Log.e(TAG, "ERROR: ConnectivityManager is null!");
            return;
        }

        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                Log.e(TAG, "═══════════════════════════════════════");
                Log.e(TAG, "★★★ NETWORK AVAILABLE ★★★");
                Log.e(TAG, "═══════════════════════════════════════");
                scheduleSyncWithDelay();
            }

            @Override
            public void onLost(@NonNull Network network) {
                Log.e(TAG, "═══════════════════════════════════════");
                Log.e(TAG, "★★★ NETWORK LOST ★★★");
                Log.e(TAG, "═══════════════════════════════════════");
                isSyncing = false;
                cancelPendingSync();
            }

            @Override
            public void onCapabilitiesChanged(@NonNull Network network,
                                              @NonNull NetworkCapabilities caps) {
                boolean hasInternet = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
                boolean validated = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);

                Log.e(TAG, "★★★ Capabilities changed - internet:" + hasInternet + " validated:" + validated + " ★★★");

                if (hasInternet && validated && !isSyncing) {
                    scheduleSyncWithDelay();
                }
            }
        };

        try {
            NetworkRequest request = new NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .build();

            connectivityManager.registerNetworkCallback(request, networkCallback);
            isCallbackRegistered = true;
            Log.e(TAG, "★★★ NETWORK CALLBACK REGISTERED OK ★★★");

        } catch (Exception e) {
            Log.e(TAG, "★★★ FAILED TO REGISTER CALLBACK: " + e.getMessage() + " ★★★");
            e.printStackTrace();
            isCallbackRegistered = false;
        }
    }

    private void unregisterNetworkCallback() {
        if (networkCallback != null && connectivityManager != null && isCallbackRegistered) {
            try {
                connectivityManager.unregisterNetworkCallback(networkCallback);
                Log.e(TAG, "Callback unregistered");
            } catch (Exception e) {
                Log.e(TAG, "Unregister error: " + e.getMessage());
            }
        }
        networkCallback = null;
        isCallbackRegistered = false;
    }

    private void scheduleSyncWithDelay() {
        cancelPendingSync();
        Log.e(TAG, "★★★ Scheduling sync in " + SYNC_DELAY_MS + "ms ★★★");

        pendingSyncRunnable = this::syncAllPendingData;
        mainHandler.postDelayed(pendingSyncRunnable, SYNC_DELAY_MS);
    }

    private void cancelPendingSync() {
        if (pendingSyncRunnable != null) {
            mainHandler.removeCallbacks(pendingSyncRunnable);
            pendingSyncRunnable = null;
        }
    }

    private boolean isNetworkReallyAvailable() {
        if (connectivityManager == null) return false;

        Network network = connectivityManager.getActiveNetwork();
        if (network == null) return false;

        NetworkCapabilities caps = connectivityManager.getNetworkCapabilities(network);
        if (caps == null) return false;

        boolean result = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);

        Log.e(TAG, "isNetworkReallyAvailable: " + result);
        return result;
    }

    @SuppressLint("CheckResult")
    public void syncAllPendingData() {
        Log.e(TAG, "═══════════════════════════════════════");
        Log.e(TAG, "★★★ syncAllPendingData() ★★★");
        Log.e(TAG, "isSyncing: " + isSyncing);
        Log.e(TAG, "networkAvailable: " + isNetworkReallyAvailable());
        Log.e(TAG, "═══════════════════════════════════════");

        if (isSyncing) {
            Log.e(TAG, "Already syncing, skip");
            return;
        }

        if (!isNetworkReallyAvailable()) {
            Log.e(TAG, "No network, skip");
            return;
        }

        boolean loggedIn = false;
        try {
            loggedIn = getAuthRepository().isSessionLoggedIn();
        } catch (Exception e) {
            Log.e(TAG, "Error checking login: " + e.getMessage());
        }

        Log.e(TAG, "User logged in: " + loggedIn);

        if (!loggedIn) {
            Log.e(TAG, "User not logged in, skip sync");
            return;
        }

        isSyncing = true;
        Log.e(TAG, "★★★ STARTING SYNC OPERATIONS ★★★");

        Disposable disposable = syncPendingRegistrations()
                .doOnComplete(() -> Log.e(TAG, "✓ Registrations synced"))
                .andThen(syncPendingPasswordChanges())
                .doOnComplete(() -> Log.e(TAG, "✓ Passwords synced"))
                .andThen(syncPendingUserUpdates())
                .doOnComplete(() -> Log.e(TAG, "✓ User updates synced"))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> {
                            isSyncing = false;
                            Log.e(TAG, "★★★ ALL SYNC COMPLETED ★★★");
                        },
                        error -> {
                            isSyncing = false;
                            Log.e(TAG, "★★★ SYNC ERROR: " + error.getMessage() + " ★★★");

                            if (retryCount < MAX_RETRY_ATTEMPTS && isNetworkReallyAvailable()) {
                                retryCount++;
                                mainHandler.postDelayed(this::syncAllPendingData, 5000);
                            }
                        }
                );

        compositeDisposable.add(disposable);
    }

    public void forceSyncNow() {
        Log.e(TAG, "★★★ FORCE SYNC NOW ★★★");
        isSyncing = false;
        cancelPendingSync();
        syncAllPendingData();
    }

    private Completable syncPendingRegistrations() {
        return Completable.defer(() -> {
            if (!isNetworkReallyAvailable()) return Completable.complete();
            return getAuthRepository().syncPendingRegistrations().onErrorComplete();
        });
    }

    private Completable syncPendingPasswordChanges() {
        return Completable.defer(() -> {
            if (!isNetworkReallyAvailable()) return Completable.complete();
            return getAuthRepository().syncPendingPasswordChanges().onErrorComplete();
        });
    }

    private Completable syncPendingUserUpdates() {
        return Completable.defer(() -> {
            if (!isNetworkReallyAvailable()) return Completable.complete();
            return getAuthRepository().syncAllPendingUserUpdates().onErrorComplete();
        });
    }

    public boolean isSyncing() { return isSyncing; }
    public boolean isCallbackRegistered() { return isCallbackRegistered; }

    public void dispose() {
        compositeDisposable.clear();
        cancelPendingSync();
        unregisterNetworkCallback();
        isSyncing = false;
    }
}
