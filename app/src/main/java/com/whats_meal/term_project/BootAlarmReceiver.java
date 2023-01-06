package com.whats_meal.term_project;

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

import java.util.Arrays;
import java.util.Calendar;

public class BootAlarmReceiver extends BroadcastReceiver {
    public AlarmManager alarmManager;
    SharedPreferences pref;
    Calendar calendar;

    String[][][] food_time = {{
            {"", "", ""},// 아침[천안[기식,학식,직원]]
            {"", "", ""},// 아침[예산[기식,학식,직원]]
            {"07:30 ~ 09:00", "", ""} // 아침[신관[기식,학식,직원]]
    },{
            {"11:40 ~ 13:30", "11:30 ~ 13:30", "11:30 ~ 13:30"}, // 점심[천안[기식,학식,직원]]
            {"", "12:00 ~ 13:30", "12:00 ~ 13:30"}, // 점심[예산[기식,학식,직원]]
            {"11:30 ~ 13:30", "11:30 ~ 13:30", "11:30 ~ 13:30"}  // 점심[신관[기식,학식,직원]]
    },{
            {"17:40 ~ 19:00", "", ""}, // 저녁[천안[기식,학식,직원]]
            {"", "17:40 ~ 19:00", ""}, // 저녁[예산[기식,학식,직원]]
            {"17:30 ~ 19:00", "", ""}} // 저녁[신관[기식,학식,직원]]
    };
    @Override
    public void onReceive(Context context, Intent intent) {
        pref = context.getSharedPreferences("pref", Activity.MODE_PRIVATE);
        boolean chk = pref.getBoolean("notify",false);
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED") && chk) {
            setAlarm(context);
        }
    }

    @SuppressLint({"UseCompatLoadingForDrawables", "UnspecifiedImmutableFlag"})
    private void setAlarm(Context context) {
        calendar = Calendar.getInstance();
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        createNotificationChannel(context);
        int select_campus = pref.getInt("select_campus", 0);
        int select_room = pref.getInt("select_restaurant", 0);
        int[] t1 =  Arrays.stream(food_time[0][select_campus][select_room].split(" ~ ")[0].split(":")).mapToInt(Integer::parseInt).toArray();
        int[] t2 =  Arrays.stream(food_time[1][select_campus][select_room].split(" ~ ")[0].split(":")).mapToInt(Integer::parseInt).toArray();
        int[] t3 =  Arrays.stream(food_time[2][select_campus][select_room].split(" ~ ")[0].split(":")).mapToInt(Integer::parseInt).toArray();

        int[][] alarms = {{t1[0], t1[1], 0}, {t2[0], t2[1], 1}, {t3[0], t3[1], 2}};

        for (int[] alarm : alarms) {
            makeNotification(context, alarm[0], alarm[1], alarm[2]);
            Log.i("data","알람 입력");
        }

    }

    private void makeNotification(Context context, int hour, int minute, int putData) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("putData", putData);
        intent.putExtra("hour", hour);
        intent.putExtra("minute", minute);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, putData, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }

    private void createNotificationChannel(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "알림";
            String description = "알림입니다!";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("JoEun_Notification_Channel", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

        }
    }
}
