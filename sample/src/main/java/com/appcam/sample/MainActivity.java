package com.appcam.sample;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;

import com.appcam.sdk.AppCam;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AppCam.startRecording(this);

    }

    @Override
    protected void onResume() {
        super.onResume();

        AppCam.attachActivity(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (AppCam.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        AppCam.dispatchTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }
}
