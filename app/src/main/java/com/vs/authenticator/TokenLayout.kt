package com.vs.authenticator

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import com.squareup.picasso.Picasso
import java.util.*

class TokenLayout : FrameLayout, View.OnClickListener, Runnable {
    private var mProgressInner: ProgressCircle? = null
    private var mProgressOuter: ProgressCircle? = null
    private var mImage: ImageView? = null
    private var mCode: TextView? = null
    private var mIssuer: TextView? = null
    private var mLabel: TextView? = null
    private var mPopupMenu: PopupMenu? = null
    private var mCodes: TokenCode? = null
    private var mType: Token.TokenType? = null
    private var mPlaceholder: String? = null
    private var mStartTime: Long = 0

    constructor(context: Context?) : super(context!!)
    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs
    )

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context!!, attrs, defStyle
    )

    override fun onFinishInflate() {
        super.onFinishInflate()
        mProgressInner = findViewById(R.id.progressInner)
        mProgressOuter = findViewById(R.id.progressOuter)
        mImage = findViewById(R.id.image)
        mCode = findViewById(R.id.code)
        mIssuer = findViewById(R.id.issuer)
        mLabel = findViewById(R.id.label)
        val mMenu = findViewById<ImageView>(R.id.menu)
        mPopupMenu = PopupMenu(context, mMenu)
        mMenu.setOnClickListener(this)
    }

    fun bind(token: Token, menu: Int, micl: PopupMenu.OnMenuItemClickListener?) {
        mCodes = null

        // Setup menu.
        mPopupMenu!!.menu.clear()
        mPopupMenu!!.menuInflater.inflate(menu, mPopupMenu!!.menu)
        mPopupMenu!!.setOnMenuItemClickListener(micl)

        // Cancel all active animations.
        isEnabled = true
        removeCallbacks(this)
        mImage!!.clearAnimation()
        mProgressInner!!.clearAnimation()
        mProgressOuter!!.clearAnimation()
        mProgressInner!!.visibility = GONE
        mProgressOuter!!.visibility = GONE

        // Get the code placeholder.
        val placeholder = CharArray(token.digits)
        Arrays.fill(placeholder, '-')
        mPlaceholder = String(placeholder)

        // Show the image.
        Picasso.with(context)
            .load(token.image)
            .placeholder(R.drawable.logo)
            .fit()
            .into(mImage)

        // Set the labels.
        mLabel!!.text = token.label
        mIssuer!!.text = token.issuer
        mCode!!.text = mPlaceholder
        if (mIssuer!!.text.length == 0) {
            mIssuer!!.text = token.label
            mLabel!!.visibility = GONE
        } else {
            mLabel!!.visibility = VISIBLE
        }
    }

    private fun animate(view: View?, anim: Int, animate: Boolean) {
        val a = AnimationUtils.loadAnimation(view!!.context, anim)
        if (!animate) a.duration = 0
        view.startAnimation(a)
    }

    fun start(type: Token.TokenType?, codes: TokenCode?, animate: Boolean) {
        mCodes = codes
        mType = type

        // Start animations.
        mProgressInner!!.visibility = VISIBLE
        animate(mProgressInner, R.anim.fadein, animate)
        animate(mImage, R.anim.token_image_fadeout, animate)
        when (type) {
            Token.TokenType.HOTP -> isEnabled = false
            Token.TokenType.TOTP -> {
                mProgressOuter!!.visibility = VISIBLE
                animate(mProgressOuter, R.anim.fadein, animate)
            }
        }
        mStartTime = System.currentTimeMillis()
        post(this)
    }

    override fun onClick(v: View) {
        mPopupMenu!!.show()
    }

    override fun run() {
        // Get the current data
        val code = if (mCodes == null) null else mCodes!!.currentCode
        if (code != null) {
            // Determine whether to enable/disable the view.
            if (!isEnabled) isEnabled = System.currentTimeMillis() - mStartTime > 5000

            // Update the fields
            mCode!!.text = code
            mProgressInner!!.setProgress(mCodes!!.currentProgress)
            if (mType != Token.TokenType.HOTP) mProgressOuter!!.setProgress(
                mCodes!!.totalProgress
            )
            postDelayed(this, 100)
            return
        }
        mCode!!.text = mPlaceholder
        mProgressInner!!.visibility = GONE
        mProgressOuter!!.visibility = GONE
        animate(mImage, R.anim.token_image_fadein, true)
    }
}