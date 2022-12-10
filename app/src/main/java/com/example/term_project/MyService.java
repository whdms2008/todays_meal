package com.example.term_project;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Handler;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Calendar;


public class MyService extends Service {
    int select_id = -1;
    SharedPreferences pref;
    String CHANNEL_ID = "whdms1107";
    NotificationManager Notifi_M;
    ServiceThread thread;
    String menu = "";
    String[] title = {"조식 메뉴", "중식 메뉴", "석식 메뉴"};
    int[] breakfast = {450, 540};
    int[] lunch = {690, 810};
    int[] dinner = {1050, 1140};
    int[] drawables = {R.drawable.sun_morning, R.drawable.breakfast, R.drawable.dinner_icon};

    public MyService() {
        System.out.println("생성자!!!");
    }


    @Override
    public IBinder onBind(Intent intent) {
        System.out.println("바인드!!!");
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("명령!!!");
        Notifi_M = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        myServiceHandler handler = new myServiceHandler();
        thread = new ServiceThread(handler);
        thread.stopForever();
        return START_STICKY;
    }

    //서비스가 종료될 때 할 작업
    public void onDestroy() {
        System.out.println("파괴ㅣ!!!");
        myServiceHandler handler = new myServiceHandler();
        thread = new ServiceThread(handler);
        thread.start();
    }

    public void start() {
        myServiceHandler handler = new myServiceHandler();
        thread = new ServiceThread(handler);
        thread.start();
    }

    public void stop() {
        myServiceHandler handler = new myServiceHandler();
        thread = new ServiceThread(handler);
        thread.stopForever();
    }

    @SuppressLint("HandlerLeak")
    public class myServiceHandler extends Handler {
        @SuppressLint("ObsoleteSdkInt")
        @Override
        public void handleMessage(android.os.Message msg) {
            pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);
            if (!pref.getBoolean("notify", false)) {
                return;
            }
            Calendar cal1 = Calendar.getInstance();
            int hour = cal1.get(Calendar.HOUR_OF_DAY);
            int minute = cal1.get(Calendar.MINUTE);


            int times = (hour * 60) + minute;
            if (breakfast[0] <= times && times <= breakfast[1]) {
                if (select_id != 0){
                    select_id = 0;
                    Notification();
                }
            } else if (lunch[0] <= times && times <= lunch[1]) {
                if (select_id != 1){
                    select_id = 1;
                    Notification();
                }
            } else if (dinner[0] <= times && times <= dinner[1]) {
                if (select_id != 2) {
                    select_id = 2;
                    Notification();
                }
            } else if (select_id != -1){
                select_id = -1;
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancelAll();
            }

        }
    }

    private void Notification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        Intent intent = new Intent(MyService.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(MyService.this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            @SuppressLint("WrongConstant")
            NotificationChannel notificationChannel = new NotificationChannel("my_notification", "n_channel", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription("알림입니다");
            notificationChannel.setName("알림 설정");
            assert notificationManager != null;
            notificationManager.createNotificationChannel(notificationChannel);
        }
        menu = getStringArrayPref(select_id);
        String rest_name = pref.getString("alarm_food_name","");
        String rest_type_name = pref.getString("alarm_food_type_name","");
        if (menu.equals("")) {

            menu = "오늘 밥 없습니다";
        } else {
            menu = menu.replace(" ", "\n");
        }
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(MyService.this, CHANNEL_ID)
                .setAutoCancel(false)
                .setOngoing(true)
                .setSmallIcon(drawables[select_id])
                .setContentTitle("[ "+rest_type_name+" | "+rest_name+" ] - "+ title[select_id])
                .setContentText(menu)
                .setVibrate(new long[]{0, 3000})
                .setContentIntent(pendingIntent)
                .setDefaults(Notification.DEFAULT_ALL)
                .setChannelId("my_notification")
                .setColor(Color.parseColor("#ffffff"))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(menu));
        notificationManager.notify(0, notificationBuilder.build());

    }

    private String getStringArrayPref(int num) {
        String json = pref.getString("food_menus", "");
        JSONArray a;
        try {
            a = new JSONArray(json);
            return a.optString(num);
        } catch (JSONException ignored) {
            return "";
        }
    }
}