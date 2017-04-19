package com.appcam.sdk;

import android.app.Activity;
import android.app.Application;
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
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.appcam.sdk.appstate.AppStateListener;
import com.appcam.sdk.appstate.AppStateMonitor;

import java.io.File;
import java.io.IOException;

import static com.appcam.sdk.AppCam.QUALITY_LOW;
import static com.appcam.sdk.AppCam.QUALITY_MEDIUM;


/**
 * Created by jackunderwood on 14/04/2017.
 */

 class AppCamInternals {

    private  MediaRecorder mediaRecorder;
    private  Activity activity;
    private  MediaProjectionManager mediaProjectionManager;
    private  MediaProjection mediaProjection;
    private  String apiKey;
    private  String fileLocation;
    private  int quality;

    private boolean hasStopped = false;

    public static final int JOB_ID = 1000119;
    public static String APP_CAM_LOG = "AppCam";

    private ImageView touchView;
    private int touchSize;
    private Interpolator interpolator;

    private int videoWidth;
    private int videoHeight;
    private boolean instantUpload;

    private boolean hasInit = false;

    private boolean isRecording = false;

    private Application application;


    void init(Application application, String key, int videoQuality, boolean instantUpload) {
        mediaRecorder = new MediaRecorder();
        apiKey = key;
        quality = videoQuality;
        this.instantUpload = instantUpload;
        this.application = application;

        calculateSizes();
        setupAppState();

        hasInit = true;

    }

    boolean hasInit() {
        return hasInit;
    }

     void attachActivity(Activity activity) {

         if(application == null) {
             return;
         }

        if(touchView != null) {
            ((ViewGroup)touchView.getParent()).removeView(touchView);
        }

        createTouchView(activity);
    }


    private void setupAppState() {

        if(application == null) {
            return;
        }

        AppStateMonitor appStateMonitor = AppStateMonitor.create(application);
        appStateMonitor.addListener(new AppStateListener() {
            @Override
            public void onAppDidEnterForeground() {

            }

            @Override
            public void onAppDidEnterBackground() {
                stop();
            }
        });

        appStateMonitor.start();

    }

    private void calculateSizes() {

        if(application == null) {
            return;
        }

        DisplayMetrics metrics = application.getApplicationContext().getResources().getDisplayMetrics();

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

        if(application == null) {
            return;
        }

        String versionName = "Unknown";

        try {
            versionName = application.getApplicationContext().getPackageManager().getPackageInfo(application.getPackageName(), 0).versionName;
        } catch (Exception e) {

        }

        fileLocation = application.getFilesDir() +  "/recordings/" + apiKey + "-" + android.os.Build.MODEL + "-" + versionName + "-" + System.currentTimeMillis() + ".mp4";
    }

    private void createTouchView(Activity activity) {

        if(application == null) {
            return;
        }

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

        if(application == null) {
            return;
        }


        final String directory = application.getFilesDir() + "/recordings/";

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

        DisplayMetrics metrics = application.getResources().getDisplayMetrics();

        prepareRecording();

        mediaProjection.createVirtualDisplay("ScreenCapture",
                videoWidth, videoHeight, metrics.densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mediaRecorder.getSurface(), null, null);

    }

     void startRecording(Activity activity) {

         if(application == null) {

             Log.e(APP_CAM_LOG, "Tried to start recording before AppCamProvider.init() is called.");

             return;
         }

         if(isRecording) {
             Log.e(APP_CAM_LOG, "AppCam is already recording.");
         }

         buildFileName();
         attachActivity(activity);

        mediaProjectionManager = (MediaProjectionManager) activity.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        activity.startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), 1);

    }


     boolean handleActivityResult(int requestCode, int resultCode, Intent data) {

         if(application == null) {
             return false;
         }

        mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data);

        if(mediaProjection != null) {
            setupMediaProjection();
            isRecording = true;
            return true;
        } else {
            return false;
        }
    }

     void stop() {

         if(!isRecording) {
             return;
         }

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

        JobInfo.Builder jobBuilder = new JobInfo.Builder(JOB_ID, new ComponentName(application, UploadIntentService.class));
        jobBuilder.setExtras(bundle);
        jobBuilder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED);
        jobBuilder.setRequiresCharging(true);

        if(instantUpload) {
            jobBuilder.setOverrideDeadline(1000);
        }


        JobScheduler jobScheduler = (JobScheduler) application.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(jobBuilder.build());

         Log.i(APP_CAM_LOG, "File saved, upload scheduled");

     }

     void dispatchTouchEvent(MotionEvent ev) {

         if(application == null) {
             return;
         }

        if(touchView == null) {
            return;
        }

        if(!isRecording) {
            return;
        }



        touchView.setTranslationX(ev.getX() - touchSize/2);
        touchView.setTranslationY(ev.getY() - touchSize/2);


        touchView.clearAnimation();
        touchView.setAlpha(1f);
        touchView.animate().alpha(0f).setDuration(200).setStartDelay(0).setInterpolator(interpolator);

    }

}
