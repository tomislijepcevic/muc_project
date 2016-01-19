package muc.project.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import muc.project.ApplicationContext;
import muc.project.DBHelper;
import muc.project.helpers.Constants;
import muc.project.model.AccessPoint;
import muc.project.model.AccessPointDao;
import muc.project.model.Client;
import muc.project.model.ClientDao;
import muc.project.model.Client_AccessPoint;
import muc.project.model.DaoSession;
import muc.project.model.History;

import static muc.project.helpers.Validation.isMacValid;


public class WifiSensingIS extends IntentService implements SharedPreferences.OnSharedPreferenceChangeListener,
        GoogleApiClient.ConnectionCallbacks, LocationListener {

    private static final String TAG = "WifiSensingIS";
    private static final String UTIL_SCRIPT = "airodump-ng.sh";
    private static final String OUI_CSV = "oui.csv";

    private DBHelper _dbHelper;
    private SharedPreferences _sharedPreferences;
    private int _broadCaptureDuration = 5;
    private int _narrowCaptureDuration = 5;
    private File _utilScript;
    private File _ouiCsv;
    private String _commandToExecute;
    private Process _process;
    private GoogleApiClient _googleApiClient;
    private Location _location;
    private List<History> _historyToUpdateLocation;

    public WifiSensingIS() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        _dbHelper = new DBHelper(ApplicationContext.getInstance());
        _sharedPreferences = getSharedPreferences(Constants.SETTINGS_PREFS, MODE_PRIVATE);
        _sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        _broadCaptureDuration = _sharedPreferences.getInt(
                Constants.AIRODUMP_BROAD_CAPTURE_DURATION,
                _broadCaptureDuration);
        _narrowCaptureDuration = _sharedPreferences.getInt(
                Constants.AIRODUMP_BROAD_CAPTURE_DURATION,
                _narrowCaptureDuration);
        _utilScript = new File (getFilesDir(), UTIL_SCRIPT);
        _ouiCsv = new File (getFilesDir(), OUI_CSV);
        _commandToExecute = "mount -o remount,rw /system && sh " + _utilScript.getAbsolutePath();
        _historyToUpdateLocation = new ArrayList<>();

        buildGoogleApiClient();

        try {
            ensureFilesExists();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void ensureFilesExists() throws IOException {
        if (!_utilScript.isFile())
            rewriteFile(_utilScript, getAssets().open(UTIL_SCRIPT));

        if (!_ouiCsv.isFile())
            rewriteFile(_ouiCsv, getAssets().open(OUI_CSV));

        if (!_utilScript.canExecute())
            _utilScript.setExecutable(true);
    }

    protected synchronized void buildGoogleApiClient() {
        _googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onDestroy() {
        if (_process != null) {
            _process.destroy();

            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.redirectErrorStream(true);
            processBuilder.command(new String[]{"su", "-c", "killall airodump-ng"});
        }

        if (_googleApiClient.isConnected()) {
            _googleApiClient.disconnect();
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            try {
                ensureFilesExists();
                _googleApiClient.connect();

                ProcessBuilder processBuilder = new ProcessBuilder();
                processBuilder.redirectErrorStream(true);
                processBuilder.directory(_utilScript.getParentFile());
                processBuilder.command(new String[]{"su", "-c", _commandToExecute});

                Log.d(TAG, "Detection: STARTED");
                _process = processBuilder.start();
                logStream(_process.getErrorStream());

                String line;
                BufferedReader input = new BufferedReader(new InputStreamReader(_process.getInputStream()));

                while ((line = input.readLine()) != null) {
                    try {
                        Client client = onClientDetected(line);

                        Intent localIntent = new Intent(client.getSubscribed() ?
                                Constants.SUBSCRIBED_CLIENT_DETECTED_BROADCAST_RESULT :
                                Constants.UNSUBSCRIBED_CLIENT_DETECTED_BROADCAST_RESULT
                        );
                        localIntent.putExtra("key", client.getId());
                        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
                    } catch (ClientMalformedDescriptionException e) {
                        e.printStackTrace();
                    }
                }

                _process.waitFor();

                Intent localIntent = new Intent(Constants.WIFI_SENSING_ENDED_BROADCAST_RESULT);
                LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);

                Log.d(TAG, "Detection: ENDED");
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            /*
            Intent broadcastIntent = new Intent(Constants.SUBSCRIBED_CLIENT_DETECTED_BROADCAST_RESULT);
            intent.putExtra("key", 1);
            LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);*/
        }
    }

    private Client onClientDetected(String clientProperties) throws ClientMalformedDescriptionException {
        Log.d(TAG, "Client detected with properties: " + clientProperties);

        String[] properties = clientProperties.split(",");

        if (properties.length != 4) {
            Log.d(TAG, "Client doesn't have all the properties");
            throw new ClientMalformedDescriptionException();
        }

        String mac = properties[1];
        String manufacturer = properties[2];
        String bssid = properties[3];

        if (!isMacValid(mac)) {
            Log.d(TAG, "Client has invalid mac");
            throw new ClientMalformedDescriptionException();
        }

        DaoSession session = _dbHelper.getSession(true);

        ClientDao clientDao = session.getClientDao();
        Client client;
        try {
            client = clientDao.queryRaw("Where T.mac Like ?", mac).get(0);
            client.setCounter(client.getCounter() + 1);
            client.update();
        } catch (IndexOutOfBoundsException e) {
            client = new Client();
            client.setMac(mac);
            client.setManufacturer(manufacturer);
            client.setSubscribed(false);
            client.setCounter(1);
            clientDao.insert(client);
        }

        History history = new History();
        history.setClient(client);
        history.setTimestamp(new Date());

        if (_location != null) {
            history.setLat(history.getLat());
            history.setLng(history.getLng());
        } else {
            _historyToUpdateLocation.add(history);
        }

        session.insert(history);

        if (isMacValid(bssid)) {
            AccessPointDao apDao = session.getAccessPointDao();
            AccessPoint ap;
            try {
                ap = apDao.queryRaw("Where T.mac Like ?", mac).get(0);
            } catch (IndexOutOfBoundsException e) {
                ap = new AccessPoint();
                ap.setMac(bssid);
                apDao.insert(ap);
            }

            if (!client.getClientAccessPoint().contains(ap)) {
                Client_AccessPoint clientAp = new Client_AccessPoint();
                clientAp.setClient(client);
                clientAp.setAccessPoint(ap);
                session.insert(clientAp);
            }
        }

        return client;
    }

    private void logStream(InputStream is) throws IOException {
        String line;
        BufferedReader input = new BufferedReader(new InputStreamReader(is));

        while ((line = input.readLine()) != null) {
            Log.d(TAG, line);
        }

        input.close();
    }

    private void rewriteFile(File file, InputStream is) throws IOException {
        FileOutputStream os = new FileOutputStream(file);

        int read = 0;
        byte[] bytes = new byte[1024];

        while ((read = is.read(bytes)) != -1)
            os.write(bytes, 0, read);

        is.close();
        os.close();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(Constants.AIRODUMP_BROAD_CAPTURE_DURATION)) {
            _broadCaptureDuration = sharedPreferences.getInt(key, _broadCaptureDuration);
        } else if (key.equals(Constants.AIRODUMP_NARROW_CAPTURE_DURATION)) {
            _narrowCaptureDuration = sharedPreferences.getInt(key, _narrowCaptureDuration);
        }
    }

    protected void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);

        LocationServices.FusedLocationApi.requestLocationUpdates(_googleApiClient, locationRequest, this);
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(_googleApiClient, this);
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        startLocationUpdates();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location == null) {
            return;
        }

        _location = location;

        for (History history : _historyToUpdateLocation) {
            history.setLat((float) location.getLatitude());
            history.setLng((float) location.getLongitude());
            history.update();
        }

        _historyToUpdateLocation.clear();
        stopLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {}

    private class ClientMalformedDescriptionException extends Exception {}
}
