package com.qou.h7se.sbr;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by k0de9x on 10/26/2015.
 */
public class NetworkStateReceiver extends BroadcastReceiver {
    private static Map<String,
            StorageGroup.ConnectionStatus> connectionStatusListeners = new HashMap<>();

    public static void addConnectionStatusListener(StorageGroup.ConnectionStatus listener, boolean notifyOnAddition) {
        if (!connectionStatusListeners.containsKey(listener.id())) {
            connectionStatusListeners.put(listener.id(), listener);
        }

        if (notifyOnAddition) {
            listener.setStatus(Utils.IsNetworkAvailable());
        }
    }

    public static void removeConnectionStatusListener(String id) {
        if (connectionStatusListeners.containsKey(id)) {
            connectionStatusListeners.remove(id);
        }
    }

    static void publishConnectionStatus(final boolean value) {
        List<StorageGroup.ConnectionStatus> tmp = new ArrayList<>(connectionStatusListeners.values());
        for(StorageGroup.ConnectionStatus listener : tmp) {
            listener.setStatus(value);

            if (listener.removeIf()) {
                removeConnectionStatusListener(listener.id());
            }
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(null == intent) {
            return;
        }

        NetworkInfo info;
        if(intent.getExtras() != null) {
            info = (NetworkInfo) intent.getExtras().get(ConnectivityManager.EXTRA_NETWORK_INFO);

            boolean connected = (info != null)
                    && info.isConnected()
                    && (info.getType() == ConnectivityManager.TYPE_WIFI);

            publishConnectionStatus(connected);
        }
    }

}

