package com.qou.h7se.sbr;

import android.os.Handler;
import android.widget.Toast;

/**
 * Created by k0de9x on 10/19/2015.
 */
public class BackPressHandler {
    public static BackPressHandler
            instance = new BackPressHandler();

    private Toast toast = null;
    private boolean pressed = false;

    void prompt(String text, int duration, int timeout) {

        BackPressHandler.instance.pressed = true;

        if(toast != null) {
            toast.cancel();
        }

        toast = Toast.makeText(
                AppEx.self.getApplicationContext(), text, Toast.LENGTH_LONG);

        toast.show();

        if(duration <= 3500) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    toast.cancel();
                }
            }, duration);
        }

        reset(timeout, false);
    }

    void reset(int timeout, final boolean cancel) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                pressed = false;
                if (cancel) {
                    toast.cancel();
                }
            }
        }, timeout);
    }

    public boolean isPressed() {
        return pressed;
    }
}