package com.qou.h7se.sbr;

import android.media.AudioManager;
import android.media.ToneGenerator;

/**
 * Created by k0de9x on 10/17/2015.
 */
public class TunePlayer {
    public static TunePlayer instance = null;

    public TunePlayer() {
        playing = false;
        handler = new android.os.Handler();
        toneGenerator = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
    }

    private boolean playing;
    private android.os.Handler handler = null;
    private ToneGenerator toneGenerator = null;

    public void play() {
        if (!playing) {
            playing = true;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    toneGenerator.startTone(
                            ToneGenerator.TONE_PROP_BEEP2);
                    playing = false;
                }
            }, 1000);
        }
    }
}
