package com.appcam.sample;

import android.app.Activity;
import android.content.Intent;
import android.os.Debug;
import android.os.Handler;
import android.os.Bundle;
import android.view.MotionEvent;

import com.appcam.sdk.AppCam;

import java.util.ArrayList;


public class MainActivity extends Activity {

    ArrayList<String> bla = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                AppCam.startRecording("078DUjQqYdQRr4ShFZNDakKQjfh1");
            }
        }, 10000);


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
