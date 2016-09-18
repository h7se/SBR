package com.qou.h7se.sbr;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by k0de9x on 10/6/2015.
 */

public class AlarmBroadcastReceiver extends BasicReceiver {
    public static Map<Integer, PendingIntent> map = new HashMap<>();

    @Override
    public void onReceive(final Context context, Intent intent) {

       // AppEx.DisableStrictMode();

        Log.e(AppEx.PACKAGE_NAME, "alarm");

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(context, MainActivity.class);
                i.putExtra("schedule.backup", true);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
            }
        }, 1);

       // AppEx.EnableStrictMode();

    }

    static class Helper {
       private static final int ID_BASE = 9123260;

       static Helper instance = new
               Helper(AppEx.self.getApplicationContext());

        private Context context;
        private AlarmManager alarmManager;

        public Helper(Context context) {
            this.context = context;
            this.alarmManager = (AlarmManager) context
                    .getSystemService(Context.ALARM_SERVICE);
        }

        PendingIntent getPendingIntent(int id) {
             return PendingIntent.getBroadcast(context,
                     ID_BASE + id, new Intent(context, AlarmBroadcastReceiver.class), 0);
        }

        void start(boolean[] days, int hour, int minute, boolean disabled, boolean daily, boolean weekly) {

            if(disabled) {
                cancel();
            } else {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());

                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);

                if(daily) {
                    this.alarmManager.set(AlarmManager.RTC_WAKEUP,
                            calendar.getTimeInMillis(),  getPendingIntent(1));
                } else if(weekly) {
                    for(int i=0, size=days.length; i<size; i++) {
                        if(days[i]) {
                            int d = Calendar.FRIDAY;
                            if(i==0) {
                                d = Calendar.SATURDAY;
                            } else if(i==1) {
                                d = Calendar.SUNDAY;
                            } else if(i==2) {
                                d = Calendar.MONDAY;
                            } else if(i==3) {
                                d = Calendar.TUESDAY;
                            } else if(i==4) {
                                d = Calendar.WEDNESDAY;
                            } else if(i==5) {
                                d = Calendar.THURSDAY;
                            } else if(i==6) {
                                d = Calendar.FRIDAY;
                            }
                            calendar.set(Calendar.DAY_OF_WEEK, d);
                            this.alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                                    calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY,  getPendingIntent(++i));
                        }
                    }
                }
            }
        }

        public void cancel() {
            for(int i=0; i < 7; i++) {
                alarmManager.cancel(getPendingIntent(++i));
            }
        }
    }
}

