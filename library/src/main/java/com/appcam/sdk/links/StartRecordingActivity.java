package com.appcam.sdk.links;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.appcam.sdk.AppCam;
import com.appcam.sdk.R;

/**
 * Created by jackunderwood on 20/04/2017.
 */

public class StartRecordingActivity extends Activity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.record_request);

        String appName = getResources().getString(getApplicationInfo().labelRes);
        ((TextView)findViewById(R.id.instructions_title)).setText(appName + " would like to record your next session.");

        Intent intent = getIntent();
        Uri data = intent.getData();


        final String apiKey = data.getQueryParameter("key");
        final int quality;

        switch (data.getQueryParameter("quality")) {
            case "low":
                quality = AppCam.QUALITY_LOW;
                break;
            case "medium":
                quality = AppCam.QUALITY_MEDIUM;
                break;
            case "high":
                quality = AppCam.QUALITY_HIGH;
                break;

            default:
                quality = AppCam.QUALITY_MEDIUM;
                break;
        }



        findViewById(R.id.start_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppCam.startRecording(StartRecordingActivity.this, apiKey, quality);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(AppCam.onActivityResult(requestCode, resultCode, data)) {
            PackageManager pm = getPackageManager();
            Intent intent = pm.getLaunchIntentForPackage(getApplicationContext().getPackageName());
            startActivity(intent);


        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }

    }
}
