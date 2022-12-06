package com.example.term_project;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.ViewCompat;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    int nums = 0;
    int select_campus = 0;
    int select_room = 0;

    LinearLayout food_menu;
    String[] campus = {
            "https://dormi.kongju.ac.kr/HOME/sub.php?code=041303",
            "https://dormi.kongju.ac.kr/HOME/sub.php?code=041304",
            "https://dormi.kongju.ac.kr/HOME/sub.php?code=041301",
            "https://dormi.kongju.ac.kr/HOME/sub.php?code=041302"};
    // 천안, 예산, 은행사/비전, 드림
    String[] food_time_type = {"td[data-mqtitle='breakfast']", "td[data-mqtitle='lunch']", "td[data-mqtitle='dinner']"};
    String[] campus_food_name = {"천안", "예산", "은행사/비전", "드림"};
    String[] dinner_type_name = {"기숙사 식당", "학생식당", "교직원식당"};
    String[] food_time = {"07:30 ~ 09:00", "11:30 ~ 13:30","17:30 ~ 19:00"};
    final Bundle bundle = new Bundle();

    long now = System.currentTimeMillis();

    Date date = new Date(now);
    @SuppressLint("SimpleDateFormat")
    SimpleDateFormat sdf = new SimpleDateFormat("MM:dd");
    String getDate = sdf.format(date);
    String month = getDate.split(":")[0];
    String day = getDate.split(":")[1];

    String[] menus;
    String foods;
    int select = 0;
    BottomNavigationView bottomNavigationView;
    TextView cam_name, diner_name, today_date, food_type, food_time_view;
    ImageView food_type_icon;
    int[] food_icon = {R.drawable.sunrise, R.drawable.sun, R.drawable.moon5};

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        View decorView = getWindow().getDecorView();
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
                    food_type.setText("조식");
                    break;
                case R.id.menu:
                    food_type.setText("조식");
                    break;
            }
            return true;
        });

    }

    Handler handler = new Handler(Looper.getMainLooper()) {
        Document document;
        Elements elements;

        @SuppressLint({"SetTextI18n", "UseCompatLoadingForDrawables"})
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            nums = Integer.parseInt(bundle.getString("numbers"));
            new Thread(() -> {
                try {
                    document = Jsoup.connect(campus[select_campus]).get();
                    elements = document.select("tbody").select("tr"); //필요한 녀석만 꼬집어서 지정
                    int i = 0;
                    for (Element a : elements.select("td[data-mqtitle='date']")) {
                        if (Objects.equals(a.text(), month + "월 " + day + "일")) {
                            select = i;
                            break;
                        }
                        i += 1;
                    }
                    food_type_icon.setImageDrawable(ResourcesCompat.getDrawable(getResources(), food_icon[nums], null));
                    document = Jsoup.connect(campus[select_campus]).get();
                    elements = document.select("tbody").select("tr"); //필요한 녀석만 꼬집어서 지정
                    Element e = elements.get(select);
                    String date = e.select("td[data-mqtitle='date']").text();
                    runOnUiThread(() -> {
                        food_menu.removeAllViews();
                        cam_name.setText(campus_food_name[select_campus]);
                        diner_name.setText(dinner_type_name[select_room]);
                        foods = e.select(food_time_type[nums]).text();
                        today_date.setText(date);
                        food_time_view.setText(food_time[nums]);
                        if (foods.length() != 0){
                            menus = foods.split(" ");
                            for (String menu : menus) {
                                food_menu.addView(test(menu));
                            }
                        }else{
                            food_menu.addView(test("밥 없어요~!!"));
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }).start();
        }
    };

    private TextView test(String content) {
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

}