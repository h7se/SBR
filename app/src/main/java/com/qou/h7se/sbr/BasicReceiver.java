package com.qou.h7se.sbr;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;

/**
 * Created by k0de9x on 10/29/2015.
 */
public abstract class BasicReceiver extends BroadcastReceiver {

    public static void enable(Context context, Class<?> receiver) {
        ComponentName componentName = new ComponentName(context, receiver);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(componentName,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    public static void disable(Context context, Class<?> receiver) {
        ComponentName componentName = new ComponentName(context, receiver);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(componentName,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }
}
