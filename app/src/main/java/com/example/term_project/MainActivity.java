package com.example.term_project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Typeface;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
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
import java.util.Date;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    int nums = 0;
    TextView dayView;
    String[] campus = {
            "https://dormi.kongju.ac.kr/HOME/sub.php?code=041303",
            "https://dormi.kongju.ac.kr/HOME/sub.php?code=041304",
            "https://dormi.kongju.ac.kr/HOME/sub.php?code=041301",
            "https://dormi.kongju.ac.kr/HOME/sub.php?code=041302"};
    // 천안, 예산, 은행사/비전, 드림
    String[] campus_name = {"천안", "예산", "은행사/비전", "드림"};
    final Bundle bundle = new Bundle();
    ArrayAdapter<CharSequence> adapter = null;

    long now = System.currentTimeMillis();

    Date date = new Date(now);
    @SuppressLint("SimpleDateFormat")
    SimpleDateFormat sdf = new SimpleDateFormat("MM:dd");
    String getDate = sdf.format(date);
    String month = getDate.split(":")[0];
    String day = getDate.split(":")[1];
    String pos = "-1";
    String[] menus;
    int select = 0;
    BottomNavigationView bottomNavigationView;
    SettingsFragment settingsFragment;
    TextView cam_name, diner_name, today_date, food_type, food_time;
    LinearLayout food_menu;
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
        food_time = findViewById(R.id.food_time);
        food_menu = findViewById(R.id.food_menu);

        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        bottomNavigationView = findViewById(R.id.menus);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()){
                case R.id.sun_rise:
                    food_type.setText("조식");
                    bundle.putString("numbers", pos);                               //핸들러를 이용해서 Thread()에서 가져온 데이터를 메인 쓰레드에 보내준다.
                    Message msg = handler.obtainMessage();
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                    break;
                case R.id.sun:
                    food_type.setText("중식");
                    break;
                case R.id.moon:
                    food_type.setText("석식");
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

    private void CreateMenu(String[] menus){
        for(int i = 0; i < menus.length;i++){
            TextView tv = new TextView(getApplicationContext());
            tv.setText(menus[i]);
            tv.setTextColor(Color.parseColor("#000000"));
            tv.setGravity(Gravity.CENTER);
            tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            tv.setTextSize(30);
            Typeface typeface = getResources().getFont(R.font.scd5);
            tv.setTypeface(typeface);
            tv.setId(i);
            LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT
                    ,LinearLayout.LayoutParams.WRAP_CONTENT);
            param.leftMargin = 20;
            param.rightMargin = 20;
            param.weight = 1;
            tv.setLayoutParams(param);
            food_menu.addView(tv);
        }
    }

    Handler handler = new Handler(Looper.getMainLooper()) {
        @SuppressLint("SetTextI18n")
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            nums = Integer.parseInt(bundle.getString("numbers"));
            new Thread(() -> {
                Document document;
                Elements elements;
                try {
                    if (nums == -1) {
                        document = Jsoup.connect(campus[0]).get();
                        nums = 0;
                        elements = document.select("tbody").select("tr"); //필요한 녀석만 꼬집어서 지정
                        int i = 0;
                        for (Element a : elements.select("td[data-mqtitle='date']")) {
                            System.out.println(a.text());
                            i += 1;
                            if (Objects.equals(a.text(), month + "월 " + day + "일")) {
                                select = i;
                                System.out.println(i);
                                break;
                            }
                        }
                    } else {
                        document = Jsoup.connect(campus[nums]).get();
                    }
                    elements = document.select("tbody").select("tr"); //필요한 녀석만 꼬집어서 지정
                    System.out.println(month + day);
                    System.out.println(campus_name[nums]);
                    TextView[] datas =
                            {findViewById(R.id.date)};

                    Element e = elements.get(select); // 날짜 같은 index가 몇번인지 찾아서 해결해보자~
                    System.out.println(e.text());
                    String date = e.select("td[data-mqtitle='date']").text();

                    System.out.println(month + "월 " + day + "일 ===" + date);
                    System.out.println(e);
                    System.out.println(e.select("td[data-mqtitle='breakfast']").text());

                    String[] breakfast = e.select("td[data-mqtitle='breakfast']").text().split(",");
                    CreateMenu(breakfast);
//                    String lunch = e.select("td[data-mqtitle='lunch']").text();
//                    String dinner = e.select("td[data-mqtitle='dinner']").text();
//                    datas[0].setText(date + "( " + e.select(".day").text() + "요일 )");
                    datas[0].setText("12월 6일");

                    try {
                        for (int i = 0; i < breakfast.length; i++) {
                            datas[1+i].setText(breakfast[i]);
                        }
                    }catch(Exception exception){
                        System.out.println("에러!");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }).start();
        }
    };
}