package com.example.recipe_android_project.core.helper;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

public class NetworkMonitor {

    public interface NetworkCallback {
        void onNetworkAvailable();
        void onNetworkLost();
    }

    private final ConnectivityManager connectivityManager;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private ConnectivityManager.NetworkCallback networkCallback;
    private NetworkCallback listener;
    private boolean isRegistered = false;
    private volatile boolean lastKnownState = true;

    private static final int NETWORK_LOST_DELAY_MS = 300;
    private Runnable pendingLostRunnable;

    public NetworkMonitor(Context context) {
        this.connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    public void setListener(NetworkCallback listener) {
        this.listener = listener;
    }

    public void startMonitoring() {
        if (isRegistered || connectivityManager == null) return;

        lastKnownState = isNetworkAvailable();
        if (!lastKnownState && listener != null) {
            mainHandler.post(() -> {
                if (listener != null) {
                    listener.onNetworkLost();
                }
            });
        }

        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                cancelPendingLost();

                if (!lastKnownState) {
                    lastKnownState = true;
                    if (listener != null) {
                        listener.onNetworkAvailable();
                    }
                }
            }

            @Override
            public void onLost(@NonNull Network network) {
                scheduleLostCheck();
            }

            @Override
            public void onCapabilitiesChanged(@NonNull Network network,
                                              @NonNull NetworkCapabilities caps) {
                boolean validated =
                        caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                                && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);

                if (validated && !lastKnownState) {
                    cancelPendingLost();
                    lastKnownState = true;
                    if (listener != null) {
                        listener.onNetworkAvailable();
                    }
                } else if (!validated && lastKnownState) {
                    scheduleLostCheck();
                }
            }
        };

        try {
            NetworkRequest request = new NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .build();
            connectivityManager.registerNetworkCallback(request, networkCallback);
            isRegistered = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void scheduleLostCheck() {
        cancelPendingLost();

        pendingLostRunnable = () -> {
            if (!isNetworkAvailable() && lastKnownState) {
                lastKnownState = false;
                if (listener != null) {
                    listener.onNetworkLost();
                }
            }
        };
        mainHandler.postDelayed(pendingLostRunnable, NETWORK_LOST_DELAY_MS);
    }

    private void cancelPendingLost() {
        if (pendingLostRunnable != null) {
            mainHandler.removeCallbacks(pendingLostRunnable);
            pendingLostRunnable = null;
        }
    }

    public void stopMonitoring() {
        cancelPendingLost();

        if (networkCallback != null
                && connectivityManager != null
                && isRegistered) {
            try {
                connectivityManager.unregisterNetworkCallback(networkCallback);
            } catch (Exception ignored) {}
        }
        isRegistered = false;
        networkCallback = null;
    }

    public boolean isNetworkAvailable() {
        if (connectivityManager == null) return false;

        Network network = connectivityManager.getActiveNetwork();
        if (network == null) return false;

        NetworkCapabilities caps =
                connectivityManager.getNetworkCapabilities(network);
        if (caps == null) return false;

        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
    }
}
