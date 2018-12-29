package com.example.wnsvy.kakaocalorie.Service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import com.example.wnsvy.kakaocalorie.Application.GlobalApplication;
import com.example.wnsvy.kakaocalorie.Utils.Logger;
import com.firebase.jobdispatcher.JobParameters;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import com.example.wnsvy.kakaocalorie.R;
import com.firebase.jobdispatcher.SimpleJobService;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.FitnessOptions;
import java.util.Calendar;


public class UpdateDbService extends SimpleJobService{

    @Override
    public int onRunJob(JobParameters job) {
        Log.d("잡서비스 호출", job.getService());

        FitnessOptions fitnessOptions  = GlobalApplication.getGlobalApplicationContext().setFitnessOptions();
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        // 4시~5시 사이에 카운트 리셋 및 DB 업데이트
        if(hour > 22 && hour < 24){
            if (GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(this), fitnessOptions)) {
                sendNotification("KakaoCalorie", "오늘의 활동 정보 업데이트");
                GlobalApplication.getGlobalApplicationContext().readHIstoryData(null,null,null,null,null,null,"upload");
                // 거리 및 걸음수 Read && Upload
            }
        }
        return 0;
    }

    public void sendNotification(String title, String message) {
        Calendar calendar = Calendar.getInstance();
        long time = calendar.getTimeInMillis();
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        //If on Oreo then notification required a notification channel.
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            if (notificationManager == null) {
                Logger.e("Failed to fetch NotificationManager from context.");
                return;
            }
            NotificationChannel channel = new NotificationChannel("default", "Default", NotificationManager.IMPORTANCE_DEFAULT);
            channel.enableVibration(false); // 진동 삭제
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder notification = new NotificationCompat.Builder(getApplicationContext(), "default")
                .setContentTitle(title)
                .setContentText(message)
                .setVibrate(null)
                .setSmallIcon(R.mipmap.ic_foots_round);

        notificationManager.notify((int) time, notification.build()); // 노티 아이디를 다르게 해주면 덮어쓰지 않고 쌓임
    }

}