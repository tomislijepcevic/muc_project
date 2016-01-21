package muc.project.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.DetectedActivity;

import java.util.Date;

import muc.project.ApplicationContext;
import muc.project.DBHelper;
import muc.project.R;
import muc.project.activities.DetailsActivity;
import muc.project.helpers.Constants;
import muc.project.model.Client;
import muc.project.model.ClientDao;
import muc.project.model.DaoSession;
import muc.project.model.History;


public class ActivityService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    protected static final String TAG = "ActivityService";
    private GoogleApiClient mGoogleApiClient;
    private DBHelper _dbHelper;
    private SubscribedClientDetectedBroadcastReceiver _clientDetectedBroadcastReceiver;
    private UnsubscribedClientDetectedBroadcastReceiver _strangerDetectedBroadcastReceiver;
    private ScanningEndedBroadcastReceiver _scanningEndedBroadcastReceiver;
    private ScanningAlarmReceiver _scanningAlarmReceiver;
    private ActivityIntentReceiver _activityIntentReceiver;
    private ClientDao clientDao;
    private static boolean wasActive = true;

    public ActivityService(){
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        buildGoogleApiClient();
        mGoogleApiClient.connect();

        _scanningAlarmReceiver = new ScanningAlarmReceiver();
        _activityIntentReceiver = new ActivityIntentReceiver();
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        lbm.registerReceiver(_scanningAlarmReceiver, new IntentFilter(Constants.ALARM_BROADCAST));
        lbm.registerReceiver(_activityIntentReceiver, new IntentFilter(Constants.ACTIVITY_BROADCAST));

        _dbHelper = new DBHelper(ApplicationContext.getInstance());
        DaoSession session = _dbHelper.getSession(true);
        clientDao = session.getClientDao();
        Log.i(TAG, "Actvity recognizer launched");

    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(ActivityRecognition.API)
                .build();
    }


    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "GoogleAPI connected");
        requestActivityUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "GoogleAPI connection failed. Connection result: " + connectionResult);
    }


    public void requestActivityUpdates() {
        if (!mGoogleApiClient.isConnected()) {
            Log.i(TAG, "GoogleAPI not connected");
            return;
        }
        else {
            ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(
                    mGoogleApiClient,
                    Constants.ACTIVITY_RECOGNITION_INTERVAL,
                    getActivityDetectionPendingIntent()
            );
            Log.i(TAG, "Acitivity updates requested");
        }

    }

    public void removeActivityUpdates() {
        if (!mGoogleApiClient.isConnected()) {
            Log.i(TAG, "GoogleAPI not connected");
            return;
        }
        else {
            ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(
                    mGoogleApiClient,
                    getActivityDetectionPendingIntent()
            );
            Log.i(TAG, "Activity updates removed");
        }
    }

    private PendingIntent getActivityDetectionPendingIntent() {
        Intent intent = new Intent(this, ActivityIntentService.class);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }


    class SubscribedClientDetectedBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Client client = clientDao.load(intent.getLongExtra("key", 0L));
            Intent detailIntent = new Intent(getApplicationContext(), DetailsActivity.class);
            detailIntent.putExtra("id", client.getId());
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                    detailIntent, 0);

            String title = "Friend nearby";
            String name = client.getName();
            String manufacturer = client.getManufacturer();

            if(name == null)
                name = "Unknown";
            if(manufacturer == null)
                manufacturer = "Unknown";

            String message = "Name: "+name+" Device: "+manufacturer;
            showNotification(title, message, pendingIntent);
        }
    }

    class UnsubscribedClientDetectedBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent != null) {
                Client client = clientDao.load(intent.getLongExtra("key", 0L));
                Intent detailIntent = new Intent(getApplicationContext(), DetailsActivity.class);
                detailIntent.putExtra("id", client.getId());
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                        detailIntent, 0);

                String title = "Possible friend nearby";
                String name = client.getName();
                String manufacturer = client.getManufacturer();
                int counter = client.getCounter();

                if(name == null)
                    name = "Unknown";
                if(manufacturer == null)
                    manufacturer = "Unknown";

                String message = "Name: "+name+"Device: "+manufacturer+" Counter: "+counter;
                showNotification(title, message, pendingIntent);
            }
        }
    }

    class ScanningEndedBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getApplicationContext());
            lbm.unregisterReceiver(_clientDetectedBroadcastReceiver);
