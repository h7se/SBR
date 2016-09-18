package com.qou.h7se.sbr;

import android.content.Context;
import android.content.Intent;

public class BootBroadcastReceiver extends BasicReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            boolean disabled = (PrefsActivity.prefs.getBoolean("schedule_backup_disabled", true));

            if(!disabled) {
                boolean daily = (PrefsActivity.prefs.getBoolean("schedule_backup_daily", false));
                boolean weekly = (PrefsActivity.prefs.getBoolean("schedule_backup_weekly", false));

                if(daily || weekly) {
                    int hour = (PrefsActivity.prefs.getInt("schedule_backup_hour", 0));
                    int minute = (PrefsActivity.prefs.getInt("schedule_backup_minute", 0));

                    boolean[] days = {
                            (PrefsActivity.prefs.getBoolean("schedule_backup_day_1", false))
                            ,(PrefsActivity.prefs.getBoolean("schedule_backup_day_2", false))
                            ,(PrefsActivity.prefs.getBoolean("schedule_backup_day_3", false))
                            ,(PrefsActivity.prefs.getBoolean("schedule_backup_day_4", false))
                            ,(PrefsActivity.prefs.getBoolean("schedule_backup_day_5", false))
                            ,(PrefsActivity.prefs.getBoolean("schedule_backup_day_6", false))
                            ,(PrefsActivity.prefs.getBoolean("schedule_backup_day_7", false))
                    };

                    AlarmBroadcastReceiver.Helper.instance.start(days, hour, minute, false /*disabled*/, daily, weekly);
                }
            }
        }
    }
}
