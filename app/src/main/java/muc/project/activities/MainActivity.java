package muc.project.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import muc.project.DBHelper;
import muc.project.R;
import muc.project.helpers.ClientArrayAdapter;
import muc.project.model.Client;
import muc.project.model.DaoSession;
import muc.project.services.ActivityService;

public class MainActivity extends ActionBarActivity {

    private ClientArrayAdapter _subscribedListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initFields();

        Intent activityRecognizerServiceIntent = new Intent(getApplicationContext(), ActivityService.class);
        startService(activityRecognizerServiceIntent);
    }

    private void initFields() {
        Button scanButton = (Button) findViewById(R.id.scan_btn);
        Button settingsButton = (Button) findViewById(R.id.settings_btn);
        ListView subscribedListView = (ListView) findViewById(R.id.subscribed_listview);
        TextView emptyTextView = (TextView) findViewById(R.id.empty_textview);

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ScanningActivity.class);
                startActivity(intent);
            }
        });

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

        subscribedListView.setEmptyView(emptyTextView);
        _subscribedListAdapter = new ClientArrayAdapter(this);
        subscribedListView.setAdapter(_subscribedListAdapter);

        DBHelper dbHelper = new DBHelper(this);
        DaoSession daoSession = dbHelper.getSession(false);

        for (Client client : daoSession.queryRaw(Client.class, "Where T.subscribed = 1")) {
            _subscribedListAdapter.add(client);
        }

        daoSession.getDatabase().close();

        subscribedListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(getApplicationContext(), DetailsActivity.class);
                Client client = (Client) _subscribedListAdapter.getItem(position);
                i.putExtra("id", client.getId());
                i.putExtra("position", position);
                startActivityForResult(i, 1);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK) {
                int position = data.getIntExtra("position", 0);
                _subscribedListAdapter.getItem(position).refresh();
                _subscribedListAdapter.notifyDataSetChanged();
            }
        }
    }
}
