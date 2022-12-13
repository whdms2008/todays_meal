package com.example.term_project;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.icu.text.SimpleDateFormat;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import com.example.term_project.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private Calendar calendar;
    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;
    SharedPreferences pref;
    SharedPreferences.Editor editor;

    AtomicInteger Temporary_num = new AtomicInteger();
    AtomicInteger Temporary_food_type_num = new AtomicInteger();
    AtomicInteger Temporary_food_room_num = new AtomicInteger();
    LinearLayout food_menu;
    String[][] campus = {
            {"https://dormi.kongju.ac.kr/HOME/sub.php?code=041303"}, // 천안 생활관 식당 [ 천안 ]
            {"https://dormi.kongju.ac.kr/HOME/sub.php?code=041304"}, // 예산 생활관 식당 [ X ]
            {"https://dormi.kongju.ac.kr/HOME/sub.php?code=041301",  // 신관 생활관 식당 [ 은행사/비전 ]
                    "https://dormi.kongju.ac.kr/HOME/sub.php?code=041302"}}; // 신관 생활관 식당 [ 드림 ]

    String[] today_menus = {"", "", ""}; // 조식메뉴, 중식, 석식
    String[] student_name = {"천안", "예산", "신관"};

    String[][] restaurants = {{}, {
            "https://www.kongju.ac.kr/kongju/13157/subview.do", // 천안 학생식당
            "https://www.kongju.ac.kr/kongju/13159/subview.do", // 예산 학생식당
            "https://www.kongju.ac.kr/kongju/13155/subview.do"},
            {
                    "https://www.kongju.ac.kr/kongju/13158/subview.do", // 천안 직원식당
                    "https://www.kongju.ac.kr/kongju/13160/subview.do", // 예산 직원식당
                    "https://www.kongju.ac.kr/kongju/13156/subview.do"}
    };

    // 천안, 예산, 은행사/비전, 드림
    String[] food_time_type = {"td[data-mqtitle='breakfast']", "td[data-mqtitle='lunch']", "td[data-mqtitle='dinner']"};
    String[] dinner_type_name = {"기숙사 식당", "학생식당", "교직원식당"};
    String[] food_time = {"07:30 ~ 09:00", "11:30 ~ 13:30", "17:30 ~ 19:00"};
    String[][] food_room_name = {{"천안"}, {"예산"}, {"은행사/비전", "드림"}};
    String[] menus;
    String[] food_time_name = {"조식", "중식", "석식"};
    final Bundle bundle = new Bundle();
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
    String alarm_food_name = "";
    String alarm_food_type_name = "";
    int select = 0;
    int nums = 0; // 조식, 중식, 석식
    int select_campus = 0; // 캠퍼스 종류
    int select_room = 0; // 식당 종류
    int select_food_room = 0; // 기숙사의 경우 식당 종류

    String food_menus = ""; // 기숙사 조식
    int[] food_icon = {R.drawable.sun_morning, R.drawable.breakfast, R.drawable.dinner_icon};

    BottomNavigationView bottomNavigationView;
    TextView cam_name, diner_name, today_date, food_type, food_time_view;
    ImageView food_type_icon, notify_icon;

    Dialog dialog01;
    Dialog menu_dialog;

    boolean notify = false;

    @SuppressLint({"UseCompatLoadingForDrawables", "SetTextI18n", "NonConstantResourceId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
//        createNotificationChannel();
        pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);
        editor = pref.edit();

        nums = pref.getInt("select_time", 0);
        select_campus = pref.getInt("select_campus", 0);
        select_room = pref.getInt("select_restaurant", 0);
        select_food_room = pref.getInt("select_dorm_restaurant", 0);
        food_menus = pref.getString("food_menus", "");
        notify = pref.getBoolean("notify", false);
        alarm_food_name = pref.getString("alarm_food_name", "");
        alarm_food_type_name = pref.getString("alarm_food_type_name", "");

        cam_name = findViewById(R.id.cam_name);
        diner_name = findViewById(R.id.diner_type);
        today_date = findViewById(R.id.date);
        food_type = findViewById(R.id.food_type);
        food_time_view = findViewById(R.id.food_time);
        food_menu = findViewById(R.id.food_menu);
        food_type_icon = findViewById(R.id.food_type_icon);

        dialog01 = new Dialog(MainActivity.this);       // Dialog 초기화
        dialog01.requestWindowFeature(Window.FEATURE_NO_TITLE); // 타이틀 제거
        dialog01.setContentView(R.layout.popup);
        WindowManager.LayoutParams params = dialog01.getWindow().getAttributes();
        params.width = LinearLayout.LayoutParams.MATCH_PARENT;
        params.height = LinearLayout.LayoutParams.WRAP_CONTENT;
        dialog01.getWindow().setAttributes(params);


        menu_dialog = new Dialog(MainActivity.this);       // Dialog 초기화
        menu_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // 타이틀 제거
        menu_dialog.setContentView(R.layout.setting);
        WindowManager.LayoutParams menu_params = menu_dialog.getWindow().getAttributes();
        menu_params.width = LinearLayout.LayoutParams.MATCH_PARENT;
        menu_params.height = LinearLayout.LayoutParams.WRAP_CONTENT;
        menu_dialog.getWindow().setAttributes(menu_params);


        notify_icon = findViewById(R.id.notification);

        notify_icon.setOnClickListener(view -> {
            if (notify) {
                cancelAlarm();
                Toast.makeText(getApplicationContext(), "알람 꺼짐", Toast.LENGTH_SHORT).show();
//                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//                notificationManager.cancelAll();
            } else {
                setAlarm();
                Toast.makeText(getApplicationContext(), "알람 켜짐", Toast.LENGTH_SHORT).show();
//                Intent serviceintent = new Intent(this, MyService.class);
//                startService(serviceintent);
            }
            editor.apply();
        });
        if (notify) {
            notify_icon.setImageDrawable(getDrawable(R.drawable.true_notification));
        } else {
            notify_icon.setImageDrawable(getDrawable(R.drawable.false_notfication));
        }


        bottomNavigationView = findViewById(R.id.menus);
        today_date.setText(month + "월 " + day + "일");

        diner_name.setText(dinner_type_name[select_room]);
        if (select_room == 0) {
            cam_name.setText(food_room_name[select_campus][select_food_room]);
        } else {
            cam_name.setText(student_name[select_campus]);
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.sun_rise:
                    bundle.putString("numbers", "0");                               //핸들러를 이용해서 Thread()에서 가져온 데이터를 메인 쓰레드에 보내준다.
                    Message msg = handler.obtainMessage();
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                    break;
                case R.id.sun:
                    bundle.putString("numbers", "1");                               //핸들러를 이용해서 Thread()에서 가져온 데이터를 메인 쓰레드에 보내준다.
                    Message msg2 = handler.obtainMessage();
                    msg2.setData(bundle);
                    handler.sendMessage(msg2);
                    break;
                case R.id.moon:
                    bundle.putString("numbers", "2");                               //핸들러를 이용해서 Thread()에서 가져온 데이터를 메인 쓰레드에 보내준다.
                    Message msg3 = handler.obtainMessage();
                    msg3.setData(bundle);
                    handler.sendMessage(msg3);
                    break;
                case R.id.campus:
                    bundle.putString("numbers", "3");                               //핸들러를 이용해서 Thread()에서 가져온 데이터를 메인 쓰레드에 보내준다.
                    Message msg4 = handler.obtainMessage();
                    msg4.setData(bundle);
                    handler.sendMessage(msg4);
                    break;
                case R.id.menu:
                    bundle.putString("numbers", "4");                               //핸들러를 이용해서 Thread()에서 가져온 데이터를 메인 쓰레드에 보내준다.
                    Message msg5 = handler.obtainMessage();
                    msg5.setData(bundle);
                    handler.sendMessage(msg5);
                    break;
            }
            return true;
        });
        if (nums == 0) {
            findViewById(R.id.sun_rise).performClick();
        } else if (nums == 1) {
            findViewById(R.id.sun).performClick();

        } else if (nums == 2) {
            findViewById(R.id.moon).performClick();

        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void cancelAlarm() {
        notify = false;
        editor.putBoolean("notify", false);
        editor.apply();
        notify_icon.setImageDrawable(getDrawable(R.drawable.false_notfication));

        Intent intent = new Intent(this, AlarmReceiver.class);


        if (alarmManager == null) {

            alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        }
        pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pendingIntent);

        pendingIntent = PendingIntent.getBroadcast(this, 1, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pendingIntent);

        pendingIntent = PendingIntent.getBroadcast(this, 2, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pendingIntent);

    }

    @SuppressLint({"UseCompatLoadingForDrawables", "UnspecifiedImmutableFlag"})
    private void setAlarm() {
        notify = true;
        editor.putBoolean("notify", true);
        editor.apply();
        notify_icon.setImageDrawable(getDrawable(R.drawable.true_notification));
        calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 17);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

