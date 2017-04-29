package com.appcam.sdk;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.appcam.sdk.tus.TusClient;
import com.appcam.sdk.tus.TusExecutor;
import com.appcam.sdk.tus.TusURLMemoryStore;
import com.appcam.sdk.tus.TusUpload;
import com.appcam.sdk.tus.TusUploader;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;

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

        Log.i(APP_CAM_LOG, "Found " + recordingFolder.listFiles().length + " videos to upload.");


        for (final File file : recordingFolder.listFiles()) {

                try {

                    // Create a new  instance
                    final TusClient client = new TusClient();

                    HashMap map = new HashMap();
                    map.put("Content-Type", "application/offset+octet-stream");
                    map.put("Upload-Metadata", getFileMetaData(file));

                    client.setHeaders(map);

                    client.setUploadCreationURL(new URL("http://159.203.140.238/" + file.getName()));

                    client.enableResuming(new TusURLMemoryStore());


                    final TusUpload upload = new TusUpload(file);


                    final TusExecutor executor = new TusExecutor() {
                        @Override
                        protected void makeAttempt() throws ProtocolException, IOException {

                            try {
                                TusUploader uploader = client.resumeOrCreateUpload(upload);

                                // Upload the file in chunks of 1KB sizes.
                                uploader.setChunkSize(1024 * 256);


                                // Upload the file as long as data is available. Once the
                                // file has been fully uploaded the method will return -1
                                do {
                                    // Calculate the progress using the total size of the uploading file and
                                    // the current offset.
                                    long totalBytes = upload.getSize();
                                    long bytesUploaded = uploader.getOffset();
                                    double progress = (double) bytesUploaded / totalBytes * 100;

                                   Log.i(APP_CAM_LOG, String.format("Upload at %02.2f%%.\n", progress));
                                } while (uploader.uploadChunk() > -1);

                                // Allow the HTTP connection to be closed and cleaned up
                                uploader.finish();

                                Log.i(APP_CAM_LOG, "Upload finished.");

                                file.delete();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    };

                    executor.makeAttempts();

                } catch (Exception e) {
                    Log.e(APP_CAM_LOG, "Upload failed.", e);

                }
            }



            if (recordingFolder.listFiles().length == 0) {
                JobScheduler jobScheduler = (JobScheduler) getApplicationContext().getSystemService(Context.JOB_SCHEDULER_SERVICE);
                jobScheduler.cancel(JOB_ID);
            }



    }


    private String getFileMetaData(File file) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        return preferences.getString("appcam_" + file.getName(), "");
    }
}
