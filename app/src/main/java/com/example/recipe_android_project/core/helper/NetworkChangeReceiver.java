package com.example.recipe_android_project.core.helper;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.util.Log;

import com.example.recipe_android_project.core.helper.SyncManager;

public class NetworkChangeReceiver extends BroadcastReceiver {

    private static final String TAG = "NetworkChangeReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(TAG, "★★★ onReceive called ★★★");

        if (isNetworkAvailable(context)) {
            Log.e(TAG, "★★★ Network is available via BroadcastReceiver ★★★");

            // Trigger sync
            try {
                SyncManager syncManager = SyncManager.getInstance(context);
                syncManager.forceSyncNow();
            } catch (Exception e) {
                Log.e(TAG, "Error triggering sync: " + e.getMessage());
            }
        } else {
            Log.e(TAG, "★★★ Network is NOT available ★★★");
        }
    }

    private boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm == null) return false;

        Network network = cm.getActiveNetwork();
        if (network == null) return false;

        NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
        if (capabilities == null) return false;

        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
    }
}
