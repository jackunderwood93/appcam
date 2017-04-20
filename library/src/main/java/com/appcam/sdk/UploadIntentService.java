package com.appcam.sdk;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.os.PersistableBundle;
import android.util.Log;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static com.appcam.sdk.AppCamInternals.APP_CAM_LOG;
import static com.appcam.sdk.AppCamInternals.JOB_ID;


/**
 * Created by jackunderwood on 14/04/2017.
 */

public class UploadIntentService extends JobService {

    public UploadIntentService() {

    }

    @Override
    public boolean onStartJob(final JobParameters params) {


        Log.i(APP_CAM_LOG, "Upload job started");

        // Get upload target from bundle
        final String fileLocation = params.getExtras().getString("file_location");

        PersistableBundle bundle = new PersistableBundle();
        bundle.putString("file_location", fileLocation);

        // Reschedule job to retry no less than every 5 minutes if this one fails.
        JobInfo.Builder jobBuilder = new JobInfo.Builder(JOB_ID, new ComponentName(getApplicationContext(), UploadIntentService.class));
        jobBuilder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED);
        jobBuilder.setExtras(bundle);
        jobBuilder.setMinimumLatency(300000);

        JobScheduler jobScheduler = (JobScheduler) getApplicationContext().getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(jobBuilder.build());

        // Start upload
        new Thread(new Runnable() {
            @Override
            public void run() {
                uploadRecordings(getApplicationContext());
            }
        }).start();


        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }


    public void uploadRecordings(Context context) {

        File recordingFolder = new File(context.getFilesDir() + "/recordings/");

        Log.e(APP_CAM_LOG, "Found " + recordingFolder.listFiles().length + " videos to upload.");
        for (File sourceFile : recordingFolder.listFiles()) {

                try {

                    /* set the variable needed by http post */
                    String actionUrl = "http://159.203.140.238:3000/upload";
                    final String end = "\r\n";
                    final String twoHyphens = "--";
                    final String boundary = "*****++++++************++++++++++++";

                    URL url = new URL(actionUrl);
                    HttpURLConnection conn = (HttpURLConnection)url.openConnection();

                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    conn.setUseCaches(false);
                    conn.setRequestMethod("POST");

                    /* setRequestProperty */
                    conn.setRequestProperty("Connection", "Keep-Alive");
                    conn.setRequestProperty("Charset", "UTF-8");
                    conn.setRequestProperty("Content-Type", "multipart/form-data;boundary="+ boundary);

                    DataOutputStream ds = new DataOutputStream(conn.getOutputStream());
                    ds.writeBytes(twoHyphens + boundary + end);
                    ds.writeBytes("Content-Disposition: form-data; name=\"from\""+end+end+"auto"+end);
                    ds.writeBytes(twoHyphens + boundary + end);
                    ds.writeBytes("Content-Disposition: form-data; name=\"to\""+end+end+"ja"+end);
                    ds.writeBytes(twoHyphens + boundary + end);
                    ds.writeBytes("Content-Disposition: form-data; name=\"video\";filename=\"" + sourceFile.getName() +"\"" + end);
                    ds.writeBytes(end);

                    FileInputStream fStream = new FileInputStream(sourceFile);
                    int bufferSize = 1024;
                    byte[] buffer = new byte[bufferSize];
                    int length = -1;

                    while((length = fStream.read(buffer)) != -1) {
                        ds.write(buffer, 0, length);
                    }
                    ds.writeBytes(end);
                    ds.writeBytes(twoHyphens + boundary + twoHyphens + end);
                    /* close streams */
                    fStream.close();
                    ds.flush();
                    ds.close();

                    if(conn.getResponseCode() == 200){
                        sourceFile.delete();

                        Log.i(APP_CAM_LOG, "File uploaded");
                    }

                } catch (Exception e) {
                    Log.e(APP_CAM_LOG, "There was an error uploading:");

                    e.printStackTrace();
                }
            }



        if(recordingFolder.listFiles().length == 0) {
            JobScheduler jobScheduler = (JobScheduler) getApplicationContext().getSystemService(Context.JOB_SCHEDULER_SERVICE);
            jobScheduler.cancel(JOB_ID);
        }


    }


}
