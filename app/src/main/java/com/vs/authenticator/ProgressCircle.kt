package com.vs.authenticator

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View

class ProgressCircle : View {
    private var mPaint: Paint? = null
    private var mRectF: RectF? = null
    private var mRect: Rect? = null
    private var mProgress = 0
    private var max = 0
    private var mHollow = false
    private var mPadding = 0f
    private var mStrokeWidth = 0f

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        setup(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        setup(context, attrs)
    }

    constructor(context: Context) : super(context) {
        setup(context, null)
    }

    private fun setup(context: Context, attrs: AttributeSet?) {
        val dm = resources.displayMetrics
        mPadding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2f, dm)
        mStrokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4f, dm)
        mRectF = RectF()
        mRect = Rect()
        mPaint = Paint()
        mPaint!!.setARGB(0x99, 0x33, 0x33, 0x33)
        mPaint!!.isAntiAlias = true
        mPaint!!.strokeCap = Paint.Cap.BUTT
        if (attrs != null) {
            val t = context.theme
            val a = t.obtainStyledAttributes(attrs, R.styleable.ProgressCircle, 0, 0)
            try {
                max = a.getInteger(R.styleable.ProgressCircle_max, 100)
                setHollow(a.getBoolean(R.styleable.ProgressCircle_hollow, false))
            } finally {
                a.recycle()
            }
        }
    }

    private fun setHollow(hollow: Boolean) {
        mHollow = hollow
        mPaint!!.style =
            if (hollow) Paint.Style.STROKE else Paint.Style.FILL
        mPaint!!.strokeWidth = (if (hollow) mStrokeWidth else 0F)
    }

    fun setProgress(progress: Int) {
        mProgress = progress
        val percent = mProgress * 100 / max
        if (percent > 25 || mProgress == 0) mPaint!!.setARGB(
            0x99,
            0x33,
            0x33,
            0x33
        ) else mPaint!!.setARGB(0x99, 0xff, 0xe0 * percent / 25, 0x00)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        getDrawingRect(mRect)
        mRect!!.left += (paddingLeft + mPadding).toInt()
        mRect!!.top += (paddingTop + mPadding).toInt()
        mRect!!.right -= (paddingRight + mPadding).toInt()
        mRect!!.bottom -= (paddingBottom + mPadding).toInt()
        mRectF!!.set(mRect!!)
        canvas.drawArc(mRectF!!, -90f, (mProgress * 360 / max).toFloat(), !mHollow, mPaint!!)
    }
}