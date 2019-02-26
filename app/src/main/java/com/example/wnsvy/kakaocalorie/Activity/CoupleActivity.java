package com.example.wnsvy.kakaocalorie.Activity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatDrawableManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wnsvy.kakaocalorie.Application.GlobalApplication;
import com.example.wnsvy.kakaocalorie.Interface.AsyncTaskEventListener;
import com.example.wnsvy.kakaocalorie.R;
import com.example.wnsvy.kakaocalorie.Service.JsonGetAsyncTask;
import com.example.wnsvy.kakaocalorie.Utils.SemiCircleProgress;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataType;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;
import com.stfalcon.multiimageview.MultiImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class CoupleActivity extends AppCompatActivity {

    private SemiCircleProgress semiCircleManDistance;
    private SemiCircleProgress semiCircleWomanDistance;
    private SemiCircleProgress semiCircleManStep;
    private SemiCircleProgress semiCircleWomanStep;
    private SemiCircleProgress semiCircleManCalorie;
    private SemiCircleProgress semiCircleWomanCalorie;
    private MultiImageView profileCouple;
    private DownloadImageTask downloadImageTask;
    private static final int REQUEST_OAUTH_REQUEST_CODE = 1;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 2;
    private JsonGetAsyncTask jsonGetAsyncTask;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_couple);

        initView();
        semiCircleManDistance.setProgressWithAnimation(2000,130);
        semiCircleWomanDistance.setProgressWithAnimation(2000,80);
        semiCircleManStep.setProgressWithAnimation(2000,110);
        semiCircleWomanStep.setProgressWithAnimation(2000,30);
        semiCircleManCalorie.setProgressWithAnimation(2000,180);
        semiCircleWomanCalorie.setProgressWithAnimation(2000,120);
        // progress는 180도 값이 max값. 따라서 max값이 10000일 경우를 비례식으로 계산하면 원하는 값일때 각 호의 progress값을 구할수있음

        //String profileMe = getIntent().getStringExtra("myProfile");
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("user",MODE_PRIVATE );
        String profileMe = sharedPreferences.getString("profileUrl", "");
        String email = sharedPreferences.getString("email", "");

        downloadImageTask = new DownloadImageTask(profileMe);
        downloadImageTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        // THREAD_POOL_EXECUTOR : 동시 실행 처리
        // SERIAL_EXECUTOR : 순차 실행 처리

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
    }

    public void initView(){
        profileCouple = findViewById(R.id.profile_couple);
        profileCouple.setShape(MultiImageView.Shape.CIRCLE);
        profileCouple.bringToFront();
        semiCircleManDistance = findViewById(R.id.semiCircleManDistance);
        semiCircleWomanDistance = findViewById(R.id.semiCircleWomanDistance);
        semiCircleManStep = findViewById(R.id.semiCircleManStep);
        semiCircleWomanStep = findViewById(R.id.semiCircleWomanStep);
        semiCircleManCalorie = findViewById(R.id.semiCircleManCalorie);
        semiCircleWomanCalorie = findViewById(R.id.semiCircleWomanCalorie);

    }

    private void showDialog(){
        final EditText edittext = new EditText(this);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("AlertDialog Title");
        builder.setMessage("AlertDialog Content");
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

    private void getCoupleData(String coupleEmail){
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
                }

                @Override
                public void onFailure(Exception e) {

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
    private class DownloadImageTask extends AsyncTask<String, Void, Void> {
        //MultiImageView bmImage;
        String bmImage;


        public DownloadImageTask(String bmImage) {
            this.bmImage = bmImage;
        }

        protected Void doInBackground(String... urls) {

            profileCouple.addImage(getBitmapFromURL(bmImage));
            profileCouple.addImage(BitmapFactory.decodeResource(getResources(), R.drawable.thumb_story));

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
