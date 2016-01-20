package muc.project.helpers;

import android.content.Intent;

/**
 * Created by peterus on 11.1.2016.
 */
public class Constants {
    public static final String SETTINGS_PREFS = "SettingsPrefs";
    public static final String AIRODUMP_BROAD_CAPTURE_DURATION = "AIRODUMP_BROAD_CAPTURE_DURATION";
    public static final String AIRODUMP_NARROW_CAPTURE_DURATION = "AIRODUMP_NARROW_CAPTURE_DURATION";
    public static final String LOCATION_SAMPLING_SETTING = "LocationSamplingSetting";
    public static final String SUBSCRIBED_CLIENT_DETECTED_BROADCAST_RESULT = "muc.project.subscribed_client_detected.BROADCAST_RESULT";
    public static final String UNSUBSCRIBED_CLIENT_DETECTED_BROADCAST_RESULT = "muc.project.unsubscribed_client_detected.BROADCAST_RESULT";
    public static final String WIFI_SENSING_ENDED_BROADCAST_RESULT = "muc.project.wifi_sensing_ended.BROADCAST_RESULT";
    public static final long ACTIVITY_RECOGNITION_INTERVAL = 10 * 1000;
    public static final long ALARM_INTERVAL = 30 * 1000;
    public static final String ALARM_ACTION = "muc.project.alarm.SensingAlarm";
    public static final String ALARM_BROADCAST = "muc.project.alarm.BROADCAST";
    public static final String DETECTED_ACTIVITY = "muc.project.activity.DETECTED_ACTIVITY";
    public static final String ACTIVITY_BROADCAST = "muc.project.activity.ACTIVITY_BROADCAST";
}
