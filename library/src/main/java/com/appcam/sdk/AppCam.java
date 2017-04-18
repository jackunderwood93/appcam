package com.appcam.sdk;

import android.app.Activity;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.hardware.display.DisplayManager;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Environment;
import android.os.PersistableBundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static android.R.attr.width;

/**
 * Created by jackunderwood on 14/04/2017.
 */

public class AppCam {

    private  MediaRecorder mediaRecorder;
    private  Activity activity;
    private  MediaProjectionManager mediaProjectionManager;
    private  MediaProjection mediaProjection;
    private  String apiKey;
    private  String fileLocation;
    private  int quality;

    private boolean hasStopped = false;

    public static final int JOB_ID = 1000119;

    public static final int QUALITY_LOW = 1;
    public static final int QUALITY_MEDIUM = 2;
    public static final int QUALITY_HIGH = 5;

    public static AppCam appCam;

    private ImageView touchView;
    private int touchSize;
    private Interpolator interpolator;

    private int videoWidth;
    private int videoHeight;
    private boolean instantUpload;


    public void init(Activity act, String key, int videoQuality, boolean instantUpload) {
        activity = act;
        mediaRecorder = new MediaRecorder();
        apiKey = key;
        quality = videoQuality;
        this.instantUpload = instantUpload;

        buildFileName();
        calculateSizes();
        startRecording();

    }

    public void init(Activity act, String key, boolean instantUpload) {
        init(act, key, QUALITY_MEDIUM, instantUpload);
    }


    private void calculateSizes() {
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);

        int deviceWidth = metrics.widthPixels;
        int deviceHeight = metrics.heightPixels;

        double ratio;

        double targetSize;

        if(quality == QUALITY_LOW || quality == QUALITY_MEDIUM) {
            targetSize = 720;
        } else {
            targetSize = 1080;
        }

        if(deviceWidth < deviceHeight) {
            ratio = deviceWidth / Math.min(targetSize, deviceWidth);
        } else {
            ratio = deviceHeight / Math.min(targetSize, deviceHeight);
        }

        videoWidth = (int) (deviceWidth / ratio);
        videoHeight = (int) (deviceHeight / ratio);
    }

    private void buildFileName() {

        String versionName = "Unknown";

        try {
            versionName = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0).versionName;
        } catch (Exception e) {

        }

        fileLocation = activity.getFilesDir() +  "/recordings/" + apiKey + "-" + android.os.Build.MODEL + "-" + versionName + "-" + System.currentTimeMillis() + ".mp4";
    }

    private void createTouchView(Activity activity) {

        Resources resources = activity.getResources();
        touchSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, resources.getDisplayMetrics());

        ViewGroup viewGroup = (ViewGroup) activity.findViewById(android.R.id.content).getRootView();
        touchView = new ImageView(activity);
        touchView.setLayoutParams(new FrameLayout.LayoutParams(touchSize, touchSize));
        touchView.setImageResource(R.drawable.oval);
        touchView.setAlpha(0f);
        viewGroup.addView(touchView);

        interpolator = new AccelerateInterpolator();

    }

    private void prepareRecording() {

        final String directory = activity.getFilesDir() + "/recordings/";

        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            return;
        }

        final File folder = new File(directory);

        if (!folder.exists()) {
            folder.mkdir();
        }



        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mediaRecorder.setVideoEncodingBitRate(1024 * 1024 * quality);
        mediaRecorder.setVideoFrameRate(30);
        mediaRecorder.setVideoSize(videoWidth, videoHeight);
        mediaRecorder.setOutputFile(fileLocation);

        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mediaRecorder.start();

    }

    private void setupMediaProjection() {

        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);

        prepareRecording();

        mediaProjection.createVirtualDisplay("ScreenCapture",
                videoWidth, videoHeight, metrics.densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mediaRecorder.getSurface(), null, null);

    }

    private void startRecording() {
        mediaProjectionManager = (MediaProjectionManager) activity.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        activity.startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), 1);
    }


    public boolean handleActivityResult(int requestCode, int resultCode, Intent data) {
        mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data);

        if(mediaProjection != null) {
            setupMediaProjection();
            createTouchView(activity);
            return true;
        } else {
            return false;
        }
    }

    public void stop() {

        if(hasStopped) {
            return;
        }

        try {
            if (mediaRecorder != null) {
                mediaRecorder.stop();
            }
        }catch (Exception e) {

        }

        try {
            if (mediaProjection != null) {
                mediaProjection.stop();
            }
        } catch (Exception e) {

        }


        PersistableBundle bundle = new PersistableBundle();
        bundle.putString("file_location", fileLocation);

        JobInfo.Builder jobBuilder = new JobInfo.Builder(JOB_ID, new ComponentName(activity, UploadIntentService.class));
        jobBuilder.setExtras(bundle);
        jobBuilder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED);
        jobBuilder.setRequiresCharging(true);

        if(instantUpload) {
            jobBuilder.setOverrideDeadline(1000);
        }


        JobScheduler jobScheduler = (JobScheduler) activity.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(jobBuilder.build());


        activity = null;
        mediaProjection = null;
        mediaProjectionManager = null;
        mediaRecorder = null;

        hasStopped = true;
    }

    public void dispatchTouchEvent(MotionEvent ev) {

        if(touchView == null) {
            return;
        }

        touchView.setTranslationX(ev.getX() - touchSize/2);
        touchView.setTranslationY(ev.getY() - touchSize/2);


        touchView.clearAnimation();
        touchView.setAlpha(1f);
        touchView.animate().alpha(0f).setDuration(200).setStartDelay(0).setInterpolator(interpolator);

    }

}