//        Intent intent = new Intent(this, AlarmReceiver.class);
        createNotificationChannel("0");

        makeNotification(7,30,0, 0);

        makeNotification(11,30,1, 1);
//
        makeNotification(17,30,2, 2);

    }

    private void makeNotification(int hour, int minute, int requestCode,int putData){
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("putData",putData);
        intent.putExtra("requestCode", requestCode);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        pendingIntent = PendingIntent.getBroadcast(this, requestCode, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT  );
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),AlarmManager.INTERVAL_DAY,pendingIntent);
//        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),pendingIntent);
    }


    private void createNotificationChannel(String id) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "알림";
            String description = "알림입니다!";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(id, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

        }


    }

    Handler handler = new Handler(Looper.getMainLooper()) {
        @SuppressLint({"SetTextI18n", "UseCompatLoadingForDrawables"})
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            nums = Integer.parseInt(bundle.getString("numbers"));
            new Thread(() -> {
                if (nums == 0 || nums == 1 || nums == 2) {
                    food_type.setText(food_time_name[nums]);
                    editor.putInt("select_time", nums);
                    editor.apply();
                    load_food(nums);
                } else if (nums == 3) {
                    popup(0);
                } else if (nums == 4) {
                    popup(1);

                }
            }).start();
        }
    };


    @SuppressLint("SetTextI18n")
    private void popup(int num) {
        runOnUiThread(() -> {
            if (num == 0) { // 캠퍼스
                dialog01.show(); // 다이얼로그 띄우기
                dialog01.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                Button campus_1 = dialog01.findViewById(R.id.radio_1);
                Button campus_2 = dialog01.findViewById(R.id.radio_2);
                Button campus_3 = dialog01.findViewById(R.id.radio_3);
                today_date.setText(month + "월 " + day + "일");
                select_room = Integer.parseInt(String.valueOf(Temporary_food_type_num));
                editor.putInt("select_restaurant", select_room);


                Button room_type_1 = dialog01.findViewById(R.id.select_room_1);
                Button room_type_2 = dialog01.findViewById(R.id.select_room_2);
                Button room_type_3 = dialog01.findViewById(R.id.select_room_3);
                // 날짜 오류

                TextView room_show = dialog01.findViewById(R.id.food_rooms_show);
                RadioGroup food_rooms_view = dialog01.findViewById(R.id.food_rooms);
                RadioGroup select_room_view = dialog01.findViewById(R.id.select_rooom_rdg);
                // room_show : 기숙사 식당 선택 시 "기숙사 선택" 이라는 문자를 띄움
                // 기본 상태 invisibility

                // ==========캠퍼스 고르기 Temporary_num = 캠퍼스 종류 [ 0, 1, 2 ]===========

                campus_1.setOnClickListener(view -> {
                    Temporary_num.set(0);
                    if (Integer.parseInt(String.valueOf(Temporary_food_type_num)) == 0) {
                        room_type_1.performClick();
                    } else if (Integer.parseInt(String.valueOf(Temporary_food_type_num)) == 1) {
                        room_type_2.performClick();
                    } else {
                        room_type_3.performClick();
                    }
                });
                campus_2.setOnClickListener(view -> {
                    Temporary_num.set(1);
                    if (Integer.parseInt(String.valueOf(Temporary_food_type_num)) == 0) {
                        room_type_1.performClick();
                    } else if (Integer.parseInt(String.valueOf(Temporary_food_type_num)) == 1) {
                        room_type_2.performClick();
                    } else {
                        room_type_3.performClick();
                    }
                });
                campus_3.setOnClickListener(view -> {
                    Temporary_num.set(2);
                    if (Integer.parseInt(String.valueOf(Temporary_food_type_num)) == 0) {
                        room_type_1.performClick();
                    } else if (Integer.parseInt(String.valueOf(Temporary_food_type_num)) == 1) {
                        room_type_2.performClick();
                    } else {
                        room_type_3.performClick();
                    }
                });
                int first_chk = pref.getInt("select_campus", 0);
                int first_chk_rest = pref.getInt("select_restaurant", 0);
                int first_chk_dorm = pref.getInt("select_dorm_restaurant", 0);
                if (first_chk == 0) {
                    campus_1.performClick();
                } else if (first_chk == 1) {

                    campus_2.performClick();
                } else {
                    campus_3.performClick();
                }

                // ================= 식당 종류 선택 ====================
                // 임시 변수 : Temporary_foo_type_num
                room_type_1.setOnClickListener(view -> {
                    food_rooms_view.removeAllViews();
                    Temporary_food_room_num.set(0);
                    Temporary_food_type_num.set(0);
                    int food_room_num = Integer.parseInt(String.valueOf(Temporary_num));
                    String[] food_room_len = food_room_name[food_room_num];
                    if (Objects.equals(food_room_len[0], "예산")) {
                        Temporary_food_type_num.set(1);
                        select_room_view.check(R.id.select_room_2);
                        room_show.setVisibility(View.GONE);
                        Toast.makeText(getApplicationContext(), "기숙사가 존재하지 않습니다.", Toast.LENGTH_LONG).show();
                    } else {
                        room_show.setVisibility(View.VISIBLE);
                        for (int i = 0; i < food_room_len.length; i++) {
                            food_rooms_view.addView(makeRadio(food_room_name[food_room_num][i], i));
                        }
                    }
                });
                if (first_chk_rest == 0) {
                    room_type_1.performClick();
                    food_rooms_view.getChildAt(first_chk_dorm).performClick();
                } else if (first_chk_rest == 1) {

                    room_type_2.performClick();
                } else {
                    room_type_3.performClick();
                }
                room_type_2.setOnClickListener(view -> {
                    Temporary_food_type_num.set(1);
                    room_show.setVisibility(View.GONE);
                    food_rooms_view.removeAllViews();
                });
                room_type_3.setOnClickListener(view -> {
                    Temporary_food_type_num.set(2);
                    room_show.setVisibility(View.GONE);
                    food_rooms_view.removeAllViews();
                });
                // ================= 기숙사 식당 선택 ==================

                dialog01.findViewById(R.id.noBtn).setOnClickListener(view -> {
                    dialog01.dismiss();
                });
                dialog01.findViewById(R.id.yesBtn).setOnClickListener(view -> {
                    select_campus = Integer.parseInt(String.valueOf(Temporary_num));
                    editor.putInt("select_campus", select_campus);
                    editor.apply();
                    today_date.setText(month + "월 " + day + "일");
                    select_room = Integer.parseInt(String.valueOf(Temporary_food_type_num));
                    editor.putInt("select_restaurant", select_room);
                    editor.apply();
                    diner_name.setText(dinner_type_name[select_room]);
                    editor.putString("alarm_food_type_name", dinner_type_name[select_room]);
                    if (select_room == 0) {
                        select_food_room = Integer.parseInt(String.valueOf(Temporary_food_room_num));

                        editor.putInt("select_dorm_restaurant", select_food_room);
                        editor.apply();
                        cam_name.setText(food_room_name[select_campus][select_food_room]);
                        editor.putString("alarm_food_name", food_room_name[select_campus][select_food_room]);
                    } else {
                        cam_name.setText(student_name[select_campus]);
                        editor.putString("alarm_food_name", student_name[select_campus]);
                    }
                    editor.apply();
                    int f_hour = Integer.parseInt(hour);
                    int f_minute = Integer.parseInt(minute);
                    today_menus[0] = "";
                    today_menus[1] = "";
                    today_menus[2] = "";
                    if (f_hour >= 13 && f_minute >= 30) {
                        findViewById(R.id.moon).performClick();
                        dialog01.dismiss(); // 다이얼로그 닫기
                    } else if (f_hour >= 9) {

                        findViewById(R.id.sun).performClick();
                        dialog01.dismiss(); // 다이얼로그 닫기
                    } else if (f_hour > 1) {
                        findViewById(R.id.sun_rise).performClick();
                        dialog01.dismiss(); // 다이얼로그 닫기
                    }
                    dialog01.dismiss(); // 다이얼로그 닫기
                });
            } else { // 메뉴
                menu_dialog.show(); // 다이얼로그 띄우기
                menu_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


                menu_dialog.findViewById(R.id.confirm_btn).setOnClickListener(view -> {
                    menu_dialog.dismiss(); // 다이얼로그 닫기
                });
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void load_food(int numbers) {
        try {
            Document document;
            Elements elements;
            if ((Objects.equals(today_menus[0], "") && Objects.equals(today_menus[1], "")) && Objects.equals(today_menus[2], "")) {
                if (select_room != 0) {
                    document = Jsoup.connect(restaurants[select_room][select_campus]).get();
                } else {
                    document = Jsoup.connect(campus[select_campus][select_food_room]).get();
                }
                elements = document.select("tbody tr"); //필요한 녀석만 꼬집어서 지정
                int i = 0;

                if (select_room != 0) {
//                    System.out.println(year +":"+ month+":"+day +","+select +", "+i);
//                    System.out.println(document.select("th.on").text());
//                    System.out.println(document.select("thead").select("tr").select("th").size());
//                    System.out.println(document.select("thead").select("tr").select("th").indexOf());
                    for (Element a : document.select("thead tr th")) {
                        if (Objects.equals(a.text(), document.select("th.on").text())) {
                            System.out.println(year +":"+ month+":"+day +","+select +", "+i);
                            select = i;
                            break;
                        }
                        i += 1;
                    }
                    System.out.println(year +":"+ month+":"+day +","+select+", "+select_room );
                    Elements elements2 = document.select("tbody");
                    for (Element e: elements2.select("tr")) {
                        System.out.println(e.child(0));
                        if(Objects.equals(e.select("th").text(), "중식")){
                            today_menus[1] = e.child(select).text();
                            System.out.println(e.child(select));
                        }
                        if(Objects.equals(e.select("th").text(), "석식")){
                            today_menus[2] = e.child(select).text();
                            System.out.println(e.child(select));
                        }
                    }
                    setStringArrayPref(today_menus);
                } else {
                    for (Element a : elements.select("td[data-mqtitle='date']")) {
                        if (Objects.equals(a.text(), month + "월 " + day + "일")) {
                            select = i;
                            break;
                        }
                        i += 1;
                    }
                    Element e = elements.get(select);
                    today_menus[0] = e.select(food_time_type[0]).text().replaceAll(",", " ").replaceAll(" {2}", " ");
                    today_menus[1] = e.select(food_time_type[1]).text().replaceAll(",", " ").replaceAll(" {2}", " ");
                    today_menus[2] = e.select(food_time_type[2]).text().replaceAll(",", " ").replaceAll(" {2}", " ");
                    setStringArrayPref(today_menus);
                }
            }
            runOnUiThread(() -> {
                food_type_icon.setImageDrawable(ResourcesCompat.getDrawable(getResources(), food_icon[numbers], null));
                food_menu.removeAllViews();
                food_time_view.setText(food_time[numbers]);
                if (today_menus[numbers].length() != 0 && !Objects.equals(today_menus[numbers], "등록된 식단내용이(가) 없습니다.")) {
                    menus = today_menus[numbers].split(" ");
                    for (String menu : menus) {
                        food_menu.addView(makeMenu(menu));
                    }
                } else {
                    food_menu.addView(makeMenu("밥 없어요~!!"));
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @NonNull
    private TextView makeMenu(String content) {
        TextView tv = new TextView(this);
        tv.setText(content);
        tv.setTextColor(Color.parseColor("#000000"));
        tv.setGravity(Gravity.CENTER);
        tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        tv.setTextSize(2, 25);
        tv.setIncludeFontPadding(false);
        Typeface typeface = getResources().getFont(R.font.scd5);
        tv.setTypeface(typeface);
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT
                , LinearLayout.LayoutParams.WRAP_CONTENT);
        param.leftMargin = 20;
        param.rightMargin = 20;
        param.weight = 1;
        tv.setLayoutParams(param);
        return tv;
    }

    @NonNull
    @SuppressLint({"ResourceType", "UseCompatLoadingForColorStateLists", "UseCompatLoadingForDrawables"})
    private RadioButton makeRadio(String content, int number) {
        RadioButton rdb = new RadioButton(this);
        rdb.setText(content);
        rdb.setButtonDrawable(getDrawable(R.drawable.radio_selector));
        rdb.setBackground(getDrawable(R.drawable.radio_selector));
        rdb.setTextColor(getResources().getColorStateList(R.drawable.radio_text_selector));
        rdb.setGravity(Gravity.CENTER);
        Typeface typeface = getResources().getFont(R.font.scd5);
        rdb.setTypeface(typeface);
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT
                , LinearLayout.LayoutParams.WRAP_CONTENT);
        rdb.setLayoutParams(param);
        param.weight = (float) 1;
        rdb.setWidth(0);
        rdb.setHeight(150);
        rdb.setOnClickListener(view -> {
            Temporary_food_room_num.set(number);
        });
        return rdb;
    }

    private void setStringArrayPref(String[] values) {
        JSONArray a = new JSONArray();
        for (String value : values) {
            a.put(value);
        }
        if (values.length != 0) {
            editor.putString("food_menus", a.toString());
        } else {
            editor.putString("food_menus", null);
        }
        editor.apply();
    }
}