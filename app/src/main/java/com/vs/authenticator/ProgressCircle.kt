package com.vs.authenticator;

import android.content.Context;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;

public class ProgressCircle extends View {
    private Paint mPaint;
    private RectF mRectF;
    private Rect mRect;
    private int mProgress;
    private int mMax;
    private boolean mHollow;
    private float mPadding;
    private float mStrokeWidth;

    public ProgressCircle(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setup(context, attrs);
    }

    public ProgressCircle(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup(context, attrs);
    }

    public ProgressCircle(Context context) {
        super(context);
        setup(context, null);
    }

    private void setup(Context context, AttributeSet attrs) {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        mPadding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, dm);
        mStrokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, dm);

        mRectF = new RectF();
        mRect = new Rect();

        mPaint = new Paint();
        mPaint.setARGB(0x99, 0x33, 0x33, 0x33);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeCap(Paint.Cap.BUTT);

        if (attrs != null) {
            Theme t = context.getTheme();
            TypedArray a = t.obtainStyledAttributes(attrs, R.styleable.ProgressCircle, 0, 0);

            try {
                setMax(a.getInteger(R.styleable.ProgressCircle_max, 100));
                setHollow(a.getBoolean(R.styleable.ProgressCircle_hollow, false));
            } finally {
                a.recycle();
            }
        }
    }

    public int getMax() {
        return mMax;
    }

    public void setMax(int max) {
        this.mMax = max;
    }

    public void setHollow(boolean hollow) {
        mHollow = hollow;
        mPaint.setStyle(hollow ? Style.STROKE : Style.FILL);
        mPaint.setStrokeWidth(hollow ? mStrokeWidth : 0);
    }

    public void setProgress(int progress) {
        mProgress = progress;

        int percent = mProgress * 100 / getMax();
        if (percent > 25 || mProgress == 0)
            mPaint.setARGB(0x99, 0x33, 0x33, 0x33);
        else
            mPaint.setARGB(0x99, 0xff, 0xe0 * percent / 25, 0x00);

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        getDrawingRect(mRect);

        mRect.left += getPaddingLeft() + mPadding;
        mRect.top += getPaddingTop() + mPadding;
        mRect.right -= getPaddingRight() + mPadding;
        mRect.bottom -= getPaddingBottom() + mPadding;
        mRectF.set(mRect);

        canvas.drawArc(mRectF, -90, mProgress * 360 / getMax(), !mHollow, mPaint);
    }
}