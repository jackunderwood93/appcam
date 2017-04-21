package com.appcam.sdk;

import android.app.Activity;
import android.app.Application;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.view.MotionEvent;

/**
 * Created by jackunderwood on 18/04/2017.
 */

public class AppCam {

    public static final int QUALITY_LOW = 1;
    public static final int QUALITY_MEDIUM = 2;
    public static final int QUALITY_HIGH = 4;

    private static AppCamInternals appCam;

    private static AppCamInternals getInstance() {
        if(appCam == null) {
            appCam = new AppCamInternals();
        }

        return appCam;
    }

    public static void startRecording(String apiKey, int quality) {
        getInstance().startRecording(apiKey, quality);
    }

    public static void stopRecording() {
        getInstance().stop();
    }

    public static void dispatchTouchEvent(MotionEvent event) {
        getInstance().dispatchTouchEvent(event);
    }

    public static boolean onActivityResult(int requestCode, int resultCode, Intent intent) {
        return getInstance().onActivityResult(requestCode, resultCode, intent);
    }

    static void setApplication(Application application) {
        getInstance().setApplication(application);
    }
}
