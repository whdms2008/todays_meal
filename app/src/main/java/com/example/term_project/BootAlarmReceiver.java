package com.example.term_project;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import java.util.Calendar;

public class BootAlarmReceiver extends BroadcastReceiver {
    public AlarmManager alarmManager;
    SharedPreferences pref;
    Calendar calendar;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED") && pref.getBoolean("notify",false)) {
            setAlarm(context, intent);
        }
    }

    @SuppressLint({"UseCompatLoadingForDrawables", "UnspecifiedImmutableFlag"})
    private void setAlarm(Context context, Intent intent) {
        calendar = Calendar.getInstance();
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

//        Intent intent = new Intent(this, AlarmReceiver.class);
        createNotificationChannel(context,"0");

        makeNotification(context, intent, 7, 30, 0, 0);

        makeNotification(context, intent, 11, 40, 1, 1);
//
        makeNotification(context, intent, 17, 30, 2, 2);

    }

    private void makeNotification(Context context, Intent intent, int hour, int minute, int requestCode, int putData) {
        intent.putExtra("putData", putData);
        intent.putExtra("requestCode", requestCode);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
    }

    private void createNotificationChannel(Context context, String id) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "알림";
            String description = "알림입니다!";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(id, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

        }
    }
}
