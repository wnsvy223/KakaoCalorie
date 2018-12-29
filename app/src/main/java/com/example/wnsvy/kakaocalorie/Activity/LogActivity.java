package com.example.wnsvy.kakaocalorie.Activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import com.example.wnsvy.kakaocalorie.Adapter.ViewPagerAdapter;
import com.example.wnsvy.kakaocalorie.Fragment.DayFragment;
import com.example.wnsvy.kakaocalorie.Fragment.MonthFragment;
import com.example.wnsvy.kakaocalorie.Fragment.WeekFragment;
import com.example.wnsvy.kakaocalorie.R;
import com.github.mikephil.charting.charts.BarChart;


public class LogActivity extends AppCompatActivity {

    public static String TAG = "Log_Data_Activity";
    private ViewPager viewPager;
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
                case R.id.day:
                    //viewPager.setCurrentItem(0);
                    //return true;
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, dayFragment)
                            .commit();
                    return true;

                case R.id.week:
                   //viewPager.setCurrentItem(1);
                    //return true;
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, weekFragment)
                            .commit();
                    return true;


                case R.id.month:
                    //viewPager.setCurrentItem(2);
                    //return true;
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
        //viewPager.setOffscreenPageLimit(3);
        //setupViewPager(viewPager);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        DayFragment dayFragment = new DayFragment();
        WeekFragment weekFragment = new WeekFragment();
        MonthFragment monthFragment = new MonthFragment();
        adapter.addFragment(dayFragment);
        adapter.addFragment(weekFragment);
        adapter.addFragment(monthFragment);
        viewPager.setAdapter(adapter);
    }

    private void initView(){
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

}
