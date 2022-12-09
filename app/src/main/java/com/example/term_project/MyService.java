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
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Calendar;
import java.util.Date;
import java.util.Objects;


public class MyService extends Service {
    int select_id = -1;
    SharedPreferences pref;
    String CHANNEL_ID = "whdms1107";
    NotificationManager Notifi_M;
    ServiceThread thread;

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
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            Intent intent = new Intent(MyService.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(MyService.this, 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_ONE_SHOT);
            Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                @SuppressLint("WrongConstant")
                NotificationChannel notificationChannel = new NotificationChannel("my_notification", "n_channel", NotificationManager.IMPORTANCE_HIGH);
                notificationChannel.setDescription("알림입니다");
                notificationChannel.setName("알림 설정");
                assert notificationManager != null;
                notificationManager.createNotificationChannel(notificationChannel);
            }

            Calendar cal = Calendar.getInstance();
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            int minute = cal.get(Calendar.MINUTE);
            String menu = "";
            String title = "";
            System.out.println("실행!!!");
            int[] drawables = {R.drawable.sunrise_icon, R.drawable.breakefast_icon, R.drawable.dinner_icon};
            int num = 0;
            // 시간 정확하게 하게 바꾸고,, 이미 선택된거 select_id 있으면 그걸로 하시오..
            if ((hour >= 7 && minute >= 30) && (hour < 11 && select_id != 0)) {
                select_id = 0;
                menu = getStringArrayPref(0);
                title = "조식 메뉴";
                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(MyService.this, CHANNEL_ID)
                        .setAutoCancel(false)
                        .setOngoing(true)
                        .setSmallIcon(drawables[num])
                        .setContentTitle(title)
                        .setContentText(menu)
                        .setSound(soundUri)
                        .setVibrate(new long[]{0, 3000})
                        .setContentIntent(pendingIntent)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setChannelId("my_notification")
                        .setColor(Color.parseColor("#ffffff"))
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(menu));
//                String asd = String.valueOf(notificationManager.getNotificationChannels().get(0));
//                Toast.makeText(getApplicationContext(),asd
//                        ,Toast.LENGTH_LONG).show();
                notificationManager.cancelAll();
                notificationManager.notify(0, notificationBuilder.build());
            }
            if ((hour >= 11 && minute >= 30) && (hour < 13 && select_id != 1)){
                select_id = 1;
                menu = getStringArrayPref(1);
                title = "중식 메뉴";
                num = 1;
                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(MyService.this, CHANNEL_ID)
                        .setAutoCancel(false)
                        .setOngoing(true)
                        .setSmallIcon(drawables[num])
                        .setContentTitle(title)
                        .setContentText(menu)
                        .setSound(soundUri)
                        .setVibrate(new long[]{0, 3000})
                        .setContentIntent(pendingIntent)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setChannelId("my_notification")
                        .setColor(Color.parseColor("#ffffff"))
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(menu));
                notificationManager.cancelAll();
                notificationManager.notify(0, notificationBuilder.build());

            }
            if ((hour >= 17 && minute >= 30) && (hour < 19 && select_id != 2)) {
                select_id = 2;
                menu = getStringArrayPref(2);
                title = "석식 메뉴";
                num = 2;
                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(MyService.this, CHANNEL_ID)
                        .setAutoCancel(false)
                        .setOngoing(true)
                        .setSmallIcon(drawables[num])
                        .setContentTitle(title)
                        .setContentText(menu)
                        .setSound(soundUri)
                        .setVibrate(new long[]{0, 3000})
                        .setContentIntent(pendingIntent)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setChannelId("my_notification")
                        .setColor(Color.parseColor("#ffffff"))
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(menu));
                notificationManager.cancelAll();
                notificationManager.notify(0, notificationBuilder.build());
            }

        }
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