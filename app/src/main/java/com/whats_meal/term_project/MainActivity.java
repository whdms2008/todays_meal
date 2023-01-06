package com.whats_meal.term_project;

import android.Manifest;
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
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends AppCompatActivity {

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

    String[][] restaurants = {{},
            {
                    "https://www.kongju.ac.kr/kongju/13157/subview.do", // 천안 학생식당
                    "https://www.kongju.ac.kr/kongju/13159/subview.do", // 예산 학생식당
                    "https://www.kongju.ac.kr/kongju/13155/subview.do"  // 신관 학생식당
            },
            {
                    "https://www.kongju.ac.kr/kongju/13158/subview.do", // 천안 직원식당
                    "https://www.kongju.ac.kr/kongju/13160/subview.do", // 예산 직원식당
                    "https://www.kongju.ac.kr/kongju/13156/subview.do"  // 신관 직원식당
            }
    };

    String[] food_time_type = {"td[data-mqtitle='breakfast']", "td[data-mqtitle='lunch']", "td[data-mqtitle='dinner']"};
    String[] dinner_type_name = {"기숙사 식당", "학생식당", "교직원식당"};
    String[][][] food_time = {{
            {"", "", ""},// 아침[천안[기식,학식,직원]]
            {"", "", ""},// 아침[예산[기식,학식,직원]]
            {"07:30 ~ 09:00", "", ""} // 아침[신관[기식,학식,직원]]
    }, {
            {"11:40 ~ 13:30", "11:30 ~ 13:30", "11:30 ~ 13:30"}, // 점심[천안[기식,학식,직원]]
            {"", "12:00 ~ 13:30", "12:00 ~ 13:30"}, // 점심[예산[기식,학식,직원]]
            {"11:30 ~ 13:30", "11:30 ~ 13:30", "11:30 ~ 13:30"}  // 점심[신관[기식,학식,직원]]
    }, {
            {"17:40 ~ 19:00", "", ""}, // 저녁[천안[기식,학식,직원]]
            {"", "17:40 ~ 19:00", ""}, // 저녁[예산[기식,학식,직원]]
            {"17:30 ~ 19:00", "", ""}} // 저녁[신관[기식,학식,직원]]
    };


    String[][] food_room_name = {{"천안"}, {"예산"}, {"은행사/비전", "드림"}};
    String[] food_time_name = {"조식", "중식", "석식"};
    final Bundle bundle = new Bundle();
    long now = System.currentTimeMillis();

    Date date = new Date(now);
    @SuppressLint("SimpleDateFormat")
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd:HH:mm");

    String getDate = sdf.format(date);
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
        pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);
        editor = pref.edit();

        nums = pref.getInt("select_time", 0);
        select_campus = pref.getInt("select_campus", 0);
        select_room = pref.getInt("select_restaurant", 0);
        select_food_room = pref.getInt("select_dorm_restaurant", 0);
        food_menus = pref.getString("food_menus", "");
        notify = pref.getBoolean("notify", false);
        alarm_food_name = pref.getString("alarm_food_name", "천안");
        alarm_food_type_name = pref.getString("alarm_food_type_name", "기숙사 식당");

        cam_name = findViewById(R.id.cam_name);
        diner_name = findViewById(R.id.diner_type);
        today_date = findViewById(R.id.date);
        food_type = findViewById(R.id.food_type);
        food_time_view = findViewById(R.id.food_time);
        food_menu = findViewById(R.id.food_menu);
        food_type_icon = findViewById(R.id.food_type_icon);

        dialog01 = new Dialog(MainActivity.this);
        dialog01.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog01.setContentView(R.layout.popup);
        WindowManager.LayoutParams params = dialog01.getWindow().getAttributes();
        params.width = LinearLayout.LayoutParams.MATCH_PARENT;
        params.height = LinearLayout.LayoutParams.WRAP_CONTENT;
        dialog01.getWindow().setAttributes(params);


        menu_dialog = new Dialog(MainActivity.this);
        menu_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        menu_dialog.setContentView(R.layout.setting);
        WindowManager.LayoutParams menu_params = menu_dialog.getWindow().getAttributes();
        menu_params.width = LinearLayout.LayoutParams.MATCH_PARENT;
        menu_params.height = LinearLayout.LayoutParams.WRAP_CONTENT;
        menu_dialog.getWindow().setAttributes(menu_params);


        notify_icon = findViewById(R.id.notification);
        notify_icon.setImageDrawable(
                notify ? getDrawable(R.drawable.true_notification) : getDrawable(R.drawable.false_notfication)
        );
        notify_icon.setOnClickListener(view -> {
            if (notify) {
                cancelAlarm();
                Toast.makeText(getApplicationContext(), "알람 꺼짐", Toast.LENGTH_SHORT).show();
            } else {
                setAlarm();
                Toast.makeText(getApplicationContext(), "알람 켜짐", Toast.LENGTH_SHORT).show();
            }
            editor.apply();
        });

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
                    sendMessageToHandler("0");
                    break;
                case R.id.sun:
                    sendMessageToHandler("1");
                    break;
                case R.id.moon:
                    sendMessageToHandler("2");
                    break;
                case R.id.campus:
                    sendMessageToHandler("3");
                    break;
                case R.id.menu:
                    sendMessageToHandler("4");
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

    private void sendMessageToHandler(String nums) {
        bundle.putString("numbers", nums);
        Message msg = handler.obtainMessage();
        msg.setData(bundle);
        handler.sendMessage(msg);
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

        List<PendingIntent> pendingIntents = new ArrayList<>();
        pendingIntents.add(PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT));
        pendingIntents.add(PendingIntent.getBroadcast(this, 1, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT));
        pendingIntents.add(PendingIntent.getBroadcast(this, 2, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT));

        for (PendingIntent pendingIntent : pendingIntents) {
            alarmManager.cancel(pendingIntent);
        }

    }

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Log.i("data", "허용!!");
                } else {
                    Log.i("data", "거절!!");
                }
            });

    @SuppressLint({"UseCompatLoadingForDrawables", "UnspecifiedImmutableFlag"})
    private void setAlarm() {
        if (Build.VERSION_CODES.TIRAMISU == Build.VERSION.SDK_INT) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
        notify = true;
        editor.putBoolean("notify", true);
        editor.apply();

        notify_icon.setImageDrawable(getDrawable(R.drawable.true_notification));
        calendar = Calendar.getInstance();
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        createNotificationChannel();
        int[][] time = {{}, {}, {}};
        for (int i = 0; i < 3; i++) {
            try {
                time[i] = Arrays.stream(food_time[i][select_campus][select_room].split(" ~ ")[0].split(":")).mapToInt(Integer::parseInt).toArray();
                makeNotification(time[i][0], time[i][1], i);
            } catch (NumberFormatException e) {
                time[i] = new int[]{0, 0};
            }
        }
    }


    @SuppressLint("UnspecifiedImmutableFlag")
    private void makeNotification(int hour, int minute, int putData) {
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("putData", putData);
        intent.putExtra("hour", hour);
        intent.putExtra("minute", minute);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        pendingIntent = PendingIntent.getBroadcast(this, putData, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }


    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "알림";
            String description = "알림입니다!";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("JoEun_Notification_Channel", name, importance);
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
                switch (nums) {
                    case 0:
                    case 1:
                    case 2:
                        food_type.setText(food_time_name[nums]);
                        editor.putInt("select_time", nums);
                        editor.apply();
                        load_food(nums);
                        break;
                    case 3:
                        popup(0);
                        break;
                    case 4:
                        popup(1);
                        break;
                }
            }).start();
        }
    };

    public static String getVersion(Context context) {
        String versionName = "";
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            versionName = pInfo.versionName + "";
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }

    @SuppressLint("SetTextI18n")
    private void popup(int num) {
        runOnUiThread(() -> {
            if (num == 0) { // 캠퍼스
                dialog01.show(); // 다이얼로그 띄우기
                dialog01.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                RadioGroup radioGroup = dialog01.findViewById(R.id.radio_group);
                Button campus_1 = dialog01.findViewById(R.id.radio_1);
                Button campus_2 = dialog01.findViewById(R.id.radio_2);
                Button campus_3 = dialog01.findViewById(R.id.radio_3);

                Button[] campus_list = {campus_1, campus_2, campus_3};

                today_date.setText(month + "월 " + day + "일");
                select_room = Integer.parseInt(String.valueOf(Temporary_food_type_num));
                editor.putInt("select_restaurant", select_room);


                // 식당 관련
                Button room_type_1 = dialog01.findViewById(R.id.select_room_1);
                Button room_type_2 = dialog01.findViewById(R.id.select_room_2);
                Button room_type_3 = dialog01.findViewById(R.id.select_room_3);


                Button[] room_types = {room_type_1, room_type_2, room_type_3};
                TextView room_show = dialog01.findViewById(R.id.food_rooms_show);
                RadioGroup food_rooms_view = dialog01.findViewById(R.id.food_rooms);
                RadioGroup select_room_view = dialog01.findViewById(R.id.select_rooom_rdg);
                // room_show : 기숙사 식당 선택 시 "기숙사 선택" 이라는 문자를 띄움
                // 기본 상태 invisibility

                // ==========캠퍼스 고르기 Temporary_num = 캠퍼스 종류 [ 0, 1, 2 ]===========

                radioGroup.setOnCheckedChangeListener((group, checkedId) -> {

                    RadioButton rdoButton = dialog01.findViewById(group.getCheckedRadioButtonId());
                    Log.i("data", String.valueOf(rdoButton.getText()));
                    switch (String.valueOf(rdoButton.getText())) {
                        case "예산":
                            Temporary_num.set(1);
                            break;
                        case "신관":
                            Temporary_num.set(2);
                            break;
                        default:
                            Temporary_num.set(0);
                            break;
                    }
                    room_types[Integer.parseInt(String.valueOf(Temporary_food_type_num))].performClick();
                });

                int first_chk = pref.getInt("select_campus", 0);
                int first_chk_rest = pref.getInt("select_restaurant", 0);
                int first_chk_dorm = pref.getInt("select_dorm_restaurant", 0);
                campus_list[first_chk].performClick();

                // ================= 식당 종류 선택 ====================
                // 임시 변수 : Temporary_foo_type_num

                select_room_view.setOnCheckedChangeListener((group, checkedId) -> {
                    RadioButton rdoButton = dialog01.findViewById(select_room_view.getCheckedRadioButtonId());
                    switch (String.valueOf(rdoButton.getText())) {
                        case "학생 식당":
                            Temporary_food_type_num.set(1);
                            break;
                        case "교직원 식당":
                            Temporary_food_type_num.set(2);
                            break;
                    }
                    room_show.setVisibility(View.GONE);
                    food_rooms_view.removeAllViews();
                });

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
                switch (first_chk_rest) {
                    case 0:
                        room_type_1.performClick();
                        try {
                            food_rooms_view.getChildAt(first_chk_dorm).performClick();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case 1:
                        room_type_2.performClick();
                        break;
                    case 2:
                        room_type_3.performClick();
                        break;
                }

                // ================= 기숙사 식당 선택 ==================

                dialog01.findViewById(R.id.noBtn).setOnClickListener(view -> dialog01.dismiss());
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

                    int time_all = (f_hour * 60) + f_minute;
                    if (19 * 60 - time_all >= 0) {
                        findViewById(R.id.moon).performClick();
                    } else if (13 * 60 + 30 - time_all >= 0) {
                        findViewById(R.id.sun).performClick();
                    } else if (9 * 60 - time_all >= 0) {
                        findViewById(R.id.sun_rise).performClick();
                    } else {
                        findViewById(R.id.sun_rise).performClick();
                    }
                    dialog01.dismiss(); // 다이얼로그 닫기
                });
            } else { // 메뉴
                menu_dialog.show(); // 다이얼로그 띄우기
                menu_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                TextView app_version = menu_dialog.findViewById(R.id.app_version);
                app_version.setText(getVersion(this));
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
                document = Jsoup.connect(select_room != 0 ? restaurants[select_room][select_campus] : campus[select_campus][select_food_room]).get();
                elements = document.select("tbody tr");
                if (select_room != 0) {
                    select = document.select("thead tr th").indexOf(document.select("th.on").first());
                    Elements elements2 = document.select("tbody");
                    for (Element e : elements2.select("tr")) {
                        switch (e.select("th").text()) {
                            case "중식":
                                today_menus[1] = e.child(select).text();
                                break;
                            case "석식":
                                today_menus[2] = e.child(select).text();
                                break;
                        }
                    }
                    setStringArrayPref(today_menus);
                } else {
                    List<String> list = Arrays.asList(elements.select("td[data-mqtitle='date']").text().replace(" ", "").split("일"));
                    select = list.indexOf(month + "월" + day);
                    try {

                        Element e = elements.get(select);

                        for (int cnt = 0; cnt < today_menus.length; cnt++) {
                            today_menus[cnt] = e.select(food_time_type[cnt]).text().replaceAll(",", " ").replaceAll(" {2}", " ");
                        }
                    } catch (ArrayIndexOutOfBoundsException e) {
                        Arrays.fill(today_menus, "");
                        e.printStackTrace();
                    }
                    setStringArrayPref(today_menus);
                }
            }
            runOnUiThread(() -> {
                food_type_icon.setImageDrawable(ResourcesCompat.getDrawable(getResources(), food_icon[numbers], null));
                food_menu.removeAllViews();
                food_time_view.setText(food_time[numbers][select_campus][select_room]);
                if (!today_menus[numbers].isEmpty() && !Objects.equals(today_menus[numbers], "등록된 식단내용이(가) 없습니다.")) {
                    List<String> menus = Arrays.asList(today_menus[numbers].split(" "));
                    menus.forEach(menu -> food_menu.addView(makeMenu(menu)));
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
        tv.setTypeface(getResources().getFont(R.font.scd5));
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT
                , LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        param.leftMargin = 20;
        param.rightMargin = 20;
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
        int color = ContextCompat.getColor(this, R.drawable.radio_text_selector);
        rdb.setTextColor(color);
        rdb.setGravity(Gravity.CENTER);
        rdb.setTypeface(getResources().getFont(R.font.scd5));
        rdb.setLayoutParams(new LinearLayout.LayoutParams(0, 150, 1));
        rdb.setOnClickListener(view -> Temporary_food_room_num.set(number));
        return rdb;
    }

    private void setStringArrayPref(String[] values) {
        String jsonString;
        if (values.length == 0) {
            jsonString = null;
        } else {
            jsonString = new JSONArray(Arrays.asList(values)).toString();
        }
        editor.putString("food_menus", jsonString).apply();
    }
}