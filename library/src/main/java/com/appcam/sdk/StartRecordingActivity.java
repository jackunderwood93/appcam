package com.appcam.sdk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.appcam.sdk.AppCam;
import com.appcam.sdk.BuildConfig;
import com.appcam.sdk.R;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by jackunderwood on 20/04/2017.
 */

public class StartRecordingActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.appcam_record_request);

        String appName;

        PackageManager packageManagers= getApplicationContext().getPackageManager();
        try {
             appName = (String) packageManagers.getApplicationLabel(packageManagers.getApplicationInfo(getApplicationContext().getPackageName(), PackageManager.GET_META_DATA));
        } catch (Exception e) {
            appName = "A developer";
        }

        ((TextView)findViewById(R.id.instructions_title)).setText(appName + " would like to record your next session.");


        long currentMillis = System.currentTimeMillis();
        final File file = new File(getApplicationContext().getFilesDir() + "/recordings/" + currentMillis + ".mp4");
        saveFileMetaData(file, currentMillis);


        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            findViewById(R.id.start_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    AppCam.prepareRecording(file);

                        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
                        startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), 1);
                }
            });
        } else {
            ((TextView)findViewById(R.id.instructions_description)).setText("Unfortunately, this requires at least Android 5.0.");
            findViewById(R.id.start_button).setVisibility(View.GONE);
        }


        findViewById(R.id.powered_by_appcam).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://appcam.io")));
            }
        });
    }

    private String getBase64String(String string) {

        if(string != null) {
            return Base64.encodeToString(string.getBytes(), Base64.DEFAULT);
        } else return "";
    }

    private void saveFileMetaData(File file, long currentMillis) {

        HashMap<String, String> metaMap = new HashMap();
        String metaData = "";


        // Link Params
        Intent intent = getIntent();
        Uri data = intent.getData();

        Set<String> params = data.getQueryParameterNames();

        for(String param : params) {
            metaMap.put(param, data.getQueryParameter(param));
        }

        String version_name = "Unknown";
        String version_code = "Unknown";
        String package_name = "Unknown";


        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);

             version_name = pInfo.versionName;
             version_code = pInfo.versionCode + "";
            package_name = pInfo.packageName;


        } catch (Exception e) {

        }

        // App Params
            metaMap.put("app_version_name", version_name);
            metaMap.put("app_version_code",version_code);
            metaMap.put("app_package_name", package_name);
            metaMap.put("date", currentMillis + "");
            metaMap.put("device_name", Build.MODEL);
            metaMap.put("device_sdk", Build.VERSION.SDK_INT + "");
            metaMap.put("appcam_version", getResources().getInteger(R.integer.appcam_version) + "");
            metaMap.put("filename", currentMillis +"");




        for(String key: metaMap.keySet()) {

            String value = metaMap.get(key);

            if(!value.equals("")) {
                metaData += key + " " + getBase64String(metaMap.get(key)) + ",";
            }

        }

        metaData = metaData.substring(0, metaData.length()-1);
        metaData = metaData.replace("\n", "");

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        preferences.edit().putString("appcam_" + file.getName(), metaData).apply();

    }

    private String getApiKey() {
        return getIntent().getData().getQueryParameter("key");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(AppCam.onActivityResult(requestCode, resultCode, data)) {
            PackageManager pm = getPackageManager();
            Intent intent = pm.getLaunchIntentForPackage(getApplicationContext().getPackageName());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            finish();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}
