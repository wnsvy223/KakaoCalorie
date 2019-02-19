/*
  Copyright 2014-2017 Kakao Corp.

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */
package com.example.wnsvy.kakaocalorie.Application;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.util.LruCache;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.example.wnsvy.kakaocalorie.Adapter.KakaoSDKAdapter;
import com.example.wnsvy.kakaocalorie.Interface.AsyncTaskEventListener;
import com.example.wnsvy.kakaocalorie.Service.JsonPostAsyncTask;
import com.example.wnsvy.kakaocalorie.Utils.Logger;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.kakao.auth.KakaoSDK;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.text.DateFormat.getDateInstance;


/**
 * 이미지를 캐시를 앱 수준에서 관리하기 위한 애플리케이션 객체이다.
 * 로그인 기반 샘플앱에서 사용한다.
 *
 * @author MJ
 */
public class GlobalApplication extends Application {
    private ImageLoader imageLoader;
    private static volatile GlobalApplication instance = null;
    public static final String TAG = "GoogleFit FootStep Update Service";
    //private ImageLoader imageLoader;

    /**
     * singleton 애플리케이션 객체를 얻는다.
     * @return singleton 애플리케이션 객체
     */
    public static GlobalApplication getGlobalApplicationContext() {
        if(instance == null)
            throw new IllegalStateException("this application does not inherit com.kakao.GlobalApplication");
        return instance;
    }

    /**
     * 이미지 로더, 이미지 캐시, 요청 큐를 초기화한다.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        KakaoSDK.init(new KakaoSDKAdapter());
        //PushService.init();

        final RequestQueue requestQueue = Volley.newRequestQueue(this);

        ImageLoader.ImageCache imageCache = new ImageLoader.ImageCache() {
            final LruCache<String, Bitmap> imageCache = new LruCache<String, Bitmap>(30);

            @Override
            public void putBitmap(String key, Bitmap value) {
                imageCache.put(key, value);
            }

            @Override
            public Bitmap getBitmap(String key) {
                return imageCache.get(key);
            }
        };

        imageLoader = new ImageLoader(requestQueue, imageCache);

        //createNotificationChannel();
    }

    public FitnessOptions setFitnessOptions(){
        return  FitnessOptions.builder()
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_WRITE)
                .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_WRITE) // 걸음수 옵션
                .addDataType(DataType.TYPE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.AGGREGATE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_DISTANCE_DELTA, FitnessOptions.ACCESS_WRITE)
                .addDataType(DataType.AGGREGATE_DISTANCE_DELTA, FitnessOptions.ACCESS_WRITE) //  이동거리 옵션
                .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.AGGREGATE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_WRITE)
                .addDataType(DataType.AGGREGATE_CALORIES_EXPENDED, FitnessOptions.ACCESS_WRITE) //  칼로리 옵션
                .addDataType(DataType.TYPE_LOCATION_SAMPLE, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.AGGREGATE_LOCATION_BOUNDING_BOX, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_LOCATION_SAMPLE, FitnessOptions.ACCESS_WRITE)
                .addDataType(DataType.AGGREGATE_LOCATION_BOUNDING_BOX, FitnessOptions.ACCESS_WRITE) //  이동 위치 추적 옵션
                .build();
    }

    /*
    public Task<Void> resetData() {
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
        return Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .updateData(request)
                .addOnCompleteListener(
                        new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    // At this point the data has been updated and can be read.
                                    Log.i(TAG, "Data update was successful.(Step Reset)");
                                } else {
                                    Log.e(TAG, "There was a problem updating the dataset.", task.getException());
                                }
                            }
                        });
    }
    */

    /*
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
                        .setAppPackageName(this)
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
    */

