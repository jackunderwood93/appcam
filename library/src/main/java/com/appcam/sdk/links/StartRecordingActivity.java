package com.appcam.sdk.links;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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

        String appName;

        PackageManager packageManagers= getApplicationContext().getPackageManager();
        try {
             appName = (String) packageManagers.getApplicationLabel(packageManagers.getApplicationInfo(getApplicationContext().getPackageName(), PackageManager.GET_META_DATA));
        } catch (Exception e) {
            appName = "A developer";
        }


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


        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            findViewById(R.id.start_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    PackageManager pm = getPackageManager();
                    Intent intent = pm.getLaunchIntentForPackage(getApplicationContext().getPackageName());
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            AppCam.startRecording(apiKey, quality);
                        }
                    }, 500);

                    finish();
                }
            });
        } else {
            ((TextView)findViewById(R.id.instructions_description)).setText("Unfortunately, this requires at least Android 5.0.");
            findViewById(R.id.start_button).setVisibility(View.GONE);
        }
    }

}
