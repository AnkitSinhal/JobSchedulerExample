package com.androidjavapoint.jobschedulerexample;

import static com.androidjavapoint.jobschedulerexample.MainActivity.MESSENGER_KEY;
import static com.androidjavapoint.jobschedulerexample.MainActivity.MSG_JOB_PROGRESS;
import static com.androidjavapoint.jobschedulerexample.MainActivity.MSG_JOB_START;
import static com.androidjavapoint.jobschedulerexample.MainActivity.MSG_JOB_STOP;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

public class JobScheduleService extends JobService {
    private static final String TAG = JobScheduleService.class.getSimpleName();
    private Messenger mActivityMessenger;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        mActivityMessenger = intent.getParcelableExtra(MESSENGER_KEY);
        return START_NOT_STICKY;
    }

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Log.d(TAG, "onStartJob");

        sendMessage(MSG_JOB_START, getString(R.string.job_started) + "\n Job Id : " +
                                   jobParameters.getJobId());

        new JobAsyncTask(this).execute(jobParameters);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        Log.d(TAG, "onStopJob");
        Toast.makeText(this, getString(R.string.job_stop), Toast.LENGTH_SHORT).show();

        sendMessage(MSG_JOB_STOP, getString(R.string.job_stop));
        return false;
    }

    private class JobAsyncTask extends AsyncTask<JobParameters, Void, JobParameters> {
        private final JobService jobService;

        JobAsyncTask(JobService jobService) {
            Log.d(TAG, "JobAsyncTask");
            this.jobService = jobService;
        }

        @Override
        protected JobParameters doInBackground(JobParameters... jobParameters) {
            SystemClock.sleep(5000);
            return jobParameters[0];
        }

        @Override
        protected void onPostExecute(JobParameters jobParameters) {
            Log.d(TAG, "JobAsyncTask completed");
            sendMessage(MSG_JOB_PROGRESS, "Background process completed");
            // Finish the job service if required
//            jobService.jobFinished(jobParameters, false);
        }
    }


    private void sendMessage(int messageID, @Nullable Object params) {
        if (mActivityMessenger == null) {
            Log.d(TAG, "Service is bound, not started. There's no callback to send a message to.");
            return;
        }
        Message m = Message.obtain();
        m.what = messageID;
        m.obj = params;
        try {
            mActivityMessenger.send(m);
        } catch (RemoteException e) {
            Log.e(TAG, "Error passing service object back to activity.");
        }
    }
}
