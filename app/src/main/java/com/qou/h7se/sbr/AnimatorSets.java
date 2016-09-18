package com.qou.h7se.sbr;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

/**
 * Created by k0de9x on 10/20/2015.
 */
public class AnimatorSets {

    public static AnimatorSet getBounceInDownAnimatorSet(View target, int duration) {
        AnimatorSet set = new AnimatorSet();
        set.playTogether(
                ObjectAnimator.ofFloat(target, "alpha", 0, 1, 1, 1),
                ObjectAnimator.ofFloat(target, "translationY", -200, -70, 80, -30, 40, -5, 10, -10, 0)
        );
        set.setDuration(duration);
        return set;
    }

    public static AnimatorSet getBounceInRightAnimatorSet(View target, int duration) {
        AnimatorSet set = new AnimatorSet();
        set.playTogether(
                ObjectAnimator.ofFloat(target,"translationX",target.getMeasuredWidth()+target.getWidth(),  -80, 40, -30, -20, 15, 0),
                ObjectAnimator.ofFloat(target,"alpha",0,1,1,1)
        );
        set.setDuration(duration);
        return set;
    }

    public static AnimatorSet getFadeOutAnimatorSet(View target, int duration) {
        AnimatorSet set = new AnimatorSet();
        set.playTogether(
                ObjectAnimator.ofFloat(target,"alpha", 0.7f, 0.7f, 0.3f, 1, 0, 0)
        );
        set.setDuration(duration);
        return set;
    }

    public static AnimatorSet getFadeInAnimatorSet(View target, int duration) {
        AnimatorSet set = new AnimatorSet();
        set.playTogether(
                ObjectAnimator.ofFloat(target,"alpha", 0.7f, 0.3f, 0.6f, 0, 1, 1)
        );
        set.setDuration(duration);
        return set;
    }

    public static AnimatorSet getZoomInAnimatorSet(View target, int duration) { /// test
        AnimatorSet set = new AnimatorSet();
        set.playTogether(
                ObjectAnimator.ofFloat(target,"alpha", 0.7f, 0.3f, 0.6f, 0, 1, 1),
                ObjectAnimator.ofFloat(target,"scaleX", 0.5f, 0.3f, 1.8f , 0.5f, 1),
                ObjectAnimator.ofFloat(target,"scaleY", 0.5f, 0.3f, 1.8f , 0.5f, 1),
                ObjectAnimator.ofFloat(target,"alpha", 0.7f, 0.3f, 0.6f, 0, 1, 1)
        );
        set.setDuration(duration);
        return set;
    }

    public static AnimatorSet getProgressAnimatorSet(View target,int from, int to, int duration) {
        AnimatorSet set = new AnimatorSet();
        set.playTogether(
                ObjectAnimator.ofInt(target, "progress", from, to)
        );
        set.setInterpolator(new DecelerateInterpolator());
        set.setDuration(duration);
        return set;
    }

    public static AnimatorSet attachEndCallback(AnimatorSet set, final GenericCallback2 callback) {
        set.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                callback.event();
            }

            @Override
            public void onAnimationCancel(Animator animator) {
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }
        });
        return set;
    }
}
