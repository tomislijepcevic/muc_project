package muc.project.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import muc.project.ApplicationContext;
import muc.project.DBHelper;
import muc.project.helpers.Constants;
import muc.project.model.AccessPoint;
import muc.project.model.AccessPointDao;
import muc.project.model.Client;
import muc.project.model.ClientDao;
import muc.project.model.Client_AccessPoint;
import muc.project.model.Client_AccessPointDao;
import muc.project.model.DaoSession;
import muc.project.model.History;
import muc.project.model.HistoryDao;


public class WifiSensingIS extends IntentService implements SharedPreferences.OnSharedPreferenceChangeListener {

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
        _commandToExecute = "mount -o remount,rw /system && " +
                "sh " + _utilScript.getAbsolutePath();

        try {
            ensureFilesExists();
        } catch (IOException e) {
            // report an error to user: "Error while creating neccessary files."
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

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            try {
                ensureFilesExists();

                ProcessBuilder processBuilder = new ProcessBuilder();
                processBuilder.redirectErrorStream(true);
                processBuilder.directory(_utilScript.getParentFile());
                processBuilder.command(new String[]{"su", "-c", _commandToExecute});

                Log.d(TAG, "Detection: STARTED");
                Process exec = processBuilder.start();
                logStream(exec.getErrorStream());

                String line;
                BufferedReader input = new BufferedReader(new InputStreamReader(exec.getInputStream()));

                while ((line = input.readLine()) != null)
                    onClientDetected(line);

                exec.waitFor();
                Log.d(TAG, "Detection: ENDED");

            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void onClientDetected(String clientProperties) {
        Log.d(TAG, "Client detected with properties: " + clientProperties);

        String[] properties = clientProperties.split(",");

        if (properties.length != 4) {
            Log.d(TAG, "Client doesn't have all the properties");
            return;
        }

        String mac = properties[1];
        String manufacturer = properties[2];
        String bssid = properties[3];
//            Integer power;

        if (!isMacValid(mac)) {
            Log.d(TAG, "Client has invalid mac");
            return;
        }

        DaoSession session = _dbHelper.getSession(true);

        ClientDao clientDao = session.getClientDao();
        Client client;
        try {
            client = clientDao.queryRaw("Where T.mac Like ?", mac).get(0);
        } catch (IndexOutOfBoundsException e) {
            client = new Client();
            client.setMac(mac);
            client.setManufacturer(manufacturer);
            clientDao.insert(client);
        }

        History history = new History();
        history.setClient(client);
        history.setTimestamp(new Date());
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
    }

    private boolean isMacValid(String mac) {
        Pattern p = Pattern.compile("^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$");
        Matcher m = p.matcher(mac);
        return m.find();
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
}
