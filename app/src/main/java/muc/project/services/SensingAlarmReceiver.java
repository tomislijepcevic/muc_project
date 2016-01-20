package muc.project.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import muc.project.ApplicationContext;
import muc.project.helpers.Constants;


public class SensingAlarmReceiver extends BroadcastReceiver {
    final static private String TAG = "SensingAlarmReceiver";

    static synchronized void startAlarm(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(Constants.ALARM_ACTION);
        PendingIntent pi = PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        long time = System.currentTimeMillis() + Constants.ALARM_INTERVAL;

        if (Build.VERSION.SDK_INT >= 19)
            am.setExact(AlarmManager.RTC_WAKEUP, time ,pi);
        else
            am.set(AlarmManager.RTC_WAKEUP, time ,pi);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Alarm broadcast received.");
        Intent broadcastIntent = new Intent(Constants.ALARM_BROADCAST);
        LocalBroadcastManager.getInstance(ApplicationContext.getInstance()).sendBroadcast(broadcastIntent);
    }

    static void stopAlarm(Context context) {
        Intent intent = new Intent(Constants.ALARM_ACTION);
        PendingIntent pi = PendingIntent.getBroadcast(ApplicationContext.getInstance(), 1, intent, 0);
        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pi);
    }
}
