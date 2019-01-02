package com.example.wnsvy.kakaocalorie.Activity;

import android.Manifest;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.wnsvy.kakaocalorie.Application.GlobalApplication;
import com.example.wnsvy.kakaocalorie.Fragment.RankFragment;
import com.example.wnsvy.kakaocalorie.Interface.AsyncTaskEventListener;
import com.example.wnsvy.kakaocalorie.Model.RankModel;
import com.example.wnsvy.kakaocalorie.R;
import com.example.wnsvy.kakaocalorie.Service.JsonPostAsyncTask;
import com.example.wnsvy.kakaocalorie.Service.UpdateDbService;
import com.example.wnsvy.kakaocalorie.Utils.JobDispatcherUtils;
import com.example.wnsvy.kakaocalorie.Utils.Logger;
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
import com.kakao.friends.AppFriendContext;
import com.kakao.friends.response.AppFriendsResponse;
import com.kakao.friends.response.model.AppFriendInfo;
import com.kakao.kakaotalk.callback.TalkResponseCallback;
import com.kakao.kakaotalk.v2.KakaoTalkService;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import tk.jamun.elements.circularimageview.CircularImageView;



public class UserDataActivity extends AppCompatActivity{

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
    private JsonPostAsyncTask jsonPostAsyncTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userdata);

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
                Intent intent = new Intent(UserDataActivity.this, LogActivity.class);
                startActivity(intent);
            }
        });

        distanceLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserDataActivity.this, LogActivity.class);
                startActivity(intent);
            }
        });

        calorieLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserDataActivity.this, LogActivity.class);
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
                requestFriends();
            }
        });
    }

    public void requestFriends() {
        AppFriendContext appFriendContext = new AppFriendContext(true, 0, 100, "asc");
        KakaoTalkService.getInstance().requestAppFriends(appFriendContext,
                new TalkResponseCallback<AppFriendsResponse>() {
                    @Override
                    public void onNotKakaoTalkUser() {
                        Toast.makeText(getApplicationContext(),"카카오톡 가입 유저가 아닙니다.",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onSessionClosed(ErrorResult errorResult) {
                        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                        startActivity(intent);
                    }

                    @Override
                    public void onNotSignedUp() {
                        //redirectSignupActivity();
                    }

                    @Override
                    public void onFailure(ErrorResult errorResult) {
                        Logger.e("onFailure: " + errorResult.toString());
                    }

                    @Override
                    public void onSuccess(AppFriendsResponse result) {
                        // 친구 목록
                        Logger.e("Friends: " + result.getFriends().toString());
                        // context의 beforeUrl과 afterUrl이 업데이트 된 상태.

                        SharedPreferences sharedPreferences = getSharedPreferences("user",MODE_PRIVATE );
                        long myToken = sharedPreferences.getLong("token", 0);

                        List<AppFriendInfo> list = result.getFriends();
                        List<Long> tokenList = new ArrayList<>();
                        tokenList.add(myToken);
                        for (int i=0; i<list.size(); i++){
                            long token = list.get(i).getId();
                            tokenList.add(token);
                        }
                        getRank(tokenList); // 카카오 친구 API로부터 받아온 친구들의 토큰값 리스트를 서버로 전송하여 해당 토큰을 가진 유저의 데이터를 받아옴
                    }
                });
    }


    public void getRank(List<Long> token){
        JSONObject jsonObject = new JSONObject();
        String restUrl = "http://192.168.0.29:3000/show-rank";
        try {
            jsonObject.accumulate("friendTokenList", token); // 서버로 보낼 친구들 토큰 리스트
            jsonPostAsyncTask = new JsonPostAsyncTask(restUrl, jsonObject
                    , getApplicationContext(), new AsyncTaskEventListener<String>() {
                @Override
                public void onSuccess(String res) {
                    Log.d("제이슨", String.valueOf(res));
                    try {
                        JSONArray jsonArray = new JSONArray(res);  // 서버로 부터 받아온 친구들의 정렬된 데이터 Json배열.
                        ArrayList<RankModel> rankList = new ArrayList<>();
                        for(int i=0; i<jsonArray.length(); i++){ // 생성된 jsonArray를 순회하며 각 키값에 접근하여 데이터를 가져옴.
                            JSONObject object = jsonArray.getJSONObject(i);
                            String id = object.getString("userID");
                            String email = object.getString("userEmail");
                            String photo = object.getString("userPhoto");
                            String footstep = object.getString("footstep");
                            String distance = object.getString("distance");
                            RankModel rankModel = new RankModel(id,email,photo,footstep,distance);
                            rankList.add(rankModel);
                        }
                        FragmentManager fragmentManager = getFragmentManager();
                        RankFragment rankFragment = new RankFragment();
                        rankFragment.setStyle(DialogFragment.STYLE_NORMAL,R.style.CustomDialog);
                        Bundle bundle = new Bundle(1);
                        bundle.putParcelableArrayList("friendProfile",rankList);
                        rankFragment.setArguments(bundle);
                        rankFragment.show(fragmentManager,"Rank");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    Log.d("AsyncTaskErr","Failed send to server"+ restUrl);
                }
            });
            jsonPostAsyncTask.execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    @Override
    protected void onResume() {
        super.onResume();
        scheduleJob(); // 잡서비스 설정 및 구동
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

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

