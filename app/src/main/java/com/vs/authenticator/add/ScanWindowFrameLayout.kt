package com.vs.authenticator.add

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout

class ScanWindowFrameLayout : FrameLayout {
    constructor(context: Context?) : super(context!!)
    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs
    )

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context!!, attrs, defStyle
    )

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (widthMeasureSpec > heightMeasureSpec) {
            super.onMeasure(heightMeasureSpec, heightMeasureSpec)
        } else {
            super.onMeasure(widthMeasureSpec, widthMeasureSpec)
        }
    }
}