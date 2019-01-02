package com.example.wnsvy.kakaocalorie.Activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.example.wnsvy.kakaocalorie.Fragment.DayFragment;
import com.example.wnsvy.kakaocalorie.Fragment.MonthFragment;
import com.example.wnsvy.kakaocalorie.Fragment.WeekFragment;
import com.example.wnsvy.kakaocalorie.R;
import com.github.mikephil.charting.charts.BarChart;


public class LogActivity extends AppCompatActivity {

    public static String TAG = "Log_Data_Activity";
    private BottomNavigationView navigation;
    public BarChart stepChart;
    public BarChart distanceChart;
    public BarChart calorieChart;
    public DayFragment dayFragment;
    public WeekFragment weekFragment;
    public MonthFragment monthFragment;


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            switch (item.getItemId()) {
                case R.id.day: ;
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, dayFragment)
                            .commit();
                    return true;

                case R.id.week:
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, weekFragment)
                            .commit();
                    return true;


                case R.id.month:
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, monthFragment)
                            .commit();
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        initView();

        dayFragment = new DayFragment();
        weekFragment = new WeekFragment();
        monthFragment = new MonthFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_container, dayFragment)
                .commit();
    }

    private void initView(){
        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

}
