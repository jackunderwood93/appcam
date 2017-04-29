package com.appcam.sample;

import android.app.Application;

import com.appcam.sdk.AppCam;
import com.squareup.leakcanary.LeakCanary;


/**
 * Created by jackunderwood on 18/04/2017.
 */

public class SampleApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);
        // Normal app init code...
    }



}
