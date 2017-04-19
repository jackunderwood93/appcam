package com.appcam.sample;

import android.app.Application;

import com.appcam.sdk.AppCam;


/**
 * Created by jackunderwood on 18/04/2017.
 */

public class SampleApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        AppCam.init(this, "sNBS64ht6ePHDhnKKiGQHh8uLZ52", AppCam.QUALITY_MEDIUM, true);

    }
}