//            lbm.unregisterReceiver(_strangerDetectedBroadcastReceiver);
            lbm.unregisterReceiver(_scanningEndedBroadcastReceiver);
        }
    }

    private void showNotification(String title, String message, PendingIntent pendingIntent) {
        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(pendingIntent)
                .setCategory(Notification.CATEGORY_SOCIAL)
                .setPriority(1)
                .setAutoCancel(true)
                .addAction(R.drawable.ic_info_outline_black_24dp, "Details", pendingIntent)
                .build();

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(10, notification);

        Log.i(TAG, "Notification showed");
    }

    class ScanningAlarmReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            requestActivityUpdates();
        }
    }

    class ActivityIntentReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Got activity intent");
            if(intent != null) {

                int activityType = intent.getIntExtra(Constants.DETECTED_ACTIVITY, -1);
                Log.i(TAG, "Activity: "+getActivityString(activityType));

                if(activityType != DetectedActivity.STILL) {
                    wasActive = true;
                    Log.i(TAG, "was active");
                }

                // Check if activity is STILL
                if (activityType == DetectedActivity.STILL && wasActive ) {
                    wasActive = false;

                    Log.i(TAG, "Device is STILL");

                    LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(ApplicationContext.getInstance());

                    // Register broadcast receiver and show notification on received broadcast
                    _clientDetectedBroadcastReceiver = new SubscribedClientDetectedBroadcastReceiver();
                    lbm.registerReceiver(_clientDetectedBroadcastReceiver,
                            new IntentFilter(Constants.SUBSCRIBED_CLIENT_DETECTED_BROADCAST_RESULT));

                    // Register broadcast receiver and show notification on received broadcast
//                    _strangerDetectedBroadcastReceiver = new UnsubscribedClientDetectedBroadcastReceiver();
//                    lbm.registerReceiver(_strangerDetectedBroadcastReceiver,
//                            new IntentFilter(Constants.UNSUBSCRIBED_CLIENT_DETECTED_BROADCAST_RESULT));

                    // Register broadcast receiver and unregister both broadcast receivers on receiver broadcast
                    _scanningEndedBroadcastReceiver = new ScanningEndedBroadcastReceiver();
                    lbm.registerReceiver(_scanningEndedBroadcastReceiver,
                            new IntentFilter(Constants.WIFI_SENSING_ENDED_BROADCAST_RESULT));

                    // Start service WifiScaningIS
                    Intent wifiSensingServiceIntent = new Intent(getApplicationContext(), WifiSensingIS.class);
                    startService(wifiSensingServiceIntent);

                    // set alarm after 15min
                    SensingAlarmReceiver.startAlarm(ApplicationContext.getInstance());
                    removeActivityUpdates();
                }

            }
        }
    }

    public static String getActivityString(int detectedActivityType) {
        switch(detectedActivityType) {
            case DetectedActivity.IN_VEHICLE:
                return "In vehicle";
            case DetectedActivity.ON_BICYCLE:
                return "On bicycle";
            case DetectedActivity.ON_FOOT:
                return "On foot";
            case DetectedActivity.RUNNING:
                return "Running";
            case DetectedActivity.STILL:
                return "Still";
            case DetectedActivity.TILTING:
                return "Tilting";
            case DetectedActivity.UNKNOWN:
                return "Unknown";
            case DetectedActivity.WALKING:
                return "Walking";
            default:
                return "unidentifiable_activity";
        }
    }
}
