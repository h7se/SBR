package com.qou.h7se.sbr;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.util.Linkify;
import android.view.View;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

import java.io.File;

public class AlertActivity extends Activity {

    private Utils utils;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_alert);

        final Intent intent = getIntent();
        assert intent != null;
        assert intent.hasExtra("MSG");

        utils = new Utils(this);

        textView = (TextView) findViewById(R.id.textView);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (intent.getStringExtra("ACTION").contentEquals("CLOSE")) {
                    finish();
                } else if (intent.getStringExtra("ACTION").contentEquals("VIEW")) {
                    Intent i = new Intent();
                    i.setAction(Intent.ACTION_VIEW);
                    i.setData(Uri.fromFile(new File(intent.getStringExtra("PATH"))));
                    startActivity(i);
                    finish();
                }
            }
        });

        textView.setText(Html.fromHtml(intent.getStringExtra("MSG")));
        Linkify.addLinks(textView, Linkify.ALL);
        // utils.vibrationNotify3(AlertActivity.this);

        Animation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(5); //You can manage the time of the blink with this parameter
        anim.setStartOffset(20);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(3);
        textView.startAnimation(anim);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 3000);
    }
}
