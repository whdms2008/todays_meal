package com.example.term_project;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;

public class BootAlarmReceiver extends BroadcastReceiver {
    public AlarmManager alarmManager;
    SharedPreferences pref;
    Calendar calendar;

    @Override
    public void onReceive(Context context, Intent intent) {
        pref = context.getSharedPreferences("pref", Activity.MODE_PRIVATE);
        boolean chk = pref.getBoolean("notify",false);
        Log.i("data","부트 시작"+chk);
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED") && chk) {
            setAlarm(context);
        }else{
            Log.i("data","알람설정 안되어있음., "+chk);
        }
    }

    @SuppressLint({"UseCompatLoadingForDrawables", "UnspecifiedImmutableFlag"})
    private void setAlarm(Context context) {

        Log.i("data","알람 설정 시작");
        calendar = Calendar.getInstance();
        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

//        Intent intent = new Intent(this, AlarmReceiver.class);
        createNotificationChannel(context,"0");

        makeNotification(context, alarmIntent, 7, 30, 0, 0);

        makeNotification(context, alarmIntent, 11, 40, 1, 1);
//
        makeNotification(context, alarmIntent, 17, 30, 2, 2);

    }

    private void makeNotification(Context context, Intent intent, int hour, int minute, int requestCode, int putData) {
        intent.putExtra("putData", putData);
        intent.putExtra("requestCode", requestCode);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        Log.i("data","알람 설정 : "+
                calendar.get(Calendar.MONTH) + "월" +
                calendar.get(Calendar.DATE) + "일 - " +
                calendar.get(Calendar.HOUR_OF_DAY)+ ":" +
                calendar.get(Calendar.MINUTE)+ ":" +
                calendar.get(Calendar.SECOND)+ ":" +
                calendar.get(Calendar.MILLISECOND));
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
