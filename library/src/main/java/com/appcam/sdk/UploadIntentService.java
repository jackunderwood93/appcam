package com.appcam.sdk;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.os.PersistableBundle;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static com.appcam.sdk.AppCam.JOB_ID;

/**
 * Created by jackunderwood on 14/04/2017.
 */

public class UploadIntentService extends JobService {

    public UploadIntentService() {

    }

    @Override
    public boolean onStartJob(final JobParameters params) {

        Log.e("LOG", "Start Job");

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
                uploadFile(params, fileLocation);
            }
        }).start();


        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }


    public  void uploadFile( JobParameters parameters, String fileName) {


        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1024 * 1024;
        File sourceFile = new File(fileName);

        if (!sourceFile.isFile()) {
            return;

        } else {
            int serverResponseCode = 0;

            try {

                // open a URL connection to the Servlet
                FileInputStream fileInputStream = new FileInputStream(sourceFile);
                URL url = new URL("http://159.203.140.238/uploads/upload.php");

                // Open a HTTP  connection to  the URL
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("uploadedfile", fileName);

                dos = new DataOutputStream(conn.getOutputStream());

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name='uploadedfile';filename='" + fileName + "'" + lineEnd);

                dos.writeBytes(lineEnd);

                // create a buffer of  maximum size
                bytesAvailable = fileInputStream.available();

                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {

                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                }

                // send multipart form data necesssary after file data...
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // Responses from the server (code and message)
                serverResponseCode = conn.getResponseCode();
                String serverResponseMessage = conn.getResponseMessage();


                Log.e("LOG", "HTTP Response is : "
                        + serverResponseMessage + ": " + serverResponseCode);

                if (serverResponseCode == 200) {

                    new File(fileName).delete();
                    Log.e("LOG", "Successfully uploaded!");

                    JobScheduler jobScheduler = (JobScheduler) getApplicationContext().getSystemService(Context.JOB_SCHEDULER_SERVICE);
                    jobScheduler.cancel(JOB_ID);

                    jobFinished(parameters, false);

                }

                //close the streams //
                fileInputStream.close();
                dos.flush();
                dos.close();

            } catch (MalformedURLException ex) {


                Log.e("LOG", "error: " + ex.getMessage(), ex);
            } catch (Exception e) {

                Log.e("LOG", "Exception : "
                        + e.getMessage(), e);
            }

        }

    }


}
