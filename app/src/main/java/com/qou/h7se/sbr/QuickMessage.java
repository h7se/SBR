package com.qou.h7se.sbr;

import android.app.Activity;
import android.graphics.Color;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Created by k0de9x on 10/6/2015.
 */

public class QuickMessage {
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }

    public static QuickMessage instance = null;

    private Queue<String> queue;
    private Handler handler;
    private Activity activity;
    private TextSwitcher switcher;
    private Animation in, out, vanish;
    public int msg_display_duration = 1600;

    public QuickMessage(final Activity activity, int switcherId) {

        this.activity = activity;
        this.queue = new ArrayDeque<>();

        this.switcher = (TextSwitcher) activity.findViewById(switcherId);
        this.switcher.setFactory(new ViewSwitcher.ViewFactory() {
            public View makeView() {
                TextView txt = new TextView(activity);
                txt.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                // myText.setTextSize(36);
                txt.setTextColor(Color.YELLOW);
                txt.setMaxLines(1);
                txt.setEllipsize(TextUtils.TruncateAt.END);
                return txt;
            }
        });

        this.in = AnimationUtils.loadAnimation(activity, R.anim.anim_slide_in_up);
       // this.out = AnimationUtils.loadAnimation(activity, R.anim.anim_slide_out_down);
        this.vanish = AnimationUtils.loadAnimation(activity, R.anim.anim_slide_out_left);

        this.handler = new Handler();
    }

    public void showMsg(final String text) {
        if (!(getVisibility())) {
            Log.e(AppEx.PACKAGE_NAME, "redirect from qmsg: " + text);
            return;
        }

        queue.add(text);

        if(queue.size() == 1) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (queue.isEmpty()) {
                        switcher.setOutAnimation(vanish);
                        switcher.setText(null);
                    } else {
                        switcher.setOutAnimation(in);
                        switcher.setText(queue.remove());

                        handler.postDelayed(
                                this, msg_display_duration + 300);
                    }
                }
            }, 1);
        }
    }

    public boolean getVisibility() {
       return (switcher.getVisibility() == View.VISIBLE);
    }

    public void setVisibility(boolean value) {
        if(value) {
            switcher.setVisibility(View.VISIBLE);
        } else {
            switcher.setVisibility(View.GONE);
        }
    }
}