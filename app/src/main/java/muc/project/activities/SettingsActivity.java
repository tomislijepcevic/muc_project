package muc.project.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import muc.project.R;
import muc.project.helpers.Constants;
import muc.project.helpers.Validation;

/**
 * Created by peterus on 11.1.2016.
 */
public class SettingsActivity extends ActionBarActivity {

    private EditText broadCaptureDuration;
    private EditText narrowCaptureDuration;
    private Button saveSettingsButton;

    private boolean formErrors;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        initFields();
    }

    private void initFields(){
        broadCaptureDuration = (EditText) findViewById(R.id.broadCaptureDuration_exitText);
        narrowCaptureDuration = (EditText) findViewById(R.id.narrowCaptureDuration_editText);
        saveSettingsButton = (Button) findViewById(R.id.save_settings_button);

        // Get stored data.
        SharedPreferences settings = getSharedPreferences(Constants.SETTINGS_PREFS, MODE_PRIVATE);
        int broadCaptureDurationInt = settings.getInt(Constants.AIRODUMP_BROAD_CAPTURE_DURATION, 10);
        int narrowCaptureDurationInt = settings.getInt(Constants.AIRODUMP_NARROW_CAPTURE_DURATION, 10);

        // Update fields.
        broadCaptureDuration.setText("" + broadCaptureDurationInt);
        narrowCaptureDuration.setText("" + narrowCaptureDurationInt);

        // Setup listeners.
        saveSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickRegisterButton();
            }
        });
    }

    private void onClickRegisterButton() {
        formErrors = false;

        String broadCaptureDurationText = broadCaptureDuration.getText().toString();
        String narrowCaptureDurationText = narrowCaptureDuration.getText().toString();

        broadCaptureDuration.setError(null);
        if (!Validation.isPositiveIntegerBetween(broadCaptureDurationText, 1, 100)){
            formErrors = true;
            broadCaptureDuration.setError(getString(R.string.broadCaptureDuration_error));
        }

        narrowCaptureDuration.setError(null);
        if (!Validation.isPositiveIntegerBetween(narrowCaptureDurationText, 1, 100)){
            formErrors = true;
            narrowCaptureDuration.setError(getString(R.string.narrowCaptureDuration_error));
        }

        if (!formErrors){
            SharedPreferences settings = getSharedPreferences(Constants.SETTINGS_PREFS, MODE_PRIVATE);
            SharedPreferences.Editor settingsEditor = settings.edit();

            settingsEditor.putInt(Constants.AIRODUMP_BROAD_CAPTURE_DURATION, Integer.parseInt(broadCaptureDurationText));
            settingsEditor.putInt(Constants.AIRODUMP_NARROW_CAPTURE_DURATION, Integer.parseInt(narrowCaptureDurationText));

            settingsEditor.commit();
            finish();
        }
    }
}
