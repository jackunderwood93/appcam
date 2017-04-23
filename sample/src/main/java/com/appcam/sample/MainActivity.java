package com.appcam.sample;

import android.app.Activity;
import android.content.Intent;
import android.os.Debug;
import android.os.Handler;
import android.os.Bundle;
import android.view.MotionEvent;

import com.appcam.sdk.AppCam;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                Intent i = new Intent(MainActivity.this, SecondActivity.class);
//                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                startActivity(i);
//            }
//        },500000);

//        AppCam.startRecording("GunRB8iFFPNPpCOrIJ5JbjXrt0q2", AppCam.QUALITY_MEDIUM);

        Debug.startMethodTracing();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Debug.stopMethodTracing();
            }
        }, 5000);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(!AppCam.onActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        AppCam.dispatchTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }
}
