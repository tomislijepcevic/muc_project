package muc.project.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;

import muc.project.R;

/**
 * Created by peterus on 11.1.2016.
 */
public class DetailsActivity extends FragmentActivity {
    private GoogleMap mMap;

    private TextView nameLabel;
    private TextView manufacturerLabel;
    private TextView counterLabel;
    private TextView lastSeenLabel;
    private TextView lastLocationLabel;
    private TextView previousLocationsLabel;

    private EditText name;
    private TextView manufacturer;
    private TextView counter;
    private TextView lastSeen;
    private TextView lastLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_details);
        setUpMapIfNeeded();
        initFields();
    }

    private void initFields() {
        nameLabel = (TextView) findViewById(R.id.name_label);
        manufacturerLabel = (TextView) findViewById(R.id.manufacturer_label);
        counterLabel = (TextView) findViewById(R.id.counter_label);
        lastSeenLabel = (TextView) findViewById(R.id.last_seen_label);
        lastLocationLabel = (TextView) findViewById(R.id.last_location_label);
        previousLocationsLabel = (TextView) findViewById(R.id.previous_locations_label);
        name = (EditText) findViewById(R.id.name);
        manufacturer = (TextView) findViewById(R.id.manufacturer);
        counter = (TextView) findViewById(R.id.counter);
        lastSeen = (TextView) findViewById(R.id.last_seen);
        lastLocation = (TextView) findViewById(R.id.last_location);

        // Append ":" to all the labels.
        nameLabel.append(":");
        manufacturerLabel.append(":");
        counterLabel.append(":");
        lastSeenLabel.append(":");
        lastLocationLabel.append(":");
        previousLocationsLabel.append(":");

        // TODO: Set name,manufactuter,counter,lastSeen,lastLocation, based on current selected device.
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        // TODO add markers and stuff.
    }
}

