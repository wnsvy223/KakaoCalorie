package com.example.wnsvy.kakaocalorie.Activity;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.wnsvy.kakaocalorie.R;
import com.example.wnsvy.kakaocalorie.Utils.SemiCircleProgress;
import com.stfalcon.multiimageview.MultiImageView;
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

        downloadImageTask = new DownloadImageTask(profileMe);
        downloadImageTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        // THREAD_POOL_EXECUTOR : 동시 실행 처리
        // SERIAL_EXECUTOR : 순차 실행 처리
    }

    public void initView(){
        profileCouple = findViewById(R.id.profile_couple);
        profileCouple.setShape(MultiImageView.Shape.CIRCLE);
        semiCircleManDistance = findViewById(R.id.semiCircleManDistance);
        semiCircleWomanDistance = findViewById(R.id.semiCircleWomanDistance);
        semiCircleManStep = findViewById(R.id.semiCircleManStep);
        semiCircleWomanStep = findViewById(R.id.semiCircleWomanStep);
        semiCircleManCalorie = findViewById(R.id.semiCircleManCalorie);
        semiCircleWomanCalorie = findViewById(R.id.semiCircleWomanCalorie);
    }


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
            /*
           switch (urls.length){
               case 1:
                   profileCouple.addImage(getBitmapFromURL(urls[0]));
                   profileCouple.addImage(BitmapFactory.decodeResource(getResources(), R.drawable.thumb_story));
                   break;
               case 2:
                   profileCouple.addImage(getBitmapFromURL(urls[0]));
                   profileCouple.addImage(getBitmapFromURL(urls[1]));
                   break;
               default:
           }
           */
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
