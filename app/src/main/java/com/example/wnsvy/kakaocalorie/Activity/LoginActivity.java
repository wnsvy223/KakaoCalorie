package com.example.wnsvy.kakaocalorie.Activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.wnsvy.kakaocalorie.Application.GlobalApplication;
import com.example.wnsvy.kakaocalorie.Model.RankModel;
import com.example.wnsvy.kakaocalorie.R;
import com.example.wnsvy.kakaocalorie.Service.JsonGetAsyncTask;
import com.example.wnsvy.kakaocalorie.Service.UpdateDbService;
import com.example.wnsvy.kakaocalorie.Utils.JobDispatcherUtils;
import com.example.wnsvy.kakaocalorie.Utils.PermissionUtils;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataType;
import com.kakao.auth.Session;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import tk.jamun.elements.circularimageview.CircularImageView;



public class LoginActivity extends AppCompatActivity{

    public static final String TAG = "BasicHistoryApi";
    private static final int REQUEST_OAUTH_REQUEST_CODE = 1;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 2;
    private GoogleApiClient mGoogleApiClient;
    private CircularProgressBar circularProgressBar;
    private CircularProgressBar distanceProgressBar;
    private CircularProgressBar calorieProgressBar;
    public Toolbar toolbar;
    public CircularImageView profileImgae;
    public TextView textId;
    public TextView textEmail;
    private FirebaseJobDispatcher mDispatcher;;
    private static final String JOB_TAG = "my-unique-tag";
    public TextView distance;
    public TextView stepCount;
    public TextView calorie;
    public ImageButton distanceLog;
    public ImageButton calorieLog;
    public ImageButton stepLog;
    private final String CUSTOM_ADAPTER_IMAGE = "image";
    private final String CUSTOM_ADAPTER_TEXT = "text";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initView();

        mDispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this));
        //mDispatcher.cancel(JOB_TAG); // 모든 예약된 Job 취소

        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.baseline_keyboard_arrow_left_black_18dp);
        toolbar.setBackgroundColor(ContextCompat.getColor(this,R.color.color6));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sessionLogOut();
            }
        });

        Intent intent= getIntent();
        String email = intent.getStringExtra("email");
        String id = intent.getStringExtra("id");
        String imageUrl = intent.getStringExtra("profileImage");

        Glide.with(getApplicationContext()).load(imageUrl).apply(new RequestOptions().override(200,200)).into(profileImgae);
        textId.setText(id);
        textEmail.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.color6));
        textEmail.setText(email);

        stepLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, LogActivity.class);
                startActivity(intent);
            }
        });

        distanceLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, LogActivity.class);
                startActivity(intent);
            }
        });

        calorieLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, LogActivity.class);
                startActivity(intent);
            }
        });

        FitnessOptions fitnessOptions  = GlobalApplication.getGlobalApplicationContext().setFitnessOptions(); // 구글핏 클라이언트 옵션 세팅
        if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(this), fitnessOptions)) {
            GoogleSignIn.requestPermissions(this, REQUEST_OAUTH_REQUEST_CODE, GoogleSignIn.getLastSignedInAccount(this), fitnessOptions);
        } else {
            GlobalApplication.getGlobalApplicationContext().readHIstoryData(circularProgressBar,stepCount,distanceProgressBar,distance,calorieProgressBar,calorie,"display");
            GlobalApplication.getGlobalApplicationContext().getFitnessRecord(DataType.TYPE_STEP_COUNT_DELTA);
            GlobalApplication.getGlobalApplicationContext().getFitnessRecord(DataType.TYPE_DISTANCE_DELTA);
            GlobalApplication.getGlobalApplicationContext().getFitnessRecord(DataType.TYPE_CALORIES_EXPENDED);
        }

        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED){
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, false);
        } // 위치 권한 요청

        profileImgae.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JsonGetAsyncTask jsonGetAsyncTask =  new JsonGetAsyncTask("http://192.168.0.29:3000/show-all-data",null);
                // 노드서버 순위정보 rest url 호출
                try {
                    String result = jsonGetAsyncTask.execute().get(); // AsyncTask의 결과값을 문자열로 반환. -> 서버응답이 지연될경우 UI블럭됨
                    try {
                        JSONArray jsonArray = new JSONArray(result); // 반환된 문자열을 jsonArray 형태로 받아서 json object들의 키값에 접근할 수 있도록 객체 생성.
                        ArrayList<String> idList = new ArrayList<>();
                        ArrayList<String> photoList = new ArrayList<>();
                        String[] arrId = {};
                        String[] arrPhoto = {};
                        for(int i=0; i<jsonArray.length(); i++){ // 생성된 jsonArray를 순회하며 각 키값에 접근하여 데이터를 가져옴.
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            String id = jsonObject.getString("userEmail");
                            String photo = jsonObject.getString("userPhoto");
                            //String footstep = jsonObject.getString("footstep");
                            //String distance = jsonObject.getString("distance");
                            //Log.d("파싱값(아이디)", id);
                            //Log.d("파싱값(사진)", photo);
                            //Log.d("파싱값(걸음)", footstep);
                            //Log.d("파싱값(거리)", distance);
                            //RankModel rankModel = new RankModel(id,photo,footstep,distance);
                            //Log.d("모델클래스", String.valueOf(rankModel));
                            idList.add(id);
                            photoList.add(photo);
                            arrId = idList.toArray(new String[idList.size()]);
                            arrPhoto = photoList.toArray(new String[photoList.size()]);

                        }
                        showLIstDialog(arrId,arrPhoto);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void showLIstDialog(String[] arrId, String[] arrPhoto){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.mipmap.ic_foots_round);
        builder.setTitle("오늘의 운동왕");
        List<Map<String,Object>> dialogItemLIst = new ArrayList<Map<String,Object>>();
        int listTimeLen = arrId.length;
        for(int i=0; i<listTimeLen; i++){
            Map<String, Object> itemMap = new HashMap<String,Object>();
            itemMap.put(CUSTOM_ADAPTER_TEXT,arrId[i]);
            itemMap.put(CUSTOM_ADAPTER_IMAGE,arrPhoto[i]);
            dialogItemLIst.add(itemMap);
        }
        Log.d("테스트", String.valueOf(dialogItemLIst));
        SimpleAdapter simpleAdapter = new SimpleAdapter(this,dialogItemLIst,
            R.layout.alert_dialog_list,
            new String[]{CUSTOM_ADAPTER_IMAGE, CUSTOM_ADAPTER_TEXT},
            new int[]{R.id.alertDialogItemImageView,R.id.alertDialogItemTextView}
        );
        builder.setAdapter(simpleAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
       builder.create();
       builder.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        scheduleJob(); // 잡서비스 설정 및 구동
    }

    public void initView(){
        toolbar = findViewById(R.id.toolbar);
        profileImgae = findViewById(R.id.profileImage);
        textId = findViewById(R.id.textId);
        circularProgressBar = findViewById(R.id.circularProgressbar);
        distanceProgressBar = findViewById(R.id.distanceProgress);
        calorieProgressBar = findViewById(R.id.calorieProgress);
        textEmail = findViewById(R.id.textEmail);
        distance = findViewById(R.id.distance);
        stepCount = findViewById(R.id.stepCount);
        calorie = findViewById(R.id.calorie);
        distanceLog = findViewById(R.id.distancelog);
        calorieLog = findViewById(R.id.calorielog);
        stepLog = findViewById(R.id.steplog);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_OAUTH_REQUEST_CODE) {
                GlobalApplication.getGlobalApplicationContext().readHIstoryData(circularProgressBar,stepCount,distanceProgressBar,distance,calorieProgressBar,calorie,"distplay");
                GlobalApplication.getGlobalApplicationContext().getFitnessRecord(DataType.TYPE_STEP_COUNT_DELTA);
                GlobalApplication.getGlobalApplicationContext().getFitnessRecord(DataType.TYPE_DISTANCE_DELTA);
                GlobalApplication.getGlobalApplicationContext().getFitnessRecord(DataType.TYPE_CALORIES_EXPENDED);
            }
        }
    }

    private void scheduleJob(){
        Job myJob = mDispatcher.newJobBuilder()
                .setTag(JOB_TAG)
                .setRecurring(true)
                .setLifetime(Lifetime.FOREVER)
                .setService(UpdateDbService.class)
                .setTrigger(JobDispatcherUtils.periodicTrigger(60*60, 1)) // 1시간 주기
                .setReplaceCurrent(false) // 같은 이름의 태그로 지정된 작업 덮어쓰기
                .setRetryStrategy(RetryStrategy.DEFAULT_LINEAR)
                .build();

        mDispatcher.mustSchedule(myJob);
    }

    private void cancelJob(String jobTag) {
        if ("".equals(jobTag)) {
            mDispatcher.cancelAll();
        } else {
            mDispatcher.cancel(jobTag);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.logOut:
                sessionLogOut();
                break;
            case R.id.friendList:
                friendListActivity();
                break;
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    private void friendListActivity(){
        Intent intent = new Intent(this,FriendActivity.class);
        startActivity(intent);
        // 친구목록은 카카오에 검수신청 해서 승인되면 실제 카카오톡 친구 목록 불러올 수 있음
        // 그 전 테스트는 카카오API 개발자 사이트에서 직접 해당 유저를 팀에 추가해야 조회할 수 있음
    }

    private void sessionLogOut(){
        UserManagement.getInstance().requestLogout(new LogoutResponseCallback() {
            @Override
            public void onCompleteLogout() {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                Session.getCurrentSession().close();
                finish();
            }
        });
    }

}

