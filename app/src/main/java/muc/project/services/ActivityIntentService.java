package muc.project.services;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import muc.project.ApplicationContext;
import muc.project.helpers.Constants;

public class ActivityIntentService extends IntentService {

    private final static String TAG = "ActivityIntentService";

    public ActivityIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            // Get most probable activity
            Log.i(TAG, "Got activity intent");
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            DetectedActivity mostProbableActivity = result.getMostProbableActivity();
            int activityType = mostProbableActivity.getType();
            Intent broadcastIntent = new Intent(Constants.ACTIVITY_BROADCAST);
            broadcastIntent.putExtra(Constants.DETECTED_ACTIVITY, activityType);
            LocalBroadcastManager.getInstance(ApplicationContext.getInstance()).sendBroadcast(broadcastIntent);
        }
    }
}