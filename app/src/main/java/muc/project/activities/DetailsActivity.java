package muc.project.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.SimpleDateFormat;
import java.util.List;

import muc.project.DBHelper;
import muc.project.R;
import muc.project.model.Client;
import muc.project.model.DaoSession;
import muc.project.model.History;

/**
 * Created by peterus on 11.1.2016.
 */
public class DetailsActivity extends FragmentActivity {

    private GoogleMap _map;
    private Client _client;
    private List<History> _history;
    private SimpleDateFormat _dateFormat = new SimpleDateFormat("dd-MM 'at' HH'h'");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_details);

        Bundle b = getIntent().getExtras();
        Long id = b.getLong("id");

        DBHelper dbHelper = new DBHelper(this);
        DaoSession daoSession = dbHelper.getSession(true);
        _client = daoSession.load(Client.class, id);
        _history = _client.getHistory();

        setUpMapIfNeeded();
        initFields();
    }

    private void initFields() {
        TextView nameLabel = (TextView) findViewById(R.id.name_label);
        TextView manufacturerLabel = (TextView) findViewById(R.id.manufacturer_label);
        TextView counterLabel = (TextView) findViewById(R.id.counter_label);
        TextView lastSeenLabel = (TextView) findViewById(R.id.last_seen_label);
        TextView lastLocationLabel = (TextView) findViewById(R.id.last_location_label);
        TextView previousLocationsLabel = (TextView) findViewById(R.id.previous_locations_label);
        final EditText name = (EditText) findViewById(R.id.name);
        TextView manufacturer = (TextView) findViewById(R.id.manufacturer);
        TextView counter = (TextView) findViewById(R.id.counter);
        TextView lastSeen = (TextView) findViewById(R.id.last_seen);
        TextView lastLocation = (TextView) findViewById(R.id.last_location);
        Button saveButton = (Button) findViewById(R.id.save_btn);

        // Append ":" to all the labels.
        nameLabel.append(":");
        manufacturerLabel.append(":");
        counterLabel.append(":");
        lastSeenLabel.append(":");
        lastLocationLabel.append(":");
        previousLocationsLabel.append(":");

        name.setText(_client.getName());
        manufacturer.setText(_client.getManufacturer());
        counter.setText(Integer.toString(_client.getCounter()));
        lastSeen.setText(_dateFormat.format(_history.get(_history.size() - 1).getTimestamp()));

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String clientName = name.getText().toString();

                if (clientName != null && clientName.length() > 0) {
                    _client.setName(clientName);
                    _client.update();

                    setResult(Activity.RESULT_OK, getIntent());
                } else {
                    setResult(Activity.RESULT_CANCELED, getIntent());
                }

                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (_map == null) {
            // Try to obtain the map from the SupportMapFragment.
            _map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (_map != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        for (History history : _client.getHistory()) {
            if (history.getLat() != null && history.getLng() != null) {
                MarkerOptions markerOptions = new MarkerOptions()
                        .title(_dateFormat.format(history.getTimestamp()))
                        .position(new LatLng(history.getLat(), history.getLng()))
                        .visible(true);

                _map.addMarker(markerOptions);
            }
        }
    }
}

