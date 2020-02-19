package com.ryoma.circleprogressbar;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

import androidx.annotation.Nullable;

public class CircleProgressView extends View {

    private Paint mBackPaint;
    private Paint mFrontPaint;
    private Paint mTextPaint;
    private RectF mFrontRectF;
    private int mMaxProgress = 100;               // 最大进度值
    private int mProgress = 0;                  // 当前进度值
    private int mProgressBarWidth = dpToPx(2); // 进度条宽度
    private int mProgressPercent;               // 进度百分比，

    private boolean mShowAnimator = true;
    private int mProgressBarBackColor;
    private int mProgressBarFrontColor;
    private boolean mShowPercentText = true;
    private float mPercentTextSize;
    private int mPercentTextColor;

    private float distanceY;


    private OnAnimateChangeListener onAnimateChangeListener;

    private ValueAnimator mAnimator;


    public CircleProgressView(Context context) {
        super(context);
        init();
    }

    public CircleProgressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        // 获取自定义属性
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.CircleProgressViewStyle);
//        mMaxProgress = array.getInteger(R.styleable.CircleProgressViewStyle_cpv_maxprogress, 100);
        mProgress = array.getInteger(R.styleable.CircleProgressViewStyle_cpv_progress, 0);
        mProgressBarWidth = (int) array.getDimension(R.styleable.CircleProgressViewStyle_cpv_progressbarWidth, dpToPx(2));
        mProgressBarBackColor = array.getColor(R.styleable.CircleProgressViewStyle_cpv_progressbarBackColor, Color.BLACK);
        mProgressBarFrontColor = array.getColor(R.styleable.CircleProgressViewStyle_cpv_progressbarFrontColor, Color.WHITE);

        mShowPercentText = array.getBoolean(R.styleable.CircleProgressViewStyle_cpv_showPercentText, true);
        mPercentTextSize = array.getDimension(R.styleable.CircleProgressViewStyle_cpv_percentTextSize, dpToPx(12));
        mPercentTextColor = array.getColor(R.styleable.CircleProgressViewStyle_cpv_percentTextColor, Color.BLACK);
        init();
    }


    private void init(){
        mBackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBackPaint.setColor(mProgressBarBackColor);
        mBackPaint.setStrokeWidth(mProgressBarWidth);
        mBackPaint.setStyle(Paint.Style.STROKE);

        mFrontPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mFrontPaint.setColor(mProgressBarFrontColor);
        mFrontPaint.setStrokeWidth(mProgressBarWidth);
        mFrontPaint.setStyle(Paint.Style.STROKE);
        mFrontPaint.setStrokeCap(Paint.Cap.ROUND);

        calcPercent();

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setTextSize(mPercentTextSize);
        mTextPaint.setColor(mPercentTextColor);
        mTextPaint.setTextAlign(Paint.Align.CENTER);    // 是基于BaseLine，
        // https://www.jianshu.com/p/8b97627b21c4
        // top,bottom,都是相对BaseLine
        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        distanceY = (fontMetrics.bottom - fontMetrics.top)/2 - fontMetrics.bottom;

    }

    private void calcPercent(){
        mProgressPercent = mProgress * 100 / mMaxProgress;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // 设置成宽高一致
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize =  MeasureSpec.getSize(widthMeasureSpec);

        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize =  MeasureSpec.getSize(heightMeasureSpec);

        if (widthSpecMode == MeasureSpec.AT_MOST){
            widthSpecSize = mProgressBarWidth * 2;
        }
        if (heightSpecMode == MeasureSpec.AT_MOST){
            heightSpecSize = mProgressBarWidth * 2;
        }

        if (widthSpecSize < heightSpecSize){
            widthSpecSize = heightSpecSize;
        }
        else{
            heightSpecSize = widthSpecSize;
        }
        setMeasuredDimension(widthSpecSize, heightSpecSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        int r = Math.min(width, height) / 2 - mProgressBarWidth / 2;

        // 绘制背景圆
        canvas.drawCircle(width / 2, height / 2, r, mBackPaint);

        // 绘制进度弧
        if (mFrontRectF == null){
            mFrontRectF = new RectF();
            mFrontRectF.left = mProgressBarWidth / 2;
            mFrontRectF.top = mProgressBarWidth / 2;
            mFrontRectF.right = width - mProgressBarWidth / 2;
            mFrontRectF.bottom = height - mProgressBarWidth / 2;
        }

        float progressAngle = (mProgressPercent * 1.0f / 100) * 360 ;
        canvas.drawArc(mFrontRectF, -90, progressAngle, false, mFrontPaint);

        // 绘制百分比
        if (mShowPercentText){
            canvas.drawText(String.format("%d%%", mProgressPercent), width / 2, height / 2 + distanceY, mTextPaint);
        }
    }

    private int dpToPx(float dp){
        float scale = getContext().getResources().getDisplayMetrics().density;
        return (int)(dp * scale + 0.5f);
    }

    public void setProgress(int progress){
        setProgress(progress, false, 100);
    }

    public void setProgress(int progress, int duration){
        setProgress(progress, false, duration);
    }


    /**
     * 设置进度
     * @param progress
     * @param increase 增量设置
     */
    public void setProgress(int progress, boolean increase, int duration){
        mProgress = progress;
        if (mShowAnimator){
            mProgress = progress;
            int startPercent = 0;
            int endPercent = progress * 100 / mMaxProgress;
            if (mAnimator != null && mAnimator.isRunning()){
                mAnimator.cancel();
            }

            if (increase){
                startPercent = mProgressPercent;
            }

            mAnimator = ValueAnimator.ofInt(startPercent, endPercent);
            mAnimator.setDuration(duration);
            mAnimator.setInterpolator(new AccelerateInterpolator());
            mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mProgressPercent = (int) animation.getAnimatedValue();
                    invalidate();
                }
            });
            mAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    if (onAnimateChangeListener != null){
                        onAnimateChangeListener.onEnd(mProgressPercent);
                    }
                }
            });
            mAnimator.start();
        }
        else{
            calcPercent();
            invalidate();
        }
    }

    public void setOnAnimateChangeListener(OnAnimateChangeListener onAnimateChangeListener) {
        this.onAnimateChangeListener = onAnimateChangeListener;
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mAnimator != null && mAnimator.isRunning()){
            mAnimator.cancel();
            mAnimator = null;
        }
    }


    interface OnAnimateChangeListener{
        void onEnd(int percent);
    }
}