    public void getFitnessRecord(DataType dataType) {
        // To create a subscription, invoke the Recording API. As soon as the subscription is
        // active, fitness data will start recording.
        Fitness.getRecordingClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .subscribe(dataType)
                .addOnCompleteListener(
                        new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.i(TAG, dataType +" Data Successfully subscribed!");
                                } else {
                                    Log.w(TAG, dataType +"There was a problem subscribing .", task.getException());
                                }
                            }
                        });
    }

    public static DataReadRequest queryFitnessData() {
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        //cal.add(Calendar.DAY_OF_WEEK, -1); // 1주 전
        //cal.add(Calendar.DATE, -1); // 1일 전
        cal.set(Calendar.HOUR_OF_DAY,0);
        cal.set(Calendar.MINUTE,0);
        cal.set(Calendar.SECOND,0);
        cal.set(Calendar.MILLISECOND,0); // 시간단위 모두 0으로 세팅해서 오늘 날짜의 0시부터 수집된 값 조회
        long startTime = cal.getTimeInMillis();

        DateFormat timeFormat = DateFormat.getTimeInstance();
        DateFormat dateFormat = getDateInstance();
        Log.i(TAG + "FitnessData", "Range Start: " + dateFormat.format(startTime) + " / " + timeFormat.format(startTime));
        Log.i(TAG + "FitnessData", "Range End: " + dateFormat.format(endTime) + " / " + timeFormat.format(endTime));

        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_DISTANCE_DELTA, DataType.AGGREGATE_DISTANCE_DELTA)
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .aggregate(DataType.TYPE_CALORIES_EXPENDED, DataType.AGGREGATE_CALORIES_EXPENDED)
                .aggregate(DataType.TYPE_SPEED, DataType.AGGREGATE_SPEED_SUMMARY)
                .aggregate(DataType.TYPE_LOCATION_SAMPLE, DataType.AGGREGATE_LOCATION_BOUNDING_BOX)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .enableServerQueries()
                .build();

        return readRequest;
    }

    public void readHIstoryData(CircularProgressBar stepProgressBar, TextView stepCount, CircularProgressBar distanceProgressBar, TextView distance, CircularProgressBar calorieProgressBar,TextView calorie,String tag){
        DataReadRequest readRequest = queryFitnessData();

        Fitness.getHistoryClient(this,GoogleSignIn.getLastSignedInAccount(this))
                .readData(readRequest)
                .addOnSuccessListener(new OnSuccessListener<DataReadResponse>() {
                    @Override
                    public void onSuccess(DataReadResponse dataReadResponse) {
                        printFitnessData(dataReadResponse,distance, getApplicationContext(),distanceProgressBar,tag,stepProgressBar,stepCount,calorieProgressBar,calorie);
                    }
                });
    }

    public static void printFitnessData(DataReadResponse dataReadResult, TextView distance , Context context, CircularProgressBar distanceProgressBar, String tag, CircularProgressBar stepProgressBar, TextView stepCount, CircularProgressBar calorieProgressBar, TextView calorie) {
        // [START parse_read_data_result]
        // If the DataReadRequest object specified aggregated data, dataReadResult will be returned
        // as buckets containing DataSets, instead of just DataSets.
        if (dataReadResult.getBuckets().size() > 0) {
            Log.i(TAG, "Number of returned buckets of DataSets is: " + dataReadResult.getBuckets().size());
            for (Bucket bucket : dataReadResult.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                Log.i(TAG, "List size: " + dataSets.size());
                for (DataSet dataSet : dataSets) {
                    if(tag.equals("display")){
                        showFitnessData(dataSet, distance, distanceProgressBar,stepCount,stepProgressBar,calorie,calorieProgressBar);
                    }else{
                        uploadFitnessData(dataSet,context);
                    }
                }
            }
        } else if (dataReadResult.getDataSets().size() > 0) {
            Log.i(TAG, "Number of returned DataSets is: " + dataReadResult.getDataSets().size());
            for (DataSet dataSet : dataReadResult.getDataSets()) {
                if(tag.equals("display")){
                    showFitnessData(dataSet, distance, distanceProgressBar,stepCount,stepProgressBar,calorie,calorieProgressBar);
                }else{
                    uploadFitnessData(dataSet,context);
                }
            }
        }
        // [END parse_read_data_result]
    }

    public static void showFitnessData(DataSet dataSet, TextView distance, CircularProgressBar distanceProgressBar, TextView stepCount, CircularProgressBar stepProgressBar,TextView calorie, CircularProgressBar calorieProgressBar){
        DateFormat timeFormat = DateFormat.getTimeInstance();
        DateFormat dateFormat = DateFormat.getDateInstance();
        for (DataPoint dp : dataSet.getDataPoints()) {
            Log.i(TAG, "Data point:");
            Log.i(TAG, "\tType: " + dp.getDataType().getName());
            Log.i(TAG, "\tStart: " + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)) + " " + timeFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
            Log.i(TAG, "\tEnd: " + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)) + " " + timeFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)));
            for (Field field : dp.getDataType().getFields()) {
                Log.i(TAG, "타입: " + field.getName() + " 값: " + dp.getValue(field));
                switch (field.getName()) {
                    case "distance":
                        String percent = dp.getValue(field).toString();
                        float result = Float.parseFloat(percent);
                        String form = String.valueOf(Math.round(result) / 1000.0);
                        distance.setText(form);
                        distanceProgressBar.setProgressMax(10000);
                        distanceProgressBar.setProgressWithAnimation(result, 500); // 구글핏으로 부터 받은 거리 값 세팅
                        break;
                    case "steps":
                        String percentStep = dp.getValue(field).toString();
                        int resultStep = Integer.parseInt(percentStep);
                        stepProgressBar.setProgressMax(10000); // 프로그래스바 최대값
                        stepProgressBar.setProgressWithAnimation(resultStep, 500); // 구글핏으로 부터 받은 걸음걸이 값 세팅
                        stepCount.setText(percentStep);
                        break;
                    case "calories":
                        String percentCal = dp.getValue(field).toString();
                        int resultCal = (int) Float.parseFloat(percentCal);
                        String formatCal = String.format(String.valueOf(resultCal));
                        calorieProgressBar.setProgressMax(100000);
                        calorieProgressBar.setProgressWithAnimation(resultCal, 500); // 구글핏으로 부터 받은 칼로리 소모값 세팅
                        calorie.setText(formatCal);
                    default:
                }
            }
        }
    }

    private static void uploadFitnessData(DataSet dataSet, Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences("user",MODE_PRIVATE );
        String email = sharedPreferences.getString("email", "");
        SharedPreferences.Editor editor = sharedPreferences.edit();

        DateFormat timeFormat = DateFormat.getTimeInstance();
        DateFormat dateFormat = DateFormat.getDateInstance();
        for (DataPoint dp : dataSet.getDataPoints()) {
            Log.i(TAG, "Data point:");
            Log.i(TAG, "\tType: " + dp.getDataType().getName());
            Log.i(TAG, "\tStart: " + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)) + " " + timeFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
            Log.i(TAG, "\tEnd: " + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)) + " " + timeFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)));
            for (Field field : dp.getDataType().getFields()) {
                Log.i(TAG, "\tField: " + field.getName() + " Value: " + dp.getValue(field));
                if(!TextUtils.isEmpty(email)) {
                    // 서버에 업로드 메소드 sendServer에 사용될 데이터들을 sharedpreference에 저장.
                    switch (field.getName()){
                        case "distance":
                            String percent = dp.getValue(field).toString();
                            float result = Float.parseFloat(percent);
                            float distanceValue = result / 1000;
                            String formatDistance = String.format("%8.1f", distanceValue).trim() + "Km"; // 소수점이동(%8.1f)후 공백제거(trim)
                            editor.putString("distance", formatDistance);
                            editor.apply();
                            break;
                        case "steps":
                            String percentStep = dp.getValue(field).toString();
                            editor.putString("step", percentStep);
                            editor.apply();
                            break;
                        case "calories":
                            String percentCal = dp.getValue(field).toString();
                            editor.putString("calorie", percentCal);
                            editor.apply();
                        default:
                    }
                }
            }
        }
    }

    public void sendServer(String email,Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences("user",MODE_PRIVATE );
        String stepCount = sharedPreferences.getString("step", "");
        String distanceCount = sharedPreferences.getString("distance", "");
        String calorie = sharedPreferences.getString("calorie", "");
        String restUrl = "http://192.168.0.29:3000/send-count";
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("stepCount", stepCount);
            jsonObject.accumulate("distance", distanceCount);
            jsonObject.accumulate("calorie", calorie);
            jsonObject.accumulate("userEmail",  email);
            JsonPostAsyncTask jsonPostAsyncTask =  new JsonPostAsyncTask(restUrl, jsonObject
                    , context
                    , new AsyncTaskEventListener() {
                @Override
                public void onSuccess(Object object) {
                    Logger.e(TAG,"Success send to server." + restUrl);
                }

                @Override
                public void onFailure(Exception e) {
                    Logger.e(TAG,"Failed send to server." + restUrl);
                }
            });
            jsonPostAsyncTask.execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public ImageLoader getImageLoader() {
        return imageLoader;
    }
    /**
     * 이미지 로더를 반환한다.
     * @return 이미지 로더
     */
    /*

    */
    /**
     * 애플리케이션 종료시 singleton 어플리케이션 객체 초기화한다.
     */
    @Override
    public void onTerminate() {
        super.onTerminate();
        instance = null;
    }

    /**
     * For API level above or equalt o 26, Separate notification
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm == null) {
                Logger.e("Failed to fetch NotificationManager from context.");
                return;
            }
            String channelId = "kakao_push_channel";
            String channelName = "Kakao SDK Push";
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
            channel.enableLights(false);
            channel.setLightColor(Color.RED);
            channel.enableVibration(false);
            nm.createNotificationChannel(channel);
            Logger.d("successfully created a notification channel.");
        }
    }

}
