package com.example.wnsvy.kakaocalorie.Fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.wnsvy.kakaocalorie.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.tasks.OnSuccessListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.text.DateFormat.getDateInstance;


public class WeekFragment extends Fragment {

    public static String TAG = "Week Fragment";
    public ArrayList<Float> stepList;
    public ArrayList<Integer> timeLine_s;
    public ArrayList<Float> distanceList;
    public ArrayList<Integer> timeLine_d;
    public ArrayList<Float> caloList;
    public ArrayList<Integer> timeLine_c;
    public BarChart stepChart;
    public BarChart distanceChart;
    public BarChart calorieChart;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_week, container, false);

        initView(view);

        DataReadRequest readRequestWeek = queryFitnessDataWeek();
        readHIstoryData(readRequestWeek,"Week"); // 주간

        return view;
    }

    private void initView(View view){
        stepChart = view.findViewById(R.id.stepChartWeek);
        stepChart.setNoDataText("");
        distanceChart = view.findViewById(R.id.distanceChartWeek);
        distanceChart.setNoDataText("");
        calorieChart = view.findViewById(R.id.calorieChartWeek);
        calorieChart.setNoDataText("");
        stepChart.setDescription("");
        stepChart.animateXY(2000, 2000);
        stepChart.invalidate();
        distanceChart.setDescription("");
        distanceChart.animateXY(2000, 2000);
        distanceChart.invalidate();
        calorieChart.setDescription("");
        calorieChart.animateXY(2000, 2000);
        calorieChart.invalidate();
        stepList = new ArrayList<>();
        timeLine_s = new ArrayList<>();
        distanceList = new ArrayList<>();
        timeLine_d = new ArrayList<>();
        caloList = new ArrayList<>();
        timeLine_c = new ArrayList<>();
    }

    public DataReadRequest queryFitnessDataWeek() { // 1주 전부터 오늘까지의 데이터 조회
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.WEEK_OF_MONTH, -1); // 1주 전
        cal.set(Calendar.HOUR_OF_DAY,24);
        cal.set(Calendar.MINUTE,0);
        cal.set(Calendar.SECOND,0);
        cal.set(Calendar.MILLISECOND,0); // 시간단위 모두 0으로 세팅해서 오늘 날짜의 0시부터 수집된 값 조회
        long startTime = cal.getTimeInMillis();

        DateFormat timeFormat = DateFormat.getTimeInstance();
        DateFormat dateFormat = getDateInstance();
        Log.i(TAG + "Fit(Week)", "Range Start: " + dateFormat.format(startTime) + " / " + timeFormat.format(startTime));
        Log.i(TAG + "Fit(Week)", "Range End: " + dateFormat.format(endTime) + " / " + timeFormat.format(endTime));

        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_DISTANCE_DELTA, DataType.AGGREGATE_DISTANCE_DELTA)
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .aggregate(DataType.TYPE_CALORIES_EXPENDED, DataType.AGGREGATE_CALORIES_EXPENDED)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();

        return readRequest;
    }

    public void readHIstoryData(DataReadRequest readRequest, String date){


        Fitness.getHistoryClient(getContext(),GoogleSignIn.getLastSignedInAccount(getContext()))
                .readData(readRequest)
                .addOnSuccessListener(new OnSuccessListener<DataReadResponse>() {
                    @Override
                    public void onSuccess(DataReadResponse dataReadResponse) {
                        printFitnessData(dataReadResponse, date);
                    }
                });
    }

    public  void printFitnessData(DataReadResponse dataReadResult, String date) {
        // [START parse_read_data_result]
        // If the DataReadRequest object specified aggregated data, dataReadResult will be returned
        // as buckets containing DataSets, instead of just DataSets.
        if (dataReadResult.getBuckets().size() > 0) {
            Log.i(TAG, "Number of returned buckets of DataSets is: " + dataReadResult.getBuckets().size());
            for (Bucket bucket : dataReadResult.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                Log.i(TAG, "List size: " + dataSets);
                for (DataSet dataSet : dataSets) {
                    showFitnessData(dataSet,date,dataReadResult.getBuckets().size());
                }
            }
        } else if (dataReadResult.getDataSets().size() > 0) {
            Log.i(TAG, "Number of returned DataSets is: " + dataReadResult.getDataSets().size());
            for (DataSet dataSet : dataReadResult.getDataSets()) {
                showFitnessData(dataSet,date,dataReadResult.getBuckets().size());
            }
        }
        // [END parse_read_data_result]
    }

    public void showFitnessData(DataSet dataSet, String date, int size){
        DateFormat timeFormat = DateFormat.getTimeInstance();
        DateFormat dateFormat = DateFormat.getDateInstance();
        for (DataPoint dp : dataSet.getDataPoints()) {
            Log.i(TAG, "Data point:" +"("+date+")");
            Log.i(TAG, "\tType: " + dp.getDataType().getName());
            Log.i(TAG, "\tStart: " + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)) + " " + timeFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
            Log.i(TAG, "\tEnd: " + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)) + " " + timeFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)));
            DateFormat form = new SimpleDateFormat("dd");
            String end = form.format(dp.getEndTime(TimeUnit.MILLISECONDS));
            String start = form.format(dp.getStartTime(TimeUnit.MILLISECONDS));
            for (Field field : dp.getDataType().getFields()) {
                Log.i(TAG, "타입: " + field.getName() + " 값: " + dp.getValue(field) + " 일: " + start + " 사이즈: "+ size);
                switch (field.getName()) {
                    case "distance":
                        float distanceVal = Float.parseFloat(dp.getValue(field).toString());
                        float distanceKM = Float.parseFloat(String.format("%8.1f", distanceVal));
                        distanceList.add(distanceKM);
                        timeLine_d.add(Integer.valueOf(start));
                        ArrayList<BarDataSet> barDataSetDistance = makeBarData(distanceList,"distance(m)");
                        BarData distanceData = new BarData(getXAxisValues(timeLine_d), barDataSetDistance);
                        distanceChart.setData(distanceData);
                        break;
                    case "steps":
                        float stepVal = Float.parseFloat(dp.getValue(field).toString());
                        stepList.add(stepVal);
                        timeLine_s.add(Integer.valueOf(start));
                        ArrayList<BarDataSet> barDataSetStep = makeBarData(stepList,"steps");
                        BarData stepData = new BarData(getXAxisValues(timeLine_s), barDataSetStep);
                        stepChart.setData(stepData);
                        break;
                    case "calories":
                        float calVal = Float.parseFloat(dp.getValue(field).toString());
                        caloList.add(calVal);
                        timeLine_c.add(Integer.valueOf(start));
                        ArrayList<BarDataSet> barDataSetCal = makeBarData(caloList,"calories");
                        BarData calData = new BarData(getXAxisValues(timeLine_c), barDataSetCal);
                        calorieChart.setData(calData);
                        break;
                    default:
                }
            }
        }
    }

    private ArrayList<BarDataSet> makeBarData( ArrayList<Float> value, String dataType){

        ArrayList<BarDataSet> dataSets = null;
        ArrayList<BarEntry> valueSet = new ArrayList<>();
        for(int i=0; i<value.size(); i++){
            BarEntry v2e1 = new BarEntry(value.get(i), i);
            valueSet.add(v2e1);
        }

        BarDataSet barDataSet = new BarDataSet(valueSet, dataType);
        switch (dataType){
            case "distance":
                barDataSet.setColor(ContextCompat.getColor(getContext(),R.color.distance));
                break;
            case "steps":
                barDataSet.setColor(ContextCompat.getColor(getContext(),R.color.step));
                break;
            case "calories":
                barDataSet.setColor(ContextCompat.getColor(getContext(),R.color.cal));
                break;
            default:
        }
        dataSets = new ArrayList<>();
        dataSets.add(barDataSet);
        return dataSets;
    }

    private ArrayList<String> getXAxisValues(ArrayList<Integer> arrayList) {
        ArrayList<String> xAxis = new ArrayList<>();
        for(int i= 0; i<arrayList.size(); i++){
            xAxis.add(arrayList.get(i)+"일");
        }
        return xAxis;
    }

}
