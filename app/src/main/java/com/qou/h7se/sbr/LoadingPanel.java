package com.qou.h7se.sbr;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Created by k0de9x on 10/14/2015.
 */

public class LoadingPanel {

    public static LoadingPanel instance;

    Activity activity;
    RelativeLayout relativeLayoutLoading;
    ProgressBar progressBar1;
    ProgressBar progressBar2;
    TextSwitcher switcher=null;
    Animation alpha, loading, in, out, vanish, anim;
    private Handler handler;
    private Queue<String> queue;
    private boolean disabled;
    Animators animators;


    public LoadingPanel(final Activity activity) {
        this.activity = activity;

        this.queue = new ArrayDeque<>();
        this.handler = new Handler();

        disabled = false;

        relativeLayoutLoading = (RelativeLayout) activity.findViewById(R.id.relativeLayoutLoading);
        progressBar1 = (ProgressBar) activity.findViewById(R.id.relativeLayoutLoadingProgressBar);
        progressBar2 = (ProgressBar) activity.findViewById(R.id.relativeLayoutLoadingProgressBar2);

        this.switcher = (TextSwitcher) activity.findViewById(R.id.textSwitcher2);
        this.switcher.setFactory(new ViewSwitcher.ViewFactory() {
            public View makeView() {
                TextView txt = new TextView(activity);
                txt.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                // myText.setTextSize(36);
                // txt.setTextColor(Color.YELLOW);
                txt.setMaxLines(1);
                txt.setEllipsize(TextUtils.TruncateAt.END);
                return txt;
            }
        });

        animators = new Animators(switcher.getChildAt(0));

        this.in = AnimationUtils.loadAnimation(activity, R.anim.anim_slide_in_up);
        this.out = AnimationUtils.loadAnimation(activity, R.anim.anim_slide_out_down);
        this.alpha = AnimationUtils.loadAnimation(activity, R.anim.fade_in);
        this.vanish = AnimationUtils.loadAnimation(activity, R.anim.vanish);
        this.loading = AnimationUtils.loadAnimation(activity, R.anim.loading_slide_up);
        this.anim = AnimationUtils.loadAnimation(activity, R.anim.vanish_fast);

//        imageMiddle.setBackgroundResource(R.drawable.transfer);
//        AnimationDrawable frameAnimation = (AnimationDrawable) imageMiddle.getBackground();
//        frameAnimation.start();
        switcher.setOutAnimation(alpha);
        switcher.setInAnimation(anim);
    }

    public boolean getVisibility() {
        return (relativeLayoutLoading.getVisibility() == View.VISIBLE) ;
    }

    public void setVisibility(boolean visible) {
        setVisibility(visible, "working...");
    }

    public void setVisibility(boolean visible, String text) {
        if(disabled) {
            return;
        }

        if(visible) {
            activity.getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            LoadingPanel.instance.reset();
            relativeLayoutLoading.setAnimation(loading);
            relativeLayoutLoading.setVisibility(View.VISIBLE);


            if(getAnimationType() == 0) {
                switcher.setOutAnimation(alpha);
                switcher.setInAnimation(anim);
            } else {
                switcher.setOutAnimation(null);
                switcher.setInAnimation(null);
            }


            showMsg(text);
        } else {
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            if(getVisibility()) {
                relativeLayoutLoading.setAnimation(vanish);
                relativeLayoutLoading.setVisibility(View.GONE);
            }
            LoadingPanel.instance.reset();
        }
    }

    void setProgress1Max(int value) {
        progressBar1.setMax(value);
    }

    int getProgress1Max() {
        return  progressBar1.getMax();
    }

    void setProgress2Max(int value) {
        progressBar2.setMax(value);
    }

    void incrementProgress1By(int diff) {
        progressBar1.incrementProgressBy(diff);
    }

    void incrementProgress2By(int diff) {
        progressBar2.incrementProgressBy(diff);
    }

    int getProgress1Value() {
        return progressBar1.getProgress();
    }

     void setProgress1Value(int value) {
        progressBar1.setProgress(value);
    }

    int getProgress2Value() {
        return progressBar1.getProgress();
    }

    void setProgress2Value(int value) {
        progressBar2.setProgress(value);
    }

    void resetProgress1() {
        setProgress1Value(0);
        setProgress1Max(0);
    }

    void resetProgress2() {
        setProgress2Value(0);
        setProgress2Max(0);
    }

    void reset() {
        setDisabled(false);
        resetProgress1();
        resetProgress2();
    }


    public int getAnimationType() {
        return animationType;
    }

    public void setAnimationType(int animationType) {
        this.animationType = animationType;
    }

    private int animationType = -1;

//    void setLeftImageBackground(int background) {
//        imageLeft.setBackgroundResource(background);
//    }
//    void setMiddleImageBackground(int background) {
//        imageMiddle.setBackgroundResource(background);
//    }
//    void setRightImageBackground(int background) {
//        imageRight.setBackgroundResource(background);
//    }


    public void showMsg(final String text) {
        if(disabled) {
            return;
        }

        queue.add(text);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                if(getAnimationType() == 0) {
                    if (queue.size() <= 1) {
                        animators.PLUSE.start();
                    } else {
                        animators.BOUNCE.start();
                    }
                }

                switcher.setText(queue.size() == 1 ? queue.peek() : queue.remove());
                handler.postDelayed(
                        this, animators.getTotalDuration() + 200);
            }
        }, 1);
    }

    public void setDisabled(boolean value) {
        setDisabled(value, -1);
    }
    public void setDisabled(boolean value, int timeout) {
            disabled = value;
        if(value) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    disabled = false;
                }
            }, timeout);
        }

    }

    static class Animators {
        public  AnimatorSet PLUSE = null;
        public  AnimatorSet BOUNCE = null;

        public View view = null;

        public Animators(View view) {
            this.view = view;

            PLUSE = getPulseAnimatorSet();
            BOUNCE = getBounceInAnimatorSet();
        }

        public AnimatorSet getPulseAnimatorSet() {
            AnimatorSet animatorSet = new AnimatorSet();
            //> https://github.com/daimajia/AndroidViewAnimations
            animatorSet.playTogether(ObjectAnimator.ofFloat(view, "scaleY", 1, 1.1f, 1),
                    ObjectAnimator.ofFloat(view, "scaleX", 1, 1.1f, 1));
            //<
            animatorSet.setDuration(400);
            return animatorSet;
        }

         public  AnimatorSet getBounceInAnimatorSet() {
             AnimatorSet animatorSet = new AnimatorSet();
             //> https://github.com/daimajia/AndroidViewAnimations
             animatorSet.playTogether(ObjectAnimator.ofFloat(view, "alpha", 0, 1, 1, 1),
                     ObjectAnimator.ofFloat(view, "scaleX", 0.3f, 1.05f, 0.9f, 1),
                     ObjectAnimator.ofFloat(view, "scaleY", 0.3f, 1.05f, 0.9f, 1));
             //<
             animatorSet.setDuration(1000);
             return animatorSet;
         }

        public int getTotalDuration() {
            return 1000 + 400;
        }
    }
}
