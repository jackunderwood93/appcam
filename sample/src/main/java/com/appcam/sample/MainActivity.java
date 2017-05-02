package com.appcam.sample;

import android.app.Activity;
import android.content.Intent;
import android.os.Debug;
import android.os.Handler;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.appcam.sdk.AppCam;

import java.util.ArrayList;


public class MainActivity extends Activity {


    private FrameLayout recordingButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        findViewById(R.id.recording_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppCam.startRecording("078DUjQqYdQRr4ShFZNDakKQjfh1");
            }
        });

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        AppCam.dispatchTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
