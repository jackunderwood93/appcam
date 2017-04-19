package com.appcam.sample;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;

import com.appcam.sdk.AppCam;
import com.appcam.sdk.AppCamProvider;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AppCamProvider.startRecording(this);

    }

    @Override
    protected void onResume() {
        super.onResume();

        AppCamProvider.attachActivity(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (AppCamProvider.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        AppCamProvider.dispatchTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }
}
