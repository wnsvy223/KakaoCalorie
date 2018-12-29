package com.example.wnsvy.kakaocalorie.Service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.wnsvy.kakaocalorie.R;
import com.example.wnsvy.kakaocalorie.Utils.Logger;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataUpdateRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import androidx.work.Result;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class UpdateWorker extends Worker {

    public static final String TAG = "GoogleFit FootStep Update Service";
    public Context context;
    private WorkerParameters workerParams;

    public UpdateWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
        this.workerParams = workerParams;
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d("잡서비스 호출", String.valueOf(workerParams));

        FitnessOptions fitnessOptions = FitnessOptions.builder()
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_WRITE)
                .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_WRITE)
                .build();

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat night = new SimpleDateFormat("HH", Locale.KOREA);
        String time = night.format(calendar.getTime());
        if(time.equals("02") || time.equals("03") || time.equals("04")){ // 2시~5시 사이에 카운트 리셋 및 DB 업데이트
            if (GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(getApplicationContext()), fitnessOptions)) {
                sendNotification("JobDispatcher Reset Trigger!", "걸음수 갱신되었습니다.");
            }
        }
        sendNotification("테스트", "테스트");
        return Result.success();
    }

    private Task<Void> resetData() {
        // Create a new dataset and update request.
        DataSet dataSet = updateFitnessData(); // 스탭카운트 0 으로 리셋
        long startTime = 0;
        long endTime = 0;

        // Get the start and end times from the dataset.
        for (DataPoint dataPoint : dataSet.getDataPoints()) {
            startTime = dataPoint.getStartTime(TimeUnit.MILLISECONDS);
            endTime = dataPoint.getEndTime(TimeUnit.MILLISECONDS);
        }

        // [START update_data_request]
        Log.i(TAG, "Updating the dataset in the History API.");

        DataUpdateRequest request =
                new DataUpdateRequest.Builder()
                        .setDataSet(dataSet)
                        .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
                        .build();

        // Invoke the History API to update data.
        return Fitness.getHistoryClient(getApplicationContext(), GoogleSignIn.getLastSignedInAccount(getApplicationContext()))
                .updateData(request)
                .addOnCompleteListener(
                        new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    // At this point the data has been updated and can be read.
                                    Log.i(TAG, "Data update was successful.");
                                } else {
                                    Log.e(TAG, "There was a problem updating the dataset.", task.getException());
                                }
                            }
                        });
    }

    private DataSet updateFitnessData() {
        Log.i(TAG, "Creating a new data update request.");

        // [START build_update_data_request]
        // Set a start and end time for the data that fits within the time range
        // of the original insertion.
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        //cal.add(Calendar.WEEK_OF_YEAR, -1); // 1주 전
        cal.add(Calendar.DATE, -1); // 1일 전
        long startTime = cal.getTimeInMillis();

        // Create a data source
        DataSource dataSource =
                new DataSource.Builder()
                        .setAppPackageName(getApplicationContext())
                        .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
                        .setStreamName(TAG + " - step count")
                        .setType(DataSource.TYPE_RAW)
                        .build();

        // Create a new data set
        int stepCountDelta = 0; //스탭카운트 0으로 리셋해서 구글핏api에 업데이트
        DataSet dataSet = DataSet.create(dataSource);
        // For each data point, specify a start time, end time, and the data value -- in this case,
        // the number of new steps.
        DataPoint dataPoint =
                dataSet.createDataPoint().setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS);
        dataPoint.getValue(Field.FIELD_STEPS).setInt(stepCountDelta);
        dataSet.add(dataPoint);
        // [END build_update_data_request]

        return dataSet;
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
