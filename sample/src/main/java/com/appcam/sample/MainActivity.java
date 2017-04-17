package com.appcam.sample;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;

import com.appcam.sdk.AppCam;

public class MainActivity extends AppCompatActivity {

    private AppCam appCam;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        appCam = new AppCam();
        appCam.init(this, "sNBS64ht6ePHDhnKKiGQHh8uLZ52", AppCam.QUALITY_LOW);
    }

    @Override
    protected void onStop() {
        super.onStop();

        appCam.stop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (!appCam.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        appCam.dispatchTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }
}
