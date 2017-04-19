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

public class AppCamProvider {

    private static AppCam appCam;

    public static AppCam getInstance() {
        if(appCam == null) {
            appCam = new AppCam();
        }

        return appCam;
    }

    public static void init(Application application, String key, int videoQuality, boolean instantUpload) {
        getInstance().init(application, key, videoQuality, instantUpload);
    }

    public static void startRecording(Activity activity) {
        getInstance().startRecording(activity);
    }

    public static void stopRecording() {
        getInstance().stop();
    }

    public static void attachActivity(Activity activity) {
        getInstance().attachActivity(activity);
    }

    public static void dispatchTouchEvent(MotionEvent event) {
        getInstance().dispatchTouchEvent(event);
    }

    public static boolean handleActivityResult(int requestCode, int resultCode, Intent intent) {
        return getInstance().handleActivityResult(requestCode, resultCode, intent);
    }
}
