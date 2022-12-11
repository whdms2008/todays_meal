package com.example.term_project;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.content.SharedPreferences;
import android.icu.text.SimpleDateFormat;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class AlarmReceiver extends BroadcastReceiver {
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    NotificationManager notificationManager;
    String CHANNEL_ID = "0";
    String menu = "";
    int[] breakfast = {450, 540};
    int[] lunch = {690, 810};
    int[] dinner = {1050, 1140};
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
    long now = System.currentTimeMillis();

    Date date = new Date(now);
    @SuppressLint("SimpleDateFormat")
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd:HH:mm");

    String getDate = sdf.format(date);
    String year = getDate.split(":")[0];
    String month = getDate.split(":")[1];
    String day = getDate.split(":")[2];
    String hour = getDate.split(":")[3];
    String minute = getDate.split(":")[4];
    Document document;
    Elements elements;

    String[] food_time_type = {"td[data-mqtitle='breakfast']", "td[data-mqtitle='lunch']", "td[data-mqtitle='dinner']"};
    @Override
    public void onReceive(Context context, Intent intent) {
        pref = context.getSharedPreferences("pref", Activity.MODE_PRIVATE);
        if (!pref.getBoolean("notify", false)) {
            return;
        }
        Calendar cal1 = Calendar.getInstance();
        int hour = cal1.get(Calendar.HOUR_OF_DAY);
        int minute = cal1.get(Calendar.MINUTE);
        int times = (hour * 60) + minute;
        if (breakfast[0] <= times && times <= breakfast[1]) {
            select_id = 0;
        } else if (lunch[0] <= times && times <= lunch[1]) {
            select_id = 1;
        } else if (dinner[0] <= times && times <= dinner[1]) {
            select_id = 2;
        }else{
            select_id = 0;
        }
        Intent i = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

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

            int i = 0;

            if (select_room != 0) {
                for (Element a : document.select("thead").select("tr").select("span")) {
                    if (Objects.equals(a.text(), year + "." + month + "." + day)) {
                        select = i;
                        break;
                    }
                    i += 1;
                }

//                menu = elements.get(select_id).select("td").get(select).text();
                menu = "";
                for (int e_cnt = 0; e_cnt < elements.size(); e_cnt++) {

                    String title;
                    title = elements.get(e_cnt).select("th").text();
                    if (Objects.equals(title, "조식") && select_id == 0) {
                        menu = elements.get(e_cnt).select("td").get(select).text();
                    }
                    if (Objects.equals(title, "중식") && select_id == 1) {
                        menu = elements.get(e_cnt).select("td").get(select).text();
                    }
                    if (Objects.equals(title, "석식") && select_id == 2){
                        menu = elements.get(e_cnt).select("td").get(select).text();
                    }
                    System.out.println("e_cnt : "+e_cnt +", "+ title + ", " + menu);
                    }
            } else {
                for (Element a : elements.select("td[data-mqtitle='date']")) {
                    if (Objects.equals(a.text(), month + "월 " + day + "일")) {
                        select = i;
                        break;
                    }
                    i += 1;
                }
                Element e = elements.get(select);
                if (select_id == 0){
                    menu = e.select(food_time_type[0]).text().replaceAll(",", " ").replaceAll(" {2}", " ");
                }else if(select_id == 1){

                    menu= e.select(food_time_type[1]).text().replaceAll(",", " ").replaceAll(" {2}", " ");
                }else{
                    menu = e.select(food_time_type[2]).text().replaceAll(",", " ").replaceAll(" {2}", " ");
                }
            }
        String rest_name = pref.getString("alarm_food_name", "");
        String rest_type_name = pref.getString("alarm_food_type_name", "");
        if (menu.equals("")) {
            menu = "오늘 밥 없습니다";
        } else {
            menu = menu.replace(" ", "\n");
        }
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setAutoCancel(false)
                .setOngoing(true)
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


