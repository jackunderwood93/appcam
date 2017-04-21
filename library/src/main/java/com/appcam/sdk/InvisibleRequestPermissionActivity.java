package com.appcam.sdk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by jackunderwood on 21/04/2017.
 */

public class InvisibleRequestPermissionActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        AppCam.onActivityResult(requestCode, resultCode, data);

        finish();
    }
}
