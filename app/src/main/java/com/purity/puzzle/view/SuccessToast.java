package com.purity.puzzle.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * Created by purity on 2016/10/2.
 */
public class SuccessToast extends View {
    RectF rectF = new RectF();
    ValueAnimator valueAnimator;
    float mAnimatedValue = 0f;
    private Paint mPaint;
    private float mWidth = 0f;
    private float mEyeWidth = 0f;
    private float mPadding = 0f;
    private float endAngle = 0f;
    private boolean isSmileLeft = false;
    private boolean isSmileRight = false;

    public SuccessToast(Context context) {
        super(context);
    }

    public SuccessToast(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SuccessToast(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        initPaint();
        initRect();
        mWidth = getMeasuredWidth();
        mPadding = dip2px(10);
        mEyeWidth = dip2px(3);
    }

    private void initPaint() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.parseColor("#5cb85c"));
        mPaint.setStrokeWidth(dip2px(2));
    }

    private void initRect() {
        rectF = new RectF(mPadding, mPadding, mWidth - mPadding, mWidth - mPadding);//（左，上，右，下）
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaint.setStyle(Paint.Style.STROKE);
        canvas.drawArc(rectF, 180, endAngle, false, mPaint);

        mPaint.setStyle(Paint.Style.FILL);
        if (isSmileLeft) {
            canvas.drawCircle(mPadding + mEyeWidth + mEyeWidth / 2, mWidth / 3, mEyeWidth, mPaint);
        }
        if (isSmileRight) {
            canvas.drawCircle(mWidth - mPadding - mEyeWidth - mEyeWidth / 2, mWidth / 3, mEyeWidth, mPaint);
        }
    }

    public int dip2px(float dpValue) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * startAnim()不带参数的方法
     */
    public void startAnim() {
        stopAnim();
        startViewAnim(0f, 1f, 2000);
    }

    /**
     * 停止动画的方法
     *
     */
    public void stopAnim() {
        if (valueAnimator != null) {
            clearAnimation();
            isSmileLeft = false;
            isSmileRight = false;
            mAnimatedValue = 0f;
            valueAnimator.end();
        }
    }

    /**
     * 开始动画的方法
     * @param startF 起始值
     * @param endF   结束值
     * @param time  动画的时间
     * @return
     */
    private ValueAnimator startViewAnim(float startF, final float endF, long time) {
        //设置valueAnimator 的起始值和结束值。
        valueAnimator = ValueAnimator.ofFloat(startF, endF);
        //设置动画时间
        valueAnimator.setDuration(time);
        //设置补间器。控制动画的变化速率
        valueAnimator.setInterpolator(new LinearInterpolator());
        //设置监听器。监听动画值的变化，做出相应方式。
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {

                mAnimatedValue = (float) valueAnimator.getAnimatedValue();
                //如果value的值小于0.5
                if (mAnimatedValue < 0.5) {
                    isSmileLeft = false;
                    isSmileRight = false;
                    endAngle = -360 * (mAnimatedValue);
                    //如果value的值在0.55和0.7之间
                } else if (mAnimatedValue > 0.55 && mAnimatedValue < 0.7) {
                    endAngle = -180;
                    isSmileLeft = true;
                    isSmileRight = false;
                    //其他
                } else {
                    endAngle = -180;
                    isSmileLeft = true;
                    isSmileRight = true;
                }
                //重绘
                postInvalidate();
            }
        });

        if (!valueAnimator.isRunning()) {
            valueAnimator.start();
        }
        return valueAnimator;
    }
}
