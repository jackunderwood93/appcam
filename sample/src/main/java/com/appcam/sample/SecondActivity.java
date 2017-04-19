package com.appcam.sample;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MotionEvent;

import com.appcam.sdk.AppCamProvider;

/**
 * Created by jackunderwood on 18/04/2017.
 */

public class SecondActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppCamProvider.attachActivity(this);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        AppCamProvider.dispatchTouchEvent(ev);

        return super.dispatchTouchEvent(ev);
    }
}
