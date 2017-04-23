package com.appcam.sample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;

import com.appcam.sdk.AppCam;

import org.jetbrains.annotations.Nullable;


/**
 * Created by jackunderwood on 18/04/2017.
 */

public class SecondActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(SecondActivity.this, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
            }
        }, 5000);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        AppCam.dispatchTouchEvent(ev);

        return super.dispatchTouchEvent(ev);
    }
}
