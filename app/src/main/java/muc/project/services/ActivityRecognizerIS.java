package muc.project.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionResult;
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


public class ActivityRecognizerIS extends IntentService implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    protected static final String TAG = "ActivityRecognizerIS";
    private GoogleApiClient mGoogleApiClient;
    private DBHelper _dbHelper;
    private SubscribedClientDetectedBroadcastReceiver _clientDetectedBroadcastReceiver;
    private UnsubscribedClientDetectedBroadcastReceiver _strangerDetectedBroadcastReceiver;
    private ScanningEndedBroadcastReceiver _scanningEndedBroadcastReceiver;
    private ClientDao clientDao;

    public ActivityRecognizerIS() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        buildGoogleApiClient();
        mGoogleApiClient.connect();

        // Add test client
        Client client = new Client();
        client.setName("Janez");
        client.setManufacturer("Apple");
        client.setMac("58:b0:35:77:6a:ea");
        client.setSubscribed(true);
        client.setCounter(50);

        History history = new History();
        history.setClient(client);
        history.setTimestamp(new Date());

        _dbHelper = new DBHelper(ApplicationContext.getInstance());
        DaoSession session = _dbHelper.getSession(true);
        clientDao = session.getClientDao();
        session.insertOrReplace(client);
        Log.i(TAG, "Actvity recognizer launched");

    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(ActivityRecognition.API)
                .build();
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(TAG, "Got activity intent");
        if(intent != null) {

            // Get most probable activity
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);

            if(result!=null) {
                DetectedActivity mostProbableActivity = result.getMostProbableActivity();
                int activityType = mostProbableActivity.getType();
                String activityString = getActivityString(activityType);

                // Check if activity is STILL
                if (activityType == DetectedActivity.STILL) {
                    Log.i(TAG, "Device is STILL");

                    LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);

                    // Register broadcast receiver and show notification on received broadcast
                    _clientDetectedBroadcastReceiver = new SubscribedClientDetectedBroadcastReceiver();
                    lbm.registerReceiver(_clientDetectedBroadcastReceiver,
                            new IntentFilter(Constants.SUBSCRIBED_CLIENT_DETECTED_BROADCAST_RESULT));

                    /* // Register broadcast receiver and show notification on received broadcast
                    _strangerDetectedBroadcastReceiver = new UnsubscribedClientDetectedBroadcastReceiver();
                    lbm.registerReceiver(_strangerDetectedBroadcastReceiver,
                            new IntentFilter(Constants.UNSUBSCRIBED_CLIENT_DETECTED_BROADCAST_RESULT));*/

                    // Register broadcast receiver and unregister both broadcast receivers on receiver broadcast
                    _scanningEndedBroadcastReceiver = new ScanningEndedBroadcastReceiver();
                    lbm.registerReceiver(_scanningEndedBroadcastReceiver,
                            new IntentFilter(Constants.WIFI_SENSING_ENDED_BROADCAST_RESULT));

                    // Start service WifiScaningIS
                    Intent wifiSensingServiceIntent = new Intent(getApplicationContext(), WifiSensingIS.class);
                    startService(wifiSensingServiceIntent);
                }
            }
        }
    }

    public void requestActivityUpdates() {
        if (!mGoogleApiClient.isConnected()) {
            Log.i(TAG, "GoogleAPI not connected");
            return;
        }
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(
                mGoogleApiClient,
                Constants.ACTIVITY_RECOGNITION_INTERVAL,
                getActivityDetectionPendingIntent()
        );
        Log.i(TAG, "Acitivity updates requested");

    }

    public void removeActivityUpdates() {
        if (!mGoogleApiClient.isConnected()) {
            Log.i(TAG, "GoogleAPI not connected");
            return;
        }
        ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(
                mGoogleApiClient,
                getActivityDetectionPendingIntent()
        );
    }

    private PendingIntent getActivityDetectionPendingIntent() {
        Intent intent = new Intent(this, ActivityRecognizerIS.class);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
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

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "GoogleAPI connected");
        requestActivityUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "GoogleAPI connection failed. Connection result: "+connectionResult);
    }

    class SubscribedClientDetectedBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // Build notification
            Log.i(TAG, "Client detected broadcast received 1111111111111111111111111111111111111111111111111111111111111111");

            int clientId = intent.getIntExtra("key", -1);

            Client client = clientDao.queryRaw("Where T._id = ?", Integer.toString(clientId)).get(0);

            Intent detailIntent = new Intent(getApplicationContext(), DetailsActivity.class);
            detailIntent.putExtra("id", clientId);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                    detailIntent, 0);

            String title = "Friend nearby";
            String message = "Name: "+client.getName()+" Device: "+client.getManufacturer();
            Log.i(TAG, message);

            showNotification(title, message, pendingIntent);
        }
    }

    class UnsubscribedClientDetectedBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent != null) {

                Log.i(TAG, "Stranger detected broadcast received 1111111111111111111111111111111111111111111111111111111111111111");
                int clientId = intent.getIntExtra("key", -1);
                Client client = clientDao.queryRaw("Where T._id = ?", Integer.toString(clientId)).get(0);

                Intent detailIntent = new Intent(getApplicationContext(), DetailsActivity.class);
                detailIntent.putExtra("id", clientId);
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                        detailIntent, 0);

                String title = "Possible friend nearby";
                String message = "Device: "+client.getManufacturer()+" Counter: "+client.getCounter();

                showNotification(title, message, pendingIntent);
            }
        }
    }

    class ScanningEndedBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Scanning ended broadcast received 1111111111111111111111111111111111111111111111111111111111111111");
            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getApplicationContext());
            lbm.unregisterReceiver(_clientDetectedBroadcastReceiver);
            lbm.unregisterReceiver(_strangerDetectedBroadcastReceiver);
            lbm.unregisterReceiver(_scanningEndedBroadcastReceiver);
        }
    }

    private void showNotification(String title, String message, PendingIntent pendingIntent) {
        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle(title)
                .setContentText(message)
                .setCategory(Notification.CATEGORY_SOCIAL)
                .setPriority(1)
                .setAutoCancel(true)
                .addAction(R.drawable.common_google_signin_btn_icon_dark, "Details", pendingIntent)
                .build();

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(10, notification);
        Log.i(TAG, "Notification showed");
    }
}
