package com.whats_meal.term_project;

import static android.content.ContentValues.TAG;

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
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
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
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;

import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.Transaction;
import com.google.gson.Gson;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.UUID;

public class MainActivity extends AppCompatActivity implements
        GestureDetector.OnGestureListener{

    private Calendar calendar;
    private AlarmManager alarmManager;
    SharedPreferences pref;
    SharedPreferences.Editor editor;

    AtomicInteger Temporary_num = new AtomicInteger();
    AtomicInteger Temporary_food_type_num = new AtomicInteger();
    AtomicInteger Temporary_food_room_num = new AtomicInteger();
    LinearLayout food_menu;

    LinearLayout ad_container;
    String[][] campus = {
            {"https://dormi.kongju.ac.kr/HOME/sub.php?code=041303"}, // 천안 생활관 식당 [ 천안 ]
            {"https://dormi.kongju.ac.kr/HOME/sub.php?code=041304"}, // 예산 생활관 식당 [ X ]
            {"https://dormi.kongju.ac.kr/HOME/sub.php?code=041301",  // 신관 생활관 식당 [ 은행사/비전 ]
                    "https://dormi.kongju.ac.kr/HOME/sub.php?code=041302"}}; // 신관 생활관 식당 [ 드림 ]

    String[] today_menus = {"", "", ""}; // 조식메뉴, 중식, 석식

    String[][] week_menus = {
            {"", "", "", "", "", "", ""},  // 조식
            {"", "", "", "", "", "", ""},  // 중식
            {"", "", "", "", "", "", ""}, // 석식
            {"", "", "", "", "", "", ""}, // 날짜
            {"", "", "", "", "", "", ""}}; // 요일

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
    int day = Integer.parseInt(getDate.split(":")[2]);
    String hour = getDate.split(":")[3];
    String minute = getDate.split(":")[4];
    String alarm_food_name = "";
    String alarm_food_type_name = "";

    ArrayList<ArrayList<Integer>> vote_stats = new ArrayList<>();


    int select = 0;
    int nums = 0; // 조식, 중식, 석식
    int select_campus = 0; // 캠퍼스 종류
    int select_room = 0; // 식당 종류
    int select_food_room = 0; // 기숙사의 경우 식당 종류

    int today_select = -1;
    String food_menus = ""; // 기숙사 조식
    int[] food_icon = {R.drawable.sun_morning, R.drawable.breakfast, R.drawable.dinner_icon};

    BottomNavigationView bottomNavigationView;
    TextView cam_name, diner_name, today_date, food_type, food_time_view, food_day, update_view;

    ImageView food_type_icon, notify_icon;
    ImageButton share_btn;

    ImageButton like_btn;
    ImageButton hate_btn;

    Button ad_btn;

    Dialog dialog01;
    Dialog menu_dialog;

    Dialog update_dialog;

    LinearLayout likes_container;

    private boolean notify = false;

    private static final int REQUEST_CODE_UPDATE = 1015;

    private static final String update_text = "- 좋아요/싫어요 기능 추가\n- 레이아웃 수정\n- 개발자 도와주기 버튼 추가\n- 업데이트 내역 업데이트시 출력\n- 개발자 : 항상 이용해주셔서 감사합니다.";

    private AppUpdateManager appUpdateManager;
    private InstallStateUpdatedListener installStateUpdatedListener;

    private GestureDetector gestureDetector;


    private FirebaseFirestore db;

    private String uuid;

    int update_chk;

    private final ArrayList<DocumentReference> docRefs = new ArrayList<>();
    private DocumentReference updateRef;


    private RewardedAd mRewardedAd;

    @SuppressLint({"UseCompatLoadingForDrawables", "SetTextI18n", "NonConstantResourceId", "ClickableViewAccessibility", "DefaultLocale", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        MobileAds.initialize(this, initializationStatus -> {
        });

        for (int i = 0; i < 3; i++) {
            ArrayList<Integer> innerList = new ArrayList<>();
            for (int j = 0; j < 3; j++) {
                innerList.add(0);
            }
            innerList.add(-1);
            vote_stats.add(innerList);
        }

        // Firestore 인스턴스 초기화
        db = FirebaseFirestore.getInstance();
        docRefs.add(db.collection("whats-meal").document("morning_likes"));
        docRefs.add(db.collection("whats-meal").document("sun_likes"));
        docRefs.add(db.collection("whats-meal").document("moon_likes"));
        updateRef = db.collection("whats-meal-update").document("mzSjXwOsExOhsdSJd8bb");
        LinearLayout view_page = findViewById(R.id.view_page);
        appUpdateManager = AppUpdateManagerFactory.create(this);

        // 업데이트 상태를 확인하는 리스너 등록
        installStateUpdatedListener = installState -> {
            if (installState.installStatus() == InstallStatus.DOWNLOADED) {
                // 다운로드 완료 시 업데이트 설치
                popupSnackbarForCompleteUpdate();
            }
        };

        // 업데이트 정보를 확인하고 업데이트 다이얼로그 표시
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();
        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                try {
                    appUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo,
                            AppUpdateType.IMMEDIATE,
                            this,
                            REQUEST_CODE_UPDATE);
                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }
            }
        });


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
        uuid = getStoredUUID();
        update_chk = pref.getInt("update_chk", 0);
        vote_stats.get(0).set(0, pref.getInt("like_morning", 0));
        vote_stats.get(0).set(1, pref.getInt("dislike_morning_", 0));
        vote_stats.get(0).set(2, pref.getInt("vote_chk", -1));

        vote_stats.get(1).set(0, pref.getInt("like_sun", 1));
        vote_stats.get(1).set(1, pref.getInt("dislikesun", 1));
        vote_stats.get(1).set(2, pref.getInt("vote_chk", -1));

        vote_stats.get(2).set(0, pref.getInt("like_moon", 2));
        vote_stats.get(2).set(1, pref.getInt("dislike_moon", 2));
        vote_stats.get(1).set(2, pref.getInt("vote_chk", -1));


        update_view = findViewById(R.id.update_text_view);

        cam_name = findViewById(R.id.cam_name);
        diner_name = findViewById(R.id.diner_type);
        today_date = findViewById(R.id.date);
        food_type = findViewById(R.id.food_type);
        food_time_view = findViewById(R.id.food_time);
        food_menu = findViewById(R.id.food_menu);
        food_type_icon = findViewById(R.id.food_type_icon);
        food_day = findViewById(R.id.day);

        ad_btn = findViewById(R.id.ad_btn);
        share_btn = findViewById(R.id.share);

        like_btn = findViewById(R.id.like_button);
        like_btn.setOnClickListener(view -> {
            addUUIDToType("like");
            updateLikesCount();
        });


        hate_btn = findViewById(R.id.dislike_button);
        hate_btn.setOnClickListener(view -> {
            addUUIDToType("hate");
            updateLikesCount();
        });

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
        WindowManager.LayoutParams params2 = menu_dialog.getWindow().getAttributes();
        params2.width = LinearLayout.LayoutParams.MATCH_PARENT;
        params2.height = LinearLayout.LayoutParams.WRAP_CONTENT;
        menu_dialog.getWindow().setAttributes(params);

        update_dialog = new Dialog(this);
        update_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        update_dialog.setContentView(R.layout.update_view);
        WindowManager.LayoutParams update_params = update_dialog.getWindow().getAttributes();
        update_params.width = LinearLayout.LayoutParams.MATCH_PARENT;
        update_params.height = LinearLayout.LayoutParams.MATCH_PARENT;
        update_dialog.getWindow().setAttributes(update_params);

        likes_container = findViewById(R.id.like_dislike_container);

        ad_container = findViewById(R.id.ad_layout);


        notify_icon = findViewById(R.id.notification);
        notify_icon.setImageDrawable(
                notify ? getDrawable(R.drawable.true_bell) : getDrawable(R.drawable.false_bell)
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
        today_date.setText(month + "월 " + String.format("%02d", day) + "일");

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

        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                float deltaX = e2.getX() - e1.getX();
                float deltaY = e2.getY() - e1.getY();

                if (nums <= 2 && Math.abs(deltaX) > Math.abs(deltaY)) {
                    if (deltaX > 0) {
                        try {
                            select--;
                            new Thread(() -> load_food(nums)).start();
                        } catch (ArrayIndexOutOfBoundsException ab) {
                            select++;
                            new Thread(() -> load_food(nums)).start();
                        }
                    } else {
                        try {
                            select++;
                            new Thread(() -> load_food(nums)).start();
                        } catch (ArrayIndexOutOfBoundsException ab) {
                            select--;
                            new Thread(() -> load_food(nums)).start();
                        }
                    }
                }
                return true;
            }
        });
        view_page.setOnTouchListener((view, motionEvent) -> gestureDetector.onTouchEvent(motionEvent));
        share_btn.setOnClickListener(v -> {
            Bitmap bitmap = captureScreen();
            Uri imageUri = saveBitmapToFile(bitmap);
            shareImage(imageUri);
        });

        ad_btn.setOnClickListener(view -> loadAd());
        updateCheck();
    }

    public void updateCheck() {

        AtomicInteger update_v = new AtomicInteger();
        db.runTransaction((Transaction.Function<Void>) transaction -> {
            // 문서 가져오기
            DocumentSnapshot snapshot = transaction.get(updateRef);
            update_v.set((Objects.requireNonNull(snapshot.getLong("version"))).intValue());
            return null;
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                runOnUiThread(() -> {
                    if (update_chk != update_v.get()) {
                        editor.putInt("update_chk", update_v.get());
                        editor.apply();
                        update_dialog.show(); // 다이얼로그 띄우기
                        update_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        TextView app_version = update_dialog.findViewById(R.id.app_version);
                        app_version.setText(getVersion(this));

                        TextView update_text_view = update_dialog.findViewById(R.id.update_text_view);
                        update_text_view.setText(update_text);

                        update_dialog.findViewById(R.id.confirm_btn).setOnClickListener(view -> {
                            update_dialog.dismiss(); // 다이얼로그 닫기
                        });
                    }
                });
                Log.d("Transaction", "Success");
            } else {
                Log.d("Transaction", "Failure", task.getException());
            }
        });
    }


    public static String generateUUID() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

    private String getStoredUUID() {
        String uuid = pref.getString("uuid", null);

        if (uuid == null) {
            uuid = generateUUID();
            editor.putString("uuid", uuid);
            editor.apply();
        }

        return uuid;
    }
    private void updateButtonWidths() {
        DocumentReference docRef = docRefs.get(nums);
        docRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    List<String> likeList = (List<String>) documentSnapshot.get("like");
                    List<String> hateList = (List<String>) documentSnapshot.get("hate");
                    int like_cnt = likeList != null ? likeList.size() : 1;
                    int hate_cnt = hateList != null ? hateList.size() : 1;
                    runOnUiThread(() -> {
                        float total = hate_cnt + like_cnt;
                        float likeWeight = like_cnt / total;
                        float dislikeWeight = hate_cnt / total;

                        LinearLayout.LayoutParams likeParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, likeWeight);
                        LinearLayout.LayoutParams dislikeParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, dislikeWeight);
                        like_btn.setLayoutParams(likeParams);
                        hate_btn.setLayoutParams(dislikeParams);
                    });
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error getting UUIDs count in like field", e);
                });
    }

    public void checkIfUUIDExists(UUIDCheckResultCallback callback) {
        DocumentReference docRef = docRefs.get(nums);

        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null) {
                    List<String> likeList = (List<String>) document.get("like");
                    List<String> hateList = (List<String>) document.get("hate");

                    boolean uuidExistsInLike = likeList != null && likeList.contains(uuid);
                    boolean uuidExistsInHate = hateList != null && hateList.contains(uuid);

                    if (uuidExistsInLike) {
                        callback.onResult(true, "like");
                    } else if (uuidExistsInHate) {
                        callback.onResult(true, "hate");
                    } else {
                        callback.onResult(false, "");
                    }
                } else {
                    Log.d(TAG, "No such document");
                    callback.onResult(false, "");
                }
            } else {
                Log.d(TAG, "get failed with ", task.getException());
                callback.onResult(false, "");
            }
        });
    }

    public interface UUIDCheckResultCallback {
        void onResult(boolean exists, String type);
    }

    public void addUUIDToType(String types) {
        checkIfUUIDExists((exists, type) -> {
            if (!exists) {
                DocumentReference docRef = docRefs.get(nums);
                docRef.update(types, FieldValue.arrayUnion(uuid))
                        .addOnSuccessListener(aVoid -> Log.d(TAG, "UUID added to " + types))
                        .addOnFailureListener(e -> Log.w(TAG, "Error adding UUID to " + types, e));
                updateLikesCount();
            } else {
                Toast.makeText(getApplicationContext(), "하루에 1번만 가능합니다.", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void updateLikesCount() {
        checkIfUUIDExists((exists, type) -> {
            if (exists) {
                Log.d(TAG, "UUID exists in: " + type);
                if (Objects.equals(type, "like")) {
                    like_btn.setImageResource(R.drawable.true_like);
                    hate_btn.setImageResource(R.drawable.false_dislike);
                } else{
                    hate_btn.setImageResource(R.drawable.true_dislike);
                    like_btn.setImageResource(R.drawable.false_like);
                }
                editor.apply();
            }else{
                hate_btn.setImageResource(R.drawable.false_dislike);
                like_btn.setImageResource(R.drawable.false_like);

            }
            updateButtonWidths();
        });
    }

    public void loadAd() {

        AdRequest adRequest = new AdRequest.Builder().build();

        // "ca-app-pub-4622337582094332/7152127242"

        // 'InterstitialAd' 대신 'RewardedAd'를 로드합니다.
        RewardedAd.load(this,
                "ca-app-pub-3940256099942544/5224354917",
                adRequest,
                new RewardedAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                        mRewardedAd = rewardedAd;
                        Log.d("ads", "Ad was loaded.");

                        mRewardedAd.setFullScreenContentCallback(new FullScreenContentCallback(){
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                mRewardedAd = null;
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                                mRewardedAd = null;
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                mRewardedAd = null;
                            }
                        });
                        showRewardedAd();
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        Log.i("ads", loadAdError.getMessage());
                        mRewardedAd = null;
                    }
                });
    }

    public void showRewardedAd() {
        if (mRewardedAd != null) {
            Activity activityContext = MainActivity.this;
            mRewardedAd.show(activityContext, rewardItem -> {
                Toast.makeText(this, "도움 주셔서 감사합니다.", Toast.LENGTH_SHORT).show();
                Log.d("ads", "User earned the reward.");
            });
        } else {
            Toast.makeText(this, "광고가 아직 준비되지 않았습니다.", Toast.LENGTH_SHORT).show();
            Log.d("ads", "The rewarded ad wasn't ready yet.");
        }
    }

    private Bitmap captureScreen() {
        View rootView = getWindow().getDecorView().getRootView();
        rootView.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(rootView.getDrawingCache());
        rootView.setDrawingCacheEnabled(false);
        return bitmap;
    }

    private Uri saveBitmapToFile(Bitmap bitmap) {
        try {
            File cachePath = new File(getExternalCacheDir(), "images");
            if (!cachePath.mkdirs()) {
                Log.e(TAG, "디렉토리 생성실패 : " + cachePath);
            }
            FileOutputStream stream = new FileOutputStream(cachePath + "/screenshot.png");
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        File imagePath = new File(getExternalCacheDir(), "images/screenshot.png");
        return FileProvider.getUriForFile(this, getPackageName() + ".provider", imagePath);
    }

    private void shareImage(Uri imageUri) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("image/png");
        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(shareIntent, "식단 공유하기"));
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (this.gestureDetector.onTouchEvent(event)) {
            return true;
        }
        return super.onTouchEvent(event);
    }

    private void popupSnackbarForCompleteUpdate() {
        Snackbar.make(findViewById(android.R.id.content), "업데이트 다운로드 완료",
                Snackbar.LENGTH_INDEFINITE).setAction("재시작", view -> appUpdateManager.completeUpdate()).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        appUpdateManager.registerListener(installStateUpdatedListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        appUpdateManager.unregisterListener(installStateUpdatedListener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_UPDATE) {
            if (resultCode != RESULT_OK) {
                // 업데이트가 취소되거나 실패한 경우
                Snackbar.make(findViewById(android.R.id.content), "업데이트 취소 또는 실패",
                        Snackbar.LENGTH_LONG).show();
            }
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
        notify_icon.setImageDrawable(getDrawable(R.drawable.false_bell));

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
        notify_icon.setImageDrawable(getDrawable(R.drawable.true_bell));
        notify = true;
        editor.putBoolean("notify", true);
        editor.apply();

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
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, putData, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }


    private void createNotificationChannel() {
        CharSequence name = "알림";
        String description = "알림입니다!";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel = new NotificationChannel("JoEun_Notification_Channel", name, importance);
        channel.setDescription(description);

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);

    }


    Handler handler = new Handler(Looper.getMainLooper()) {
        @SuppressLint({"SetTextI18n", "UseCompatLoadingForDrawables"})
        @Override
        public void handleMessage(Message msg) {
            int temp_num = nums;
            Bundle bundle = msg.getData();
            nums = Integer.parseInt(bundle.getString("numbers"));
            new Thread(() -> {
                switch (nums) {
                    case 0:
                    case 1:
                    case 2:
                        updateLikesCount();
                        food_type.setText(food_time_name[nums]);
                        editor.putInt("select_time", nums);
                        editor.apply();
                        load_food(nums);
                        break;
                    case 3:
                        popup(0, temp_num);
                        break;
                    case 4:
                        popup(1, temp_num);
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

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    private void popup(int num, int temp_num) {
        runOnUiThread(() -> {
            if (num == 0) { // 캠퍼스
                dialog01.show(); // 다이얼로그 띄우기
                dialog01.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                RadioGroup radioGroup = dialog01.findViewById(R.id.radio_group);
                Button campus_1 = dialog01.findViewById(R.id.radio_1);
                Button campus_2 = dialog01.findViewById(R.id.radio_2);
                Button campus_3 = dialog01.findViewById(R.id.radio_3);

                Button[] campus_list = {campus_1, campus_2, campus_3};

                today_date.setText(month + "월 " + String.format("%02d", day) + "일");
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

                dialog01.findViewById(R.id.noBtn).setOnClickListener(view -> {
                    nums = temp_num;
                    if (nums == 0) {
                        findViewById(R.id.sun_rise).performClick();
                    } else if (nums == 1) {
                        findViewById(R.id.sun).performClick();

                    } else if (nums == 2) {
                        findViewById(R.id.moon).performClick();
                    }
                    dialog01.dismiss();
                });
                dialog01.findViewById(R.id.yesBtn).setOnClickListener(view -> {
                    today_select = -1;
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
                    week_menus = new String[][]{
                            {"", "", "", "", "", "", ""},
                            {"", "", "", "", "", "", ""},
                            {"", "", "", "", "", "", ""},
                            {"", "", "", "", "", "", ""},
                            {"", "", "", "", "", "", ""}};

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
                    nums = temp_num;
                    if (nums == 0) {
                        findViewById(R.id.sun_rise).performClick();
                    } else if (nums == 1) {
                        findViewById(R.id.sun).performClick();

                    } else if (nums == 2) {
                        findViewById(R.id.moon).performClick();
                    } else{
                        findViewById(R.id.moon).performClick();
                    }
                    menu_dialog.dismiss(); // 다이얼로그 닫기
                });
            }
        });
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    private void load_food(int numbers) {
        try {
            pref = this.getSharedPreferences("pref", Activity.MODE_PRIVATE);
            if (pref.getBoolean("notify", false)) {
                cancelAlarm();
                setAlarm();
            }
            Document document;
            Elements elements;
            if ((Objects.equals(week_menus[0][4], "") && Objects.equals(week_menus[1][4], "")) && Objects.equals(week_menus[2][4], "")) {
                document = Jsoup.connect(select_room != 0 ? restaurants[select_room][select_campus] : campus[select_campus][select_food_room]).get();
                elements = document.select("tbody tr");
                // 학식
                if (select_room != 0 || select_campus == 0 && !elements.select("td[class='noedge-l first']").isEmpty() && elements.select("td[data-mqtitle='lunch']").text().isEmpty()) { // 기숙사가 아닐때 식단
                    try {
                        if (select_campus == 0 && !elements.select("td[class='noedge-l first']").isEmpty()) {
                            document = Jsoup.connect("https://www.kongju.ac.kr/kongju/13163/subview.do").get();
                            elements = document.select("tbody tr");
                        }
                        updateSelectAndWeekMenus(document);

                        for (Element e : elements) {
                            switch (e.children().get(0).text()) {
                                case "조식":
                                    updateWeekMenus(e, 0);
                                    break;
                                case "중식":
                                    updateWeekMenus(e, 1);
                                    break;
                                case "석식":
                                    updateWeekMenus(e, 2);
                                    break;
                            }
                        }
                    } catch (ArrayIndexOutOfBoundsException e) {
                        select = 6;
                    }
                    saveWeekMenus(getApplicationContext(), week_menus);
                } else { // 기식
                    updateWeekMenusForCampus(elements);
                    saveWeekMenus(getApplicationContext(), week_menus);
                }
            }
            updateUI(numbers);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateSelectAndWeekMenus(Document document) {
        Elements thElements = document.select("thead tr th");
        Element onElement = document.select("th.on").first();
        select = (onElement != null) ? thElements.indexOf(onElement) - 1 : 6;
        if (today_select == -1) {
            today_select = select;
        }
        String regex = "(\\d{2}\\.\\d{2} )";
        Pattern pattern = Pattern.compile(regex);

        String regex2 = "\\(\\s*(.+?)\\s*\\)";
        Pattern pattern2 = Pattern.compile(regex2);
        Elements week_menu_times = document.select("thead tr th");
        Matcher matcher;
        Matcher matcher2;
        for (int i = 1; i < week_menu_times.size(); i++) {
            matcher = pattern.matcher(week_menu_times.get(i).text());
            matcher2 = pattern2.matcher(week_menu_times.get(i).text());
            if (matcher.find()) {
                String extracted = matcher.group(1);
                assert extracted != null;
                week_menus[3][i - 1] = extracted.replace(" ", "일").replace(".", "월 ");
            }
            if (matcher2.find()) {
                String extracted = matcher2.group(1);
                assert extracted != null;
                week_menus[4][i - 1] = extracted;
            }
        }
    }

    private void updateWeekMenus(Element e, int index) {
        for (int i = 0; i < e.children().size() - 1; i++) {
            week_menus[index][i] = e.children().get(i + 1).text();
        }
    }

    @SuppressLint("DefaultLocale")
    private void updateWeekMenusForCampus(Elements elements) {
        for (int i = 0; i < elements.size(); i++) {
            week_menus[0][i] = elements.get(i).select(food_time_type[0]).text().replaceAll(",", " ").replaceAll(" {2}", " ");
            week_menus[1][i] = elements.get(i).select(food_time_type[1]).text().replaceAll(",", " ").replaceAll(" {2}", " ");
            week_menus[2][i] = elements.get(i).select(food_time_type[2]).text().replaceAll(",", " ").replaceAll(" {2}", " ");
            week_menus[3][i] = elements.get(i).select("td[data-mqtitle='date']").text();
            week_menus[4][i] = elements.get(i).select("td[data-mqtitle='day']").text();
        }
        List<String> list = Arrays.asList(elements.select("td[data-mqtitle='date']").text().replace(" ", "").split("일"));
        select = list.indexOf(month + "월" + String.format("%02d", day));
        if (today_select == -1) {
            today_select = select;
        }
    }

    @SuppressLint("SetTextI18n")
    private void updateUI(int numbers) {
        runOnUiThread(() -> {
            food_type_icon.setImageDrawable(ResourcesCompat.getDrawable(getResources(), food_icon[numbers], null));
            food_menu.removeAllViews();
            food_time_view.setText(food_time[numbers][select_campus][select_room]);
            handleSelect();

            today_date.setText(week_menus[3][select]);
            food_day.setText("[ " + week_menus[4][select] + " ]");

            if (!week_menus[numbers][select].isEmpty() && !Objects.equals(week_menus[numbers][select], "등록된 식단내용이(가) 없습니다.")) {
                List<String> menus = Arrays.asList(week_menus[numbers][select].replace(",", "").split(" "));
                menus.forEach(menu -> food_menu.addView(makeMenu(menu)));
            } else {
                food_menu.addView(makeMenu("밥 없어요~!!"));
            }
            if (today_select == select) {
                likes_container.setVisibility(View.VISIBLE);
                ad_container.setVisibility(View.GONE);
            } else if (today_select != -1) {
                likes_container.setVisibility(View.GONE); // 이것으로 가시성을 INVISIBLE로 설정합니다.
                ad_container.setVisibility(View.VISIBLE);
            }
            updateButtonWidths();
        });
    }

    private void handleSelect() {
        try {
            if (select < 0) {
                throw new ArrayIndexOutOfBoundsException();
            } else if (select >= 7) {
                select--;
                Toast.makeText(getApplicationContext(), "마지막 페이지 입니다.", Toast.LENGTH_SHORT).show();
            }
        } catch (ArrayIndexOutOfBoundsException exception) {
            select = 0;
            Toast.makeText(getApplicationContext(), "마지막 페이지 입니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private TextView makeMenu(String content) {
        TextView tv = new TextView(this);
        tv.setText(content);
        tv.setTextColor(Color.parseColor("#000000"));
        tv.setGravity(Gravity.CENTER);
        tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        tv.setIncludeFontPadding(false);
        tv.setTypeface(getResources().getFont(R.font.scd5));
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT
                , LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        param.leftMargin = 20;
        param.rightMargin = 20;
        tv.setLayoutParams(param);
        int minTextSize = 20;
        int maxTextSize = 23;
        int stepGranularity = 1;
        tv.setAutoSizeTextTypeUniformWithConfiguration(minTextSize, maxTextSize, stepGranularity, TypedValue.COMPLEX_UNIT_SP);


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
        rdb.setLayoutParams(new LinearLayout.LayoutParams(0, 160, 1));
        rdb.setOnClickListener(view -> Temporary_food_room_num.set(number));
        return rdb;
    }

    public static void saveWeekMenus(Context context, String[][] weekMenus) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("pref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        Gson gson = new Gson();
        String json = gson.toJson(weekMenus);

        editor.putString("week_menus", json);
        editor.apply();
    }

    @Override
    public boolean onDown(@NonNull MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(@NonNull MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(@NonNull MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(@NonNull MotionEvent e1, @NonNull MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(@NonNull MotionEvent e) {

    }

    @Override
    public boolean onFling(@NonNull MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }
}