package com.whats_meal.term_project;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.content.SharedPreferences;
import android.icu.text.SimpleDateFormat;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.whats_meal.term_project.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class AlarmReceiver extends BroadcastReceiver {
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    String CHANNEL_ID = "0";
    String menu = "";
    int[] drawables = {R.drawable.sun_morning, R.drawable.breakfast, R.drawable.dinner_icon};
    int select_id = 0;
    String[][] restaurants = {{}, {
            "https://www.kongju.ac.kr/kongju/13157/subview.do", // 천안 학생식당
            "https://www.kongju.ac.kr/kongju/13159/subview.do", // 예산 학생식당
            "https://www.kongju.ac.kr/kongju/13155/subview.do"},
            {
                    "https://www.kongju.ac.kr/kongju/13158/subview.do", // 천안 직원식당
                    "https://www.kongju.ac.kr/kongju/13160/subview.do", // 예산 직원식당
                    "https://www.kongju.ac.kr/kongju/13156/subview.do"}
    };
    String[][] campus = {
            {"https://dormi.kongju.ac.kr/HOME/sub.php?code=041303"}, // 천안 생활관 식당 [ 천안 ]
            {"https://dormi.kongju.ac.kr/HOME/sub.php?code=041304"}, // 예산 생활관 식당 [ X ]
            {"https://dormi.kongju.ac.kr/HOME/sub.php?code=041301",  // 신관 생활관 식당 [ 은행사/비전 ]
                    "https://dormi.kongju.ac.kr/HOME/sub.php?code=041302"}}; // 신관 생활관 식당 [ 드림 ]
    String[] title = {"조식 메뉴", "중식 메뉴", "석식 메뉴"};

    int select = 0;
    int requestCode = 0;
    long now = System.currentTimeMillis();

    Date date = new Date(now);
    @SuppressLint("SimpleDateFormat")
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd:HH:mm");

    String getDate = sdf.format(date);
    String month = getDate.split(":")[1];
    String day = getDate.split(":")[2];
    Document document;
    Elements elements;
    AlarmManager alarmManager;
    Calendar calendar;
    String[] food_time_type = {"td[data-mqtitle='breakfast']", "td[data-mqtitle='lunch']", "td[data-mqtitle='dinner']"};

    @SuppressLint("UnspecifiedImmutableFlag")
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("data", "알람 실행됨!"+ intent.getIntExtra("putDATA",0)+", 현재시간:" + Arrays.toString(getDate.split(":")));
        pref = context.getSharedPreferences("pref", Activity.MODE_PRIVATE);
        editor = pref.edit();
        if (!pref.getBoolean("notify", false)) {
            return;
        }
        calendar = Calendar.getInstance();
        select_id = intent.getIntExtra("putData", 0);
        intent = new Intent(context, MainActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        editor.putInt("select_time", select_id);
        editor.apply();

        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, requestCode, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        Notification(pendingIntent, context);

    }

    private void Notification(PendingIntent pendingIntent, Context context) {
        int select_room = pref.getInt("select_restaurant", 0);
        int select_campus = pref.getInt("select_campus", 0);
        int select_food_room = pref.getInt("select_dorm_restaurant", 0);
        new Thread(() -> {
            if (select_room != 0) {
                try {
                    document = Jsoup.connect(restaurants[select_room][select_campus]).get();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    document = Jsoup.connect(campus[select_campus][select_food_room]).get();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            elements = document.select("tbody").select("tr"); //필요한 녀석만 꼬집어서 지정
            if (select_room != 0) {
                select = document.select("thead tr th").indexOf(document.select("th.on").first());

                menu = "";
                String[] meal_times = {"조식", "중식", "석식"};
                for (int e_cnt = 0; e_cnt < elements.size(); e_cnt++) {
                    String title = elements.get(e_cnt).select("th").text();
                    if (Objects.equals(title, meal_times[select_id])) {
                        menu = elements.get(e_cnt).select("td").get(select).text();
                        break;
                    }
                }
            } else {
                List<String> list = Arrays.asList(elements.select("td[data-mqtitle='date']").text().replace(" ", "").split("일"));
                select = list.indexOf(month + "월" + day);
                Element e = elements.get(select);
                menu = e.select(food_time_type[select_id]).text().replaceAll(",", " ").replaceAll(" {2}", " ");
            }
            String rest_name = pref.getString("alarm_food_name", "천안");
            String rest_type_name = pref.getString("alarm_food_type_name", "기숙사 식당");
            if (menu.equals("")) {
                menu = "오늘 밥 없습니다";
            } else {
                menu = menu.replace(" ", "\n");
            }
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setAutoCancel(false)
                    .setOngoing(false)
                    .setSmallIcon(drawables[select_id])
                    .setContentTitle("[ " + rest_type_name + " | " + rest_name + " ] - " + title[select_id])
                    .setContentText(menu)
                    .setVibrate(new long[]{0, 3000})
                    .setContentIntent(pendingIntent)
                    .setDefaults(android.app.Notification.DEFAULT_ALL)
                    .setChannelId(CHANNEL_ID)
                    .setColor(Color.parseColor("#ffffff"))
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(menu));

            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);

            notificationManagerCompat.notify(0, notificationBuilder.build());
        }).start();
    }
}