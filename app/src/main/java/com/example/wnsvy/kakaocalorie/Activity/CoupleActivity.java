package com.example.wnsvy.kakaocalorie.Activity;


import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.wnsvy.kakaocalorie.Application.GlobalApplication;
import com.example.wnsvy.kakaocalorie.Interface.AsyncTaskEventListener;
import com.example.wnsvy.kakaocalorie.R;
import com.example.wnsvy.kakaocalorie.Service.JsonGetAsyncTask;
import com.example.wnsvy.kakaocalorie.Utils.SemiCircleProgress;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataType;
import com.stfalcon.multiimageview.MultiImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class CoupleActivity extends AppCompatActivity {

    public SemiCircleProgress semiCircleMeDistance;
    public SemiCircleProgress semiCircleOtherDistance;
    public SemiCircleProgress semiCircleMeStep;
    public SemiCircleProgress semiCircleOtherStep;
    public SemiCircleProgress semiCircleMeCalorie;
    public SemiCircleProgress semiCircleOtherCalorie;
    public TextView stepText;
    public TextView distanceText;
    public TextView calorieText;
    public TextView stepOtherText;
    public TextView distanceOtherText;
    public TextView calorieOtherText;
    public MultiImageView profileCouple;
    private DownloadImageTask downloadImageTask;
    private static final int REQUEST_OAUTH_REQUEST_CODE = 1;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 2;
    private JsonGetAsyncTask jsonGetAsyncTask;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_couple);

        initView();
        semiCircleMeDistance.setProgressWithAnimation(2000,130);
        semiCircleOtherDistance.setProgressWithAnimation(2000,80);
        semiCircleMeCalorie.setProgressWithAnimation(2000,180);
        semiCircleOtherCalorie.setProgressWithAnimation(2000,120);


        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("user",MODE_PRIVATE );
        String profileMe = sharedPreferences.getString("profileUrl", "");
        String email = sharedPreferences.getString("email", "");
        String profileCoupleUser = sharedPreferences.getString("couplePhoto", "");

        List<String> coupleProfileList = new ArrayList<>();
        coupleProfileList.add(profileMe);
        coupleProfileList.add(profileCoupleUser);
        if(!TextUtils.isEmpty(profileCoupleUser) && !TextUtils.isEmpty(profileMe)) {
            downloadImageTask = new DownloadImageTask(coupleProfileList);
            downloadImageTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            // THREAD_POOL_EXECUTOR : 병렬 처리
            // SERIAL_EXECUTOR : 순차 처리
        }
        profileCouple.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 커플상대의 데이터 가져오기위한 이메일 입력 다이얼로그
                showDialog();
            }
        });

        FitnessOptions fitnessOptions  = GlobalApplication.getGlobalApplicationContext().setFitnessOptions(); // 구글핏 클라이언트 옵션 세팅
        if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(getApplicationContext()), fitnessOptions)) {
            GoogleSignIn.requestPermissions(this, REQUEST_OAUTH_REQUEST_CODE, GoogleSignIn.getLastSignedInAccount(getApplicationContext()), fitnessOptions);
        } else {
            GlobalApplication.getGlobalApplicationContext().getFitnessRecord(DataType.TYPE_STEP_COUNT_DELTA);
            GlobalApplication.getGlobalApplicationContext().getFitnessRecord(DataType.TYPE_DISTANCE_DELTA);
            GlobalApplication.getGlobalApplicationContext().getFitnessRecord(DataType.TYPE_CALORIES_EXPENDED);
            GlobalApplication.getGlobalApplicationContext().getFitnessRecord(DataType.TYPE_LOCATION_SAMPLE);
            GlobalApplication.getGlobalApplicationContext().readHIstoryData(null,null,null,null,null,null,"upload");
            GlobalApplication.getGlobalApplicationContext().sendServer(email,getApplicationContext());
        }

        String couple = sharedPreferences.getString("coupleEmail", "");
        getData(couple);

    }

    public void initView(){
        profileCouple = findViewById(R.id.profile_couple);
        profileCouple.setShape(MultiImageView.Shape.CIRCLE);
        profileCouple.bringToFront();
        semiCircleMeDistance = findViewById(R.id.semiCircleManDistance);
        semiCircleOtherDistance = findViewById(R.id.semiCircleWomanDistance);
        semiCircleMeStep = findViewById(R.id.semiCircleManStep);
        semiCircleOtherStep = findViewById(R.id.semiCircleWomanStep);
        semiCircleMeCalorie = findViewById(R.id.semiCircleManCalorie);
        semiCircleOtherCalorie = findViewById(R.id.semiCircleWomanCalorie);
        stepText = findViewById(R.id.stepCount);
        distanceText = findViewById(R.id.distance);
        calorieText = findViewById(R.id.calorie);
        stepOtherText = findViewById(R.id.stepCountOther);
        distanceOtherText = findViewById(R.id.distanceOther);
        calorieOtherText = findViewById(R.id.calorieOther);
    }

    private void showDialog(){
        final EditText edittext = new EditText(this);

        AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.CustomDialogTheme);
        builder.setTitle("KakaoCalorie 메이트 검색");
        builder.setMessage("함께할 상대방의 Email을 입력해주세요.");
        builder.setView(edittext);
        builder.setPositiveButton("입력",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        getCoupleData(edittext.getText().toString());
                        //이메일 입력받아서 커플상대 데이터 조회 함수 호출
                    }
                });
        builder.setNegativeButton("취소",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.show();
    }

    private void getData(String coupleEmail){
        String restUrl = "http://192.168.0.29:3000/usersData/getUser?userEmail=" + coupleEmail;
        // get방식으로 url에 커플상대유저의 이메일을 전송하여 해당 유저의 데이터를 서버로부터 받아오도록함.

        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("user",MODE_PRIVATE );
        String footstepMe = sharedPreferences.getString("step","");
        String distanceMe = sharedPreferences.getString("distance","");
        String calMe = sharedPreferences.getString("calorie","");

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("coupleEmail", coupleEmail);
            jsonGetAsyncTask = new JsonGetAsyncTask(restUrl, jsonObject, new AsyncTaskEventListener<String>() {
                @Override
                public void onSuccess(String  res) {
                    JSONArray jsonArray = null;  // 서버로 부터 받아온 친구들의 정렬된 데이터 Json배열.
                    try {
                        jsonArray = new JSONArray(res);
                        for(int i=0; i<jsonArray.length(); i++){ // 생성된 jsonArray를 순회하며 각 키값에 접근하여 데이터를 가져옴.
                            JSONObject object = jsonArray.getJSONObject(i);
                            String footstep = object.getString("footstep");
                            String distance = object.getString("distance");
                            String calorie = object.getString("calorie");

                            semiCircleMeStep.setProgressWithAnimation(2000,(Integer.parseInt(footstepMe) * 180) / 10000);
                            semiCircleOtherStep.setProgressWithAnimation(2000, (Integer.parseInt(footstep) * 180) / 10000);
                            stepOtherText.setText(footstep);
                            stepText.setText(footstepMe);

                            int index = distance.indexOf("K");
                            String result = distance.substring(0, index);
                            String resultMe = distanceMe.substring(0, index);
                            float f = Float.parseFloat(result) * 1000;
                            float f2 = Float.parseFloat(resultMe) * 1000;
                            semiCircleOtherDistance.setProgressWithAnimation(2000, ((int) f * 180) / 10000 );
                            semiCircleMeDistance.setProgressWithAnimation(2000, ((int) f2 * 180) / 10000 );
                            distanceOtherText.setText(result);
                            distanceText.setText(resultMe);

                            int idx = calMe.indexOf(".");
                            String resultCal = calMe.substring(0,idx);
                            semiCircleMeCalorie.setProgressWithAnimation(2000, (Integer.parseInt(resultCal) * 180) / 10000);
                            semiCircleOtherCalorie.setProgressWithAnimation(2000,(Integer.parseInt(calorie) * 180) / 10000);
                            calorieText.setText(resultCal);
                            calorieOtherText.setText(calorie);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onFailure(Exception e) {
                    jsonGetAsyncTask.cancel(true);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
        jsonGetAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void getCoupleData(String coupleEmail){
        profileCouple.clear();
        // 커플 상대 이메일 저장
        SharedPreferences sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("coupleEmail", coupleEmail);
        editor.apply();

        String restUrl = "http://192.168.0.29:3000/usersData/getUser?userEmail=" + coupleEmail;
        // get방식으로 url에 커플상대유저의 이메일을 전송하여 해당 유저의 데이터를 서버로부터 받아오도록함.

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("coupleEmail", coupleEmail);
            jsonGetAsyncTask = new JsonGetAsyncTask(restUrl, jsonObject, new AsyncTaskEventListener<String>() {
                @Override
                public void onSuccess(String  res) {
                    Log.d("AsyncTask 응답데이터", String.valueOf(res));

                    SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("user",MODE_PRIVATE );
                    String profileMe = sharedPreferences.getString("profileUrl", "");

                    JSONArray jsonArray = null;
                    try {
                        jsonArray = new JSONArray(res);
                        for(int i=0; i<jsonArray.length(); i++){ // 생성된 jsonArray를 순회하며 각 키값에 접근하여 데이터를 가져옴.
                            JSONObject object = jsonArray.getJSONObject(i);
                            String id = object.getString("userID");
                            String email = object.getString("userEmail");
                            String photo = object.getString("userPhoto");
                            String footstep = object.getString("footstep");
                            String distance = object.getString("distance");
                            String token = object.getString("token");

                            editor.putString("coupleID", id);
                            editor.putString("coupleEmail", email);
                            editor.putString("couplePhoto", photo);
                            editor.putString("coupleFootStep", footstep);
                            editor.putString("coupleDistance", distance);
                            editor.putString("coupleToken", token);
                            editor.apply();

                            if(TextUtils.isEmpty(res)){
                                profileCouple.addImage(BitmapFactory.decodeResource(getResources(), R.drawable.thumb_story));
                                profileCouple.addImage(BitmapFactory.decodeResource(getResources(), R.drawable.thumb_story));
                            }else{
                                List<String> profile = new ArrayList<>();
                                profile.add(profileMe);
                                profile.add(photo);
                                downloadImageTask = new DownloadImageTask(profile);
                                downloadImageTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onFailure(Exception e) {
                    jsonGetAsyncTask.cancel(true);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }

        jsonGetAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Nullable
    public static Bitmap getBitmapFromURL(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(input);
            return bitmap;
        } catch (IOException e) {
            // Log exception
            return null;
        }
    }

    // 이건 시험용 JsonAsyncTask클래스로 커플상대의 데이터 가져와서 넣어주고 나의 데이터는 Application클래스로 부터 받아와서 넣어주도록.
    // post로 커플상대의 토큰값을 보내서 토큰값을 가진 유저의 데이터 받아오기.
    private class DownloadImageTask extends AsyncTask<List<String>, Void, Void> {
        List<String > bmImage;

        public DownloadImageTask(List<String> bmImage) {
            this.bmImage = bmImage;
        }

        @Override
        protected Void doInBackground(List<String>... lists) {
            profileCouple.addImage(getBitmapFromURL(bmImage.get(0)));
            profileCouple.addImage(getBitmapFromURL(bmImage.get(1)));
            return null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try
        {
            if (downloadImageTask.getStatus() == AsyncTask.Status.RUNNING)
            {
                downloadImageTask.cancel(true);
                Log.d("downloadImageTask","Success cancel AsyncTask");
            }
        }
        catch (Exception e)
        {
            Log.d("downloadImageTask", String.valueOf(e));
        }
    }
}
