package com.appcam.sample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;

import com.appcam.sdk.AppCam;

import java.util.ArrayList;


/**
 * Created by jackunderwood on 18/04/2017.
 */

public class SecondActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        AppCam.dispatchTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }
}
