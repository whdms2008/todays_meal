package com.example.term_project;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends AppCompatActivity {
    int nums = 0;
    int select_campus = 0;
    int select_room = 0;
    int select_food_room = 0;
    AtomicInteger Temporary_num = new AtomicInteger();
    AtomicInteger Temporary_food_type_num = new AtomicInteger();
    AtomicInteger Temporary_food_room_num = new AtomicInteger();
    LinearLayout food_menu;
    String[][] campus = {
            {"https://dormi.kongju.ac.kr/HOME/sub.php?code=041303"}, // 천안 생활관 식당 [ 천안 ]
            {"https://dormi.kongju.ac.kr/HOME/sub.php?code=041304"}, // 예산 생활관 식당 [ X ]
            {"https://dormi.kongju.ac.kr/HOME/sub.php?code=041301",  // 신관 생활관 식당 [ 은행사/비전 ]
                    "https://dormi.kongju.ac.kr/HOME/sub.php?code=041302"}}; // 신관 생활관 식당 [ 드림 ]
    String[][] datas = {
                    {"https://dormi.kongju.ac.kr/HOME/sub.php?code=041303", // 천안 생활관 식당 [ 천안 ]
                    "https://dormi.kongju.ac.kr/HOME/sub.php?code=041304", // 예산 생활관 식당 [ X ]
                    "https://dormi.kongju.ac.kr/HOME/sub.php?code=041301",  // 신관 생활관 식당 [ 은행사/비전 ]
                      "https://dormi.kongju.ac.kr/HOME/sub.php?code=041302"}, // 신관 생활관 식당 [ 드림 ]},

                    {"https://www.kongju.ac.kr/kongju/13157/subview.do", // 천안 학생식
                            "https://www.kongju.ac.kr/kongju/13159/subview.do", // 예산 학생식당
                            "https://www.kongju.ac.kr/kongju/13155/subview.do"},

                    {"https://www.kongju.ac.kr/kongju/13158/subview.do", // 천안 직원식당
                            "https://www.kongju.ac.kr/kongju/13160/subview.do", // 예산 직원식당
                            "https://www.kongju.ac.kr/kongju/13156/subview.do"}
    };

    // datas[식당 종류][캠퍼스]
    // datas[0] = 생활관
    // datas[0][0] = 천안 생활관
    // datas[0][1] = 예산 생활관
    // datas[0][2] = 신관 생활관
    // datas[0][3] = 신관 생활관

    // datas[1] = 학생식당
    // datas[1][0] = 천안 학생식당
    // datas[1][1] = 예산 학생식당
    // datas[1][2] = 신관 학생식당

    // datas[2] = 직원식당
    // datas[2][0] = 천안 직원식당
    // datas[2][1] = 예산 직원식당
    // datas[2][2] = 신관 직원식당

    String[] student_restaurant = {
            "https://www.kongju.ac.kr/kongju/13157/subview.do", // 천안 학생식당
            "https://www.kongju.ac.kr/kongju/13159/subview.do", // 예산 학생식당
            "https://www.kongju.ac.kr/kongju/13155/subview.do"}; //신관 학생식당

    String[] staff_restaurant = {
            "https://www.kongju.ac.kr/kongju/13158/subview.do", // 천안 직원식당
            "https://www.kongju.ac.kr/kongju/13160/subview.do", // 예산 직원식당
            "https://www.kongju.ac.kr/kongju/13156/subview.do"};// 신관 직원식당


    //https://www.kongju.ac.kr/kongju/13155/subview.do 신관 학생식당
    //https://www.kongju.ac.kr/kongju/13156/subview.do 식관 직원식당
    //https://www.kongju.ac.kr/kongju/13157/subview.do 천안 학생식당
    //https://www.kongju.ac.kr/kongju/13158/subview.do 천안 직원식당
    //https://www.kongju.ac.kr/kongju/13163/subview.do 천안 생활관 식당
    //https://www.kongju.ac.kr/kongju/13159/subview.do 예산 학생 식당
    //https://www.kongju.ac.kr/kongju/13160/subview.do 예산 직원 식당
    //https://dormi.kongju.ac.kr/HOME/sub.php?code=041301  신관 생활관 식당 [ 은행사/비전 ]
    //https://dormi.kongju.ac.kr/HOME/sub.php?code=041302  신관 생활관 식당 [ 드림 ]

    // 천안, 예산, 은행사/비전, 드림
    String[] food_time_type = {"td[data-mqtitle='breakfast']", "td[data-mqtitle='lunch']", "td[data-mqtitle='dinner']"};
    String[] campus_food_name = {"천안", "예산", "은행사/비전", "드림"};
    String[] dinner_type_name = {"기숙사 식당", "학생식당", "교직원식당"};
    String[] food_time = {"07:30 ~ 09:00", "11:30 ~ 13:30", "17:30 ~ 19:00"};
    String[][] food_room_name = {{"천안"}, {""}, {"은행사/비전", "드림"}};
    final Bundle bundle = new Bundle();

    long now = System.currentTimeMillis();

    Date date = new Date(now);
    @SuppressLint("SimpleDateFormat")
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd");
    String getDate = sdf.format(date);
    String year = getDate.split(":")[0];
    String month = getDate.split(":")[1];
    String day = getDate.split(":")[2];

    String[] menus;
    String foods;
    int select = 0;
    BottomNavigationView bottomNavigationView;
    TextView cam_name, diner_name, today_date, food_type, food_time_view;
    ImageView food_type_icon;
    int[] food_icon = {R.drawable.sunrise_icon2, R.drawable.breakefast_icon, R.drawable.dinner_icon};
    Dialog dialog01;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //LayoutInflater 객체 생성
        // datas[0] = 기숙사
        // datas[0][0][0] = 천안 기숙사
        // datas[0][1][0] = 예산 기숙사
        // datas[0][2][0] = 신관 기숙사 [ 은행사/비전 ]
        // datas[0][2][1] = 신관 기숙사 [ 드림 ]
        
        // datas[1] = 학생식당
        // datas[1][0][0] = 천안 학생식당
        // datas[1][1][0] = 예산 학생식당
        // datas[1][2][0] = 신관 학생식당
        
        // datas[2] = 직원식당
        // datas[2][0][0] = 천안 직원식당
        // datas[2][1][0] = 예산 직원식당
        // datas[2][2][0] = 신관 직원식당
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.popup, null);


        View decorView = getWindow().getDecorView();
        AtomicInteger Temporary_num = new AtomicInteger();
        cam_name = findViewById(R.id.cam_name);
        diner_name = findViewById(R.id.diner_type);
        today_date = findViewById(R.id.date);
        food_type = findViewById(R.id.food_type);
        food_time_view = findViewById(R.id.food_time);
        food_menu = findViewById(R.id.food_menu);
        food_type_icon = findViewById(R.id.food_type_icon);
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);


        dialog01 = new Dialog(MainActivity.this);       // Dialog 초기화
        dialog01.requestWindowFeature(Window.FEATURE_NO_TITLE); // 타이틀 제거
        dialog01.setContentView(R.layout.popup);
        WindowManager.LayoutParams params = dialog01.getWindow().getAttributes();
        params.width = LinearLayout.LayoutParams.MATCH_PARENT;
        params.height = LinearLayout.LayoutParams.WRAP_CONTENT;
        dialog01.getWindow().setAttributes(params);

        bottomNavigationView = findViewById(R.id.menus);
        food_type.setText("조식");
        bundle.putString("numbers", "0");                               //핸들러를 이용해서 Thread()에서 가져온 데이터를 메인 쓰레드에 보내준다.
        Message start = handler.obtainMessage();
        start.setData(bundle);
        handler.sendMessage(start);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.sun_rise:
                    food_type.setText("조식");
                    bundle.putString("numbers", "0");                               //핸들러를 이용해서 Thread()에서 가져온 데이터를 메인 쓰레드에 보내준다.
                    Message msg = handler.obtainMessage();
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                    break;
                case R.id.sun:
                    food_type.setText("중식");
                    bundle.putString("numbers", "1");                               //핸들러를 이용해서 Thread()에서 가져온 데이터를 메인 쓰레드에 보내준다.
                    Message msg2 = handler.obtainMessage();
                    msg2.setData(bundle);
                    handler.sendMessage(msg2);
                    break;
                case R.id.moon:
                    food_type.setText("석식");
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

    }

    Handler handler = new Handler(Looper.getMainLooper()) {
        @SuppressLint({"SetTextI18n", "UseCompatLoadingForDrawables"})
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            nums = Integer.parseInt(bundle.getString("numbers"));
            new Thread(() -> {
                if (nums == 0 || nums == 1 || nums == 2) {
                    load_food(nums);
                } else if (nums == 3) {
                    popup();
                } else if (nums == 4) {

                }
            }).start();
        }
    };


    private void popup() {
        runOnUiThread(() -> {
            dialog01.show(); // 다이얼로그 띄우기
            dialog01.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            Button campus_1 = dialog01.findViewById(R.id.radio_1);
            Button campus_2 = dialog01.findViewById(R.id.radio_2);
            Button campus_3 = dialog01.findViewById(R.id.radio_3);


            Button room_type_1 = dialog01.findViewById(R.id.select_room_1);
            Button room_type_2 = dialog01.findViewById(R.id.select_room_2);
            Button room_type_3 = dialog01.findViewById(R.id.select_room_3);


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

            // ================= 식당 종류 선택 ====================
            // 임시 변수 : Temporary_foo_type_num
            room_type_1.setOnClickListener(view -> {
                food_rooms_view.removeAllViews();
                Temporary_food_type_num.set(0);
                int food_room_num = Integer.parseInt(String.valueOf(Temporary_num));
                String[] food_room_len = food_room_name[food_room_num];
                System.out.println(food_room_num + ", " + food_room_len[0]);
                if (Objects.equals(food_room_len[0], "")) {
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


            dialog01.findViewById(R.id.yesBtn).setOnClickListener(view -> {
                // select_room : 기숙사식당 ,학생식당, 교직원식당 구분하는 변수
                // select_campus : 어디 캠퍼스인지
                //
                select_campus = Integer.parseInt(String.valueOf(Temporary_num));
                select_room = Integer.parseInt(String.valueOf(Temporary_food_type_num));
                select_food_room = Integer.parseInt(String.valueOf(Temporary_food_room_num));
                cam_name.setText(food_room_name[select_campus][select_food_room]);

                dialog01.dismiss(); // 다이얼로그 닫기
            });
        });
    }

    private void load_food(int nums) {
        try {
            Document document;
            Elements elements;
            System.out.println(select_campus + ", " + select_room);
            String[] web_call =
                    {"tbody", "tr", "td[data-mqtitle='date']", food_time_type[nums]};
            if (select_room == 0) {
                System.out.println("0번 [ 기숙사 임 ]");
                document = Jsoup.connect(campus[select_campus][select_food_room]).get();
                elements = document.select(web_call[0]).select(web_call[1]); //필요한 녀석만 꼬집어서 지정
                int i = 0;
                for (Element a : elements.select(web_call[2])) {
                    if (Objects.equals(a.text(), month + "월 " + day + "일")) {
                        select = i;
                        break;
                    }
                    i += 1;
                }
                food_type_icon.setImageDrawable(ResourcesCompat.getDrawable(getResources(), food_icon[nums], null));
                Element e = elements.get(select);
                String date = e.select("td[data-mqtitle='date']").text();
                runOnUiThread(() -> {
                    food_menu.removeAllViews();
                    cam_name.setText(campus_food_name[select_campus]);
                    diner_name.setText(dinner_type_name[select_room]);
                    foods = e.select(food_time_type[nums]).text();
                    today_date.setText(date);
                    food_time_view.setText(food_time[nums]);
                    if (foods.length() != 0) {
                        menus = foods.split(" ");
                        for (String menu : menus) {
                            food_menu.addView(makeMenu(menu));
                        }
                    } else {
                        food_menu.addView(makeMenu("밥 없어요~!!"));
                    }
                });
            } else if (select_room == 1){
                String[] times = {"","tr[class='_even']","tr[class='_odd']"};
                System.out.println("학생식당");
                String[] web_call2 =
                        {"tbody", "tr", "td[data-mqtitle='date']", food_time_type[nums]};
                document = Jsoup.connect(student_restaurant[select_campus]).get();
                if (nums == 0){
                    elements = document.select("tbody").select("tr"); //필요한 녀석만 꼬집어서 지정
                }
                else if(nums == 1){
                    elements = document.select("tbody").select("tr"); //필요한 녀석만 꼬집어서 지정
                }else{

                    elements = document.select("tbody").select("tr"); //필요한 녀석만 꼬집어서 지정
                }
                int i = 0;
                for (Element a : document.select("thead").select("tr").select("span")) {
                    System.out.println(a);
                    if (Objects.equals(a.text(), year+"."+month + "." + day)) {
                        System.out.println("맞음 : "+ a.text()+ year+"."+month+"."+day);
                        select = i;
                        break;
                    }
                    i += 1;
                }
                for (Element b : elements){
                    System.out.println(b);
                }

                // 위 selector로 불러온 엘리먼트의 문자 값으로 중식, 석식 구분해서 잘 해보록.
//                food_type_icon.setImageDrawable(ResourcesCompat.getDrawable(getResources(), food_icon[nums], null));
//                Element e = elements.get(select);
//                String date = e.select("td[data-mqtitle='date']").text();
//                runOnUiThread(() -> {
//                    food_menu.removeAllViews();
//                    cam_name.setText(campus_food_name[select_campus]);
//                    diner_name.setText(dinner_type_name[select_room]);
//                    foods = e.select(food_time_type[nums]).text();
//                    today_date.setText(date);
//                    food_time_view.setText(food_time[nums]);
//                    if (foods.length() != 0) {
//                        menus = foods.split(" ");
//                        for (String menu : menus) {
//                            food_menu.addView(makeMenu(menu));
//                        }
//                    } else {
//                        food_menu.addView(makeMenu("밥 없어요~!!"));
//                    }
//                });
            }else if (select_room == 2){
                System.out.println("직원식당");
                String[] web_call2 =
                        {"tbody", "tr", "td[data-mqtitle='date']", food_time_type[nums]};
                document = Jsoup.connect(campus[select_campus][select_room]).get();
                elements = document.select("tbody").select("tr"); //필요한 녀석만 꼬집어서 지정
                int i = 0;
                for (Element a : document.select("thead").select("tr")) {
                    System.out.println(a);
                    if (Objects.equals(a.text(), month + "월 " + day + "일")) {
                        select = i;
                        break;
                    }
                    i += 1;
                }
                food_type_icon.setImageDrawable(ResourcesCompat.getDrawable(getResources(), food_icon[nums], null));
                Element e = elements.get(select);
                String date = e.select("td[data-mqtitle='date']").text();
                runOnUiThread(() -> {
                    food_menu.removeAllViews();
                    cam_name.setText(campus_food_name[select_campus]);
                    diner_name.setText(dinner_type_name[select_room]);
                    foods = e.select(food_time_type[nums]).text();
                    today_date.setText(date);
                    food_time_view.setText(food_time[nums]);
                    if (foods.length() != 0) {
                        menus = foods.split(" ");
                        for (String menu : menus) {
                            food_menu.addView(makeMenu(menu));
                        }
                    } else {
                        food_menu.addView(makeMenu("밥 없어요~!!"));
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private TextView makeMenu(String content) {
        TextView tv = new TextView(this);
        tv.setText(content);
        tv.setTextColor(Color.parseColor("#000000"));
        tv.setGravity(Gravity.CENTER);
        tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        tv.setTextSize(30);
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

    @SuppressLint({"ResourceType", "UseCompatLoadingForColorStateLists", "UseCompatLoadingForDrawables"})
    private RadioButton makeRadio(String content, int number) {
        RadioButton rdb = new RadioButton(this);
        rdb.setText(content);
        rdb.setButtonDrawable(getResources().getDrawable(R.drawable.radio_selector));
        rdb.setBackground(getResources().getDrawable(R.drawable.radio_selector));
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
            System.out.println("기숙사 선택 : " + number);
        });
        return rdb;
    }
}