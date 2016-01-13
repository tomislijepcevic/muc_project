package muc.project.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import muc.project.R;
import muc.project.services.WifiSensingIS;

public class MainActivity extends ActionBarActivity {

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
                Intent wifiSensingServiceIntent = new Intent(getApplicationContext(), WifiSensingIS.class);
                startService(wifiSensingServiceIntent);
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
}
