package muc.project.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import muc.project.R;

public class MainActivity extends ActionBarActivity {

    private static final String TAG = "MainActivity";
    private static final String AIRODUMP_UTIL_SCRIPT = "q4.sh";
    private static final String OUI_CSV = "oui.csv";
    private static final int AIRODUMP_BROAD_CAPTURE_DURATION = 5;
    private static final int AIRODUMP_NARROW_CAPTURE_DURATION = 10;

    private Button detailsButton;
    private Button scanButton;
    private Button settingsButton;
    private ListView subscribedListView;
    private TextView emptyTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initFields();

        /*
        try {
            makeNewFile(getAssets().open(OUI_CSV), OUI_CSV);
            File script = makeNewFile(getAssets().open(AIRODUMP_UTIL_SCRIPT), AIRODUMP_UTIL_SCRIPT);
            execScriptAsRoot(script);
        } catch (IOException e) {
            e.printStackTrace();
        }
        */

    }

    private void initFields() {
        detailsButton = (Button) findViewById(R.id.todo2_btn); // TODO: Details should be changed to onClick ListView item.
        detailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start details activity.
                Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
                startActivity(intent);
            }
        });

        scanButton = (Button) findViewById(R.id.scan_btn);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start scanning activity.
            }
        });

        settingsButton = (Button) findViewById(R.id.settings_btn);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start settings activity.
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

        subscribedListView = (ListView) findViewById(R.id.subscribed_listview);
        emptyTextView = (TextView) findViewById(R.id.empty_textview);
        subscribedListView.setEmptyView(emptyTextView);
        subscribedListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Start details activity.
            }
        });
    }

    private void execScriptAsRoot(File script) {
        try {
            final Runtime runtime = Runtime.getRuntime();
            File dir = script.getParentFile();

            runtime.exec("su");
            runtime.exec("mount -o remount,rw /system ");
            Process p = runtime.exec(script.getAbsolutePath(), null, dir);

            Log.d(TAG, "Start");
            String line;
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            while ((line = input.readLine()) != null) {
                //textView.setText("result : " + line);
                Log.d(TAG, line);
            }

            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File makeNewFile(InputStream is, String filename) throws IOException {
        File file = new File (getFilesDir(), filename);
        FileOutputStream os = new FileOutputStream(file);

        file.setExecutable(true, false);
        file.setWritable(true, false);
        file.setReadable(true, false);

        int read = 0;
        byte[] bytes = new byte[1024];

        while ((read = is.read(bytes)) != -1)
            os.write(bytes, 0, read);

        is.close();
        os.close();

        return file;
    }
}
