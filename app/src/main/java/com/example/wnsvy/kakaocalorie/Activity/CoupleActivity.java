package com.example.wnsvy.kakaocalorie.Activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.wnsvy.kakaocalorie.R;
import com.example.wnsvy.kakaocalorie.Utils.SemiCircleProgress;

public class CoupleActivity extends AppCompatActivity {

    private SemiCircleProgress semiCircleManDistance;
    private SemiCircleProgress semiCircleWomanDistance;
    private SemiCircleProgress semiCircleManStep;
    private SemiCircleProgress semiCircleWomanStep;
    private SemiCircleProgress semiCircleManCalorie;
    private SemiCircleProgress semiCircleWomanCalorie;

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

    }

    public void initView(){
        semiCircleManDistance = findViewById(R.id.semiCircleManDistance);
        semiCircleWomanDistance = findViewById(R.id.semiCircleWomanDistance);
        semiCircleManStep = findViewById(R.id.semiCircleManStep);
        semiCircleWomanStep = findViewById(R.id.semiCircleWomanStep);
        semiCircleManCalorie = findViewById(R.id.semiCircleManCalorie);
        semiCircleWomanCalorie = findViewById(R.id.semiCircleWomanCalorie);
    }
}
