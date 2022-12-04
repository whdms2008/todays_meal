package com.example.term_project;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Arrays;
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
    String pos;
    int select = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        View decorView = getWindow().getDecorView();
        // Hide both the navigation bar and the status bar.
        // SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
        // a general rule, you should design your app to hide the status bar whenever you
        // hide the navigation bar.
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
//        Spinner spinner = findViewById(R.id.spinner);
//        dayView = findViewById(R.id.date_view);
        adapter = ArrayAdapter.createFromResource(this, R.array.campus, android.R.layout.simple_spinner_dropdown_item);
        //미리 정의된 레이아웃 사용
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        // 스피너 객체에다가 어댑터를 넣어줌
//        spinner.setAdapter(adapter);
//        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            // 선택되면
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                pos = String.valueOf(position);
//                bundle.putString("numbers", pos);                               //핸들러를 이용해서 Thread()에서 가져온 데이터를 메인 쓰레드에 보내준다.
//                Message msg = handler.obtainMessage();
//                msg.setData(bundle);
//                handler.sendMessage(msg);
//            }
//
//            // 아무것도 선택되지 않은 상태일 때
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//                bundle.putString("numbers", "-1");                               //핸들러를 이용해서 Thread()에서 가져온 데이터를 메인 쓰레드에 보내준다.
//                Message msg = handler.obtainMessage();
//                msg.setData(bundle);
//                handler.sendMessage(msg);
//            }
//        });
        View.OnClickListener left_click = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                select -= 1;
                if (select < 0) {
                    select += 1;
                }
                bundle.putString("numbers", pos);                               //핸들러를 이용해서 Thread()에서 가져온 데이터를 메인 쓰레드에 보내준다.
                Message msg = handler.obtainMessage();
                msg.setData(bundle);
                handler.sendMessage(msg);
            }
        };
        View.OnClickListener right_click = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                select += 1;
                if (select > 6) {
                    select -= 1;
                }
                bundle.putString("numbers", pos);                               //핸들러를 이용해서 Thread()에서 가져온 데이터를 메인 쓰레드에 보내준다.
                Message msg = handler.obtainMessage();
                msg.setData(bundle);
                handler.sendMessage(msg);
            }
        };
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
//                    TextView[] datas =
//                            {findViewById(R.id.date_view), findViewById(R.id.breakfast), findViewById(R.id.lunch), findViewById(R.id.dinner)
//                            };
                    Element e = elements.get(select); // 날짜 같은 index가 몇번인지 찾아서 해결해보자~
                    System.out.println(e.text());
                    String date = e.select("td[data-mqtitle='date']").text();

                    System.out.println(month + "월 " + day + "일 ===" + date);
                    System.out.println(e);
                    String breakfast = e.select("td[data-mqtitle='breakfast']").text().replace(",", "\n");
                    String lunch = e.select("td[data-mqtitle='lunch']").text();
                    String dinner = e.select("td[data-mqtitle='dinner']").text();
//                    datas[0].setText(date + "( " + e.select(".day").text() + "요일 )");
//                    datas[1].setText(breakfast);
//                    datas[2].setText(lunch);
//                    datas[3].setText(dinner);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }).start();
        }
    };
}