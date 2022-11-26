package com.example.term_project;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Spinner spinner = findViewById(R.id.spinner);
        dayView = findViewById(R.id.day_1);
        adapter = ArrayAdapter.createFromResource(this, R.array.campus, android.R.layout.simple_spinner_dropdown_item);
        //미리 정의된 레이아웃 사용
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        // 스피너 객체에다가 어댑터를 넣어줌
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            // 선택되면
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                bundle.putString("numbers", String.valueOf(position));                               //핸들러를 이용해서 Thread()에서 가져온 데이터를 메인 쓰레드에 보내준다.
                Message msg = handler.obtainMessage();
                msg.setData(bundle);
                handler.sendMessage(msg);
            }

            // 아무것도 선택되지 않은 상태일 때
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                bundle.putString("numbers", "0");                               //핸들러를 이용해서 Thread()에서 가져온 데이터를 메인 쓰레드에 보내준다.
                Message msg = handler.obtainMessage();
                msg.setData(bundle);
                handler.sendMessage(msg);
            }
        });
    }

    Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            nums = Integer.parseInt(bundle.getString("numbers"));
            new Thread(() -> {
                Document document;
                try {
                    @SuppressLint("CutPasteId") TextView[][] datas = {
                            {findViewById(R.id.day_1), findViewById(R.id.date_1), findViewById(R.id.breakfast_1), findViewById(R.id.lunch_1), findViewById(R.id.dinner_1)},
                            {findViewById(R.id.day_2), findViewById(R.id.date_2), findViewById(R.id.breakfast_2), findViewById(R.id.lunch_2), findViewById(R.id.dinner_2)},
                            {findViewById(R.id.day_3), findViewById(R.id.date_3), findViewById(R.id.breakfast_3), findViewById(R.id.lunch_3), findViewById(R.id.dinner_3)},
                            {findViewById(R.id.day_4), findViewById(R.id.date_4), findViewById(R.id.breakfast_4), findViewById(R.id.lunch_4), findViewById(R.id.dinner_4)},
                            {findViewById(R.id.day_5), findViewById(R.id.date_5), findViewById(R.id.breakfast_5), findViewById(R.id.lunch_5), findViewById(R.id.dinner_5)},
                            {findViewById(R.id.day_6), findViewById(R.id.date_6), findViewById(R.id.breakfast_6), findViewById(R.id.lunch_6), findViewById(R.id.dinner_6)},
                            {findViewById(R.id.day_7), findViewById(R.id.date_7), findViewById(R.id.breakfast_7), findViewById(R.id.lunch_7), findViewById(R.id.dinner_7)},
                    };
                    document = Jsoup.connect(campus[nums]).get();
                    Elements elements = document.select("tbody").select("tr"); //필요한 녀석만 꼬집어서 지정
                    System.out.println(campus_name[nums]);
                    int i = 0;
                    for (Element e : elements) {
//                        System.out.println(e.select(".day").text());
//                        System.out.println(e.select("td[data-mqtitle='date']").text());
//                        System.out.println(e.select("td[data-mqtitle='breakfast']").text());
//                        System.out.println(e.select("td[data-mqtitle='lunch']").text());
//                        System.out.println(e.select("td[data-mqtitle='dinner']").text());
                        datas[i][0].setText(e.select(".day").text());
                        datas[i][1].setText(e.select("td[data-mqtitle='date']").text());
                        datas[i][2].setText(e.select("td[data-mqtitle='breakfast']").text());
                        datas[i][3].setText(e.select("td[data-mqtitle='lunch']").text());
                        datas[i][4].setText(e.select("td[data-mqtitle='dinner']").text());
                        i += 1;
                    }
//                    Elements day = document.select("#food-info > div > table > tbody > tr:nth-child(" + nums + ") > td.noedge-l.first > font");
//                    Elements date = document.select("#food-info > div > table > tbody > tr:nth-child(" + nums + ") > td:nth-child(2)");
//                    Elements breakfast = document.select("#food-info > div > table > tbody > tr:nth-child(" + nums + ") > td:nth-child(3)");
//                    Elements lunch = document.select("#food-info > div > table > tbody > tr:nth-child(" + nums + ") > td:nth-child(4) ");
//                    Elements dinner = document.select("#food-info > div > table > tbody > tr:nth-child(" + nums + ") > td.noedge-r.last");

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }).start();
        }
    };
}