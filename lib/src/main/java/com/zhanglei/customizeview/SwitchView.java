package com.zhanglei.customizeview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by zhanglei on 15/6/27.
 */
public class SwitchView extends View {

    private Paint mBackPaint;
    private static final float mStrokeWidth = 5;
    public static final int CHECK_COLOR = 0xff32cd66;
    public static final int UNCHECK_COLOR = 0xffb5b5b5;
    public static final int INNER_BACK_COLOR = 0xffe2e2e2;



    private long mStartTime;
    private long mTargetTime;
    private long mDuration = -1;

    private int mLeftStartX;
    private int mLeftTargetX;

    private int mRightStartX;
    private int mRightTargetX;

    private long mRightStartTime;
    private long mRightTargetTime;
    private long mRightDuration = -1;
    private boolean isPlayingRight = false;
    private boolean isPlaying = false;


    private boolean isChecked = false;

    private int mEdgeWidth;

    private OnCheckedChangeListener mOnCheckedlistener;


    public SwitchView(Context context) {
        this(context, null, 0);
    }

    public SwitchView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwitchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mBackPaint = new Paint();
        mBackPaint.setColor(0xffffffff);
        mBackPaint.setAntiAlias(true);
        mBackPaint.setStyle(Paint.Style.FILL);
        mBackPaint.setStrokeWidth(mStrokeWidth);

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setChecked(!isChecked);
                initAnimator();
            }
        });

        setChecked(true);


    }

    private void initAnimator() {
        mStartTime = System.currentTimeMillis();
        mDuration = isChecked ? 400 : 300;
        mTargetTime = mStartTime + mDuration;

        mRightStartTime = System.currentTimeMillis();
        mRightDuration = isChecked ? 300 : 400;
        mRightTargetTime = mRightStartTime + mRightDuration;
        isPlaying = true;
        isPlayingRight = true;
        invalidate();
    }


    public void setChecked(boolean check) {
        if (mOnCheckedlistener!=null && check!=isChecked) {
            mOnCheckedlistener.onCheckedChanged(this, check);
        }
        isChecked = check;

    }

    // interface to get isChecked state
    public boolean isChecked() {
        return isChecked;
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        mOnCheckedlistener = listener;
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            mEdgeWidth = getWidth()/30;
            mLeftStartX = getWidth()/3;
            mLeftTargetX = getWidth()*2/3;
            mRightStartX = mLeftStartX;
            mRightTargetX = mLeftTargetX;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mBackPaint.setColor(getCurColor());
        drawRectCir(canvas, getWidth() / 3, getWidth() / 3, getWidth() * 2 / 3, getHeight() / 2, mBackPaint);

        mBackPaint.setColor(INNER_BACK_COLOR);
        drawRectCir(canvas, getInnerBackR(), getInnerLeftCenterX(), getWidth() - getInnerLeftCenterX(), getHeight() / 2, mBackPaint);
        mBackPaint.setColor(0xffffffff);
        drawRectCir(canvas, getWidth() / 3 - mEdgeWidth, getLeftCenterX(), getRightCenterX(), getHeight() / 2, mBackPaint);
        if (isPlaying || isPlayingRight) {
            invalidate();
        }


    }

    private int getInnerBackR() {
        if (isPlayingRight || isPlaying) {
            long duration = isChecked ? Math.min(mDuration, mRightDuration) * 2 / 3 : Math.max(mDuration, mRightDuration);
            if (System.currentTimeMillis() >= duration+mStartTime) {
                return isChecked ? 0: getWidth() / 3 - mEdgeWidth;
            }
            float ratio = (float)(System.currentTimeMillis() - mStartTime) / duration;
            ratio = Math.min(ratio, 1);
            ratio = isChecked ? (1-ratio) : ratio;
            return (int)((getWidth()/3 - mEdgeWidth) * ratio);
        } else {
            return isChecked ? 0: getWidth() / 3 - mEdgeWidth;
        }
    }

    private int getInnerLeftCenterX() {
        int innerRadius = getInnerBackR();
        float ratio = (float)innerRadius / (getWidth()/3 -mEdgeWidth);
        int leftX = (int) (getWidth()/2 - (getWidth()/4) * ratio);
        leftX = Math.max(leftX, getWidth() / 3);
        return leftX;
    }


    private int getCurColor() {
        int curColor = UNCHECK_COLOR;
        if (isPlayingRight || isPlaying) {
            long duration = Math.min(mDuration, mRightDuration);
            if (System.currentTimeMillis() >= duration+mStartTime) {
                return isChecked ? CHECK_COLOR : UNCHECK_COLOR;
            }
            float ratio = (float)(System.currentTimeMillis() - mStartTime) / duration;
            ratio = Math.min(ratio, 1);
            ratio = isChecked ? ratio : 1 - ratio;
            int curAlpha =(int) (0xff * ratio);
            curColor = Color.argb(curAlpha, Color.red(CHECK_COLOR), Color.green(CHECK_COLOR), Color.blue(CHECK_COLOR));

        } else {
            return isChecked ? CHECK_COLOR : UNCHECK_COLOR;
        }
        return curColor;
    }


    private int getLeftCenterX() {
        int currentLeftCenerX = isChecked ? mLeftStartX : mLeftTargetX;

        if (isPlaying) {
            float ratio = ((float)(System.currentTimeMillis()-mStartTime) / mDuration) * (isChecked ? 1 : -1);
            currentLeftCenerX += (mLeftTargetX - mLeftStartX) * ratio;
            if ((isChecked && currentLeftCenerX >= mLeftTargetX ) ||
                    !isChecked && currentLeftCenerX <= mLeftStartX ) {
                isPlaying = false;
            }
        } else {
            return isChecked ? mLeftTargetX : mLeftStartX;
        }

        return currentLeftCenerX;
    }

    private int getRightCenterX() {
        int currentRightX = isChecked ? mRightStartX : mRightTargetX;
        if (isPlayingRight) {
            float ratio = ((float)(System.currentTimeMillis()-mRightStartTime) / mRightDuration) * (isChecked ? 1 : -1);
            currentRightX += (mRightTargetX - mRightStartX) * ratio;
            if ((isChecked && currentRightX >= mRightTargetX) ||
                !isChecked && currentRightX <= mRightStartX ) {
                isPlayingRight = false;
            }
        } else {
            return isChecked ?  mRightTargetX : mRightStartX;
        }
        return currentRightX;
    }



    private void drawRectCir(Canvas canvas, int radius, int leftCenterX, int rightCenterX, int centerY, Paint paint) {
//        // left circle
//        canvas.drawCircle(leftCenterX, centerY, radius, paint);
//        //right circle
//        canvas.drawCircle(rightCenterX, centerY, radius, paint);
//        // center Rectangle
//        if (rightCenterX > leftCenterX) {
//            canvas.drawRect(leftCenterX, centerY - radius, rightCenterX, centerY + radius, paint);
//        }
        canvas.drawRoundRect(new RectF(leftCenterX-radius, centerY-radius, rightCenterX+radius, centerY+radius), radius, radius, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isPlaying||isPlayingRight) {
            return true;
        }
        return super.onTouchEvent(event);
    }

    public interface OnCheckedChangeListener {
        void onCheckedChanged(View view, boolean isChecked);
    }
}
