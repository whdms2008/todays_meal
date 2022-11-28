package com.example.term_project;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
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
    int nums;
    TextView dayView;
    String[] food_type = {"기숙사 식당","학생식당","직원식당"};
    String[] campus_name = {"천안", "예산", "은행사/비전 (신관)", "드림 (신관)"};
    // 특이사항 : 예산은 기숙사 식당이 없고 학생식당, 직원식당만 있음.

    String[] campus = {
            "https://dormi.kongju.ac.kr/HOME/sub.php?code=041303", // 천안
            "https://dormi.kongju.ac.kr/HOME/sub.php?code=041304", // 예산
            "https://dormi.kongju.ac.kr/HOME/sub.php?code=041301", // 은행사/비전 ( 신관 )
            "https://dormi.kongju.ac.kr/HOME/sub.php?code=041302"}; // 드림 ( 신관 )

    String[] school_cafeteria = {
            "https://www.kongju.ac.kr/kongju/13157/subview.do", // 천안
            "https://www.kongju.ac.kr/kongju/13159/subview.do", // 예산
            "https://www.kongju.ac.kr/kongju/13155/subview.do"  // 신관
    };
    String[] staff_cafeteria = {
            "https://www.kongju.ac.kr/kongju/13158/subview.do", // 천안
            "https://www.kongju.ac.kr/kongju/13160/subview.do", // 예산
            "https://www.kongju.ac.kr/kongju/13156/subview.do"  // 신관
    };

    final Bundle bundle = new Bundle();
    ArrayAdapter<CharSequence> campus_adapter = null;
    ArrayAdapter<CharSequence> type_adapter = null;

    long now = System.currentTimeMillis();

    Date date = new Date(now);
    @SuppressLint("SimpleDateFormat")
    SimpleDateFormat sdf = new SimpleDateFormat("MM:dd");
    String getDate = sdf.format(date);
    String month = getDate.split(":")[0];
    String day = getDate.split(":")[1];
    String pos;
    int select = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Spinner spinner = findViewById(R.id.spinner);
        Spinner spinner2 = findViewById(R.id.food_type);
        dayView = findViewById(R.id.date_view);
        campus_adapter = ArrayAdapter.createFromResource(this, R.array.campus, android.R.layout.simple_spinner_dropdown_item);
        campus_adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spinner.setAdapter(campus_adapter);


        type_adapter = ArrayAdapter.createFromResource(this, R.array.type, android.R.layout.simple_spinner_dropdown_item);
        type_adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spinner2.setAdapter(type_adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            // 선택되면
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                pos = String.valueOf(position);
                bundle.putString("numbers", pos);                               //핸들러를 이용해서 Thread()에서 가져온 데이터를 메인 쓰레드에 보내준다.
                Message msg = handler.obtainMessage();
                msg.setData(bundle);
                handler.sendMessage(msg);
            }

            // 아무것도 선택되지 않은 상태일 때
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                bundle.putString("numbers", "-1");
                Message msg = handler.obtainMessage();
                msg.setData(bundle);
                handler.sendMessage(msg);
            }
        });
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
        Button btn_left = findViewById(R.id.left_btn);
        btn_left.setOnClickListener(left_click);
        Button btn_right = findViewById(R.id.right_btn);
        btn_right.setOnClickListener(right_click);
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
                    System.out.println(nums);
                    document = Jsoup.connect(campus[nums == -1 ? 0 :  nums]).get();
                    elements = document.select("tbody").select("tr"); //필요한 녀석만 꼬집어서 지정
                    if (select == -1){
                        nums = 0;
                        int i = 0;
                        for (Element a : elements.select("td[data-mqtitle='date']")) {
                            System.out.println(a.text());
                            if (Objects.equals(a.text(), month + "월 " + day + "일")) {
                                select = i;
                                System.out.println("맞음! : "+i);
                                break;
                            }
                            i += 1;
                        }
                    }
                    elements = document.select("tbody").select("tr"); //필요한 녀석만 꼬집어서 지정
                    TextView[] datas =
                            {findViewById(R.id.date_view), findViewById(R.id.breakfast), findViewById(R.id.lunch), findViewById(R.id.dinner)
                            };
                    Element e = elements.get(select); // 날짜 같은 index가 몇번인지 찾아서 해결해보자~
                    String date = e.select("td[data-mqtitle='date']").text();
                    String breakfast = e.select("td[data-mqtitle='breakfast']").text().replace(",", "\n");
                    String lunch = e.select("td[data-mqtitle='lunch']").text();
                    String dinner = e.select("td[data-mqtitle='dinner']").text();
                    datas[0].setText(date + "( " + e.select(".day").text() + "요일 )");
                    datas[1].setText(breakfast);
                    datas[2].setText(lunch);
                    datas[3].setText(dinner);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }).start();
        }
    };
}