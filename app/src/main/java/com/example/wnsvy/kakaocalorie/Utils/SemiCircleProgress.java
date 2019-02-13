package com.example.wnsvy.kakaocalorie.Utils;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.example.wnsvy.kakaocalorie.R;

public class SemiCircleProgress extends View implements ValueAnimator.AnimatorUpdateListener{

    private Path rimPath;
    private Paint rimPaint;
    private RectF rimRect;

    private Path frontPath;
    private Paint frontPaint;
    private RectF frontRect;

    private int width;
    private int thickness;
    private int mColorFront;
    private int mColorBack;
    private ValueAnimator mAnimator;


    public SemiCircleProgress(Context context, AttributeSet attrs) {
        super(context, attrs);


        //Getting our XML attributes declared in <declare-styleable>
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.SemiCircleProgress,
                0, 0);
        try {
            mColorFront = a.getColor(R.styleable.SemiCircleProgress_colorFront, Color.CYAN);
            mColorBack = a.getColor(R.styleable.SemiCircleProgress_colorBack, Color.LTGRAY);
        } finally {
            a.recycle();
        }

        initView();
    }

    private void initView(){
        rimPath = new Path();
        rimRect = new RectF();
        rimPaint = new Paint();
        rimPaint.setStyle(Paint.Style.FILL);
        rimPaint.setColor(mColorBack);
        rimPaint.setAntiAlias(true);

        frontPath = new Path();
        frontRect = new RectF();
        frontPaint = new Paint();
        frontPaint.setStyle(Paint.Style.FILL);
        frontPaint.setColor(mColorFront);
        frontPaint.setAntiAlias(true);
    }

    protected void onDraw(Canvas canvas) {
        //Draws our Paths(what to draw) using our Paints(how to draw)   : drawPath - 인자값으로 주어진 경로대로 그림
        canvas.drawPath(rimPath, rimPaint);
        canvas.drawPath(frontPath,frontPaint);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {


        //Set the thickness of our circle to 1/8 of the width
        thickness = w /8;

        //makes the current width available to our other methods
        width = w;

        // Set the Rectangle coordinates to the full size of the View
        rimRect.set(0, 0, w, w);

        //This makes sure the Path is empty
        rimPath.reset();

        rimPath.moveTo(0, w / 2);
        //Draw exterior arc
        rimPath.arcTo(rimRect, 180, 180);

        // Draw right closing line
        rimPath.rLineTo(-thickness, 0);

        // Move the side of rectangle inward (dx positive)
        rimRect.inset(thickness, thickness);
        // Create & Add interior arc based on narrowed rectangle
        rimPath.addArc(rimRect, 0, -180);

        // Draw left closing line
        rimPath.rLineTo(-thickness, 0);

    }

    public void setProgressWithAnimation(long duration, int progress) {
        // sets the range of our value
            mAnimator = ValueAnimator.ofInt(0, progress);  // 반원이기 때문에 0 ~180도 사이의 값을 지정하여 그 위치까지 그려지도록 세팅 내가 원하는 값을 사용하려면 비율값으로 해야할듯
            // sets the duration of our animation
            mAnimator.setDuration(duration);
            // registers our AnimatorUpdateListener
            mAnimator.addUpdateListener(this);
            mAnimator.start();
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {


        //gets the current value of our animation
        int value = (int) animation.getAnimatedValue();

        //makes sure the path is empty
        frontPath.reset();

        //sets the rectangle for our arc
        frontRect.set(0, 0, width, width);

        // starts our drawing on the middle left
        frontPath.moveTo(0, width/2);

        //draws an arc starting at 180 and moving clockwise for the corresponding value
        frontPath.arcTo(frontRect, 180, value);

        //moves our rectangle inward in order to draw the interior arc
        frontRect.inset(thickness, thickness);

        //draws the interior arc starting at(180+value) and moving counter-clockwise for the corresponding value
        frontPath.arcTo(frontRect, 180 + value, -value);

        //draws the closing line
        frontPath.rLineTo(-thickness, 0);

        // Forces the view to reDraw itself
        invalidate();
    }

}
