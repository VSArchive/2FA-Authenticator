package com.vs.authenticator

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView

class TokenLayout : FrameLayout, View.OnClickListener, Runnable {
    private var mImage: ImageView? = null
    private var mCode: TextView? = null
    private var mIssuer: TextView? = null
    private var mLabel: TextView? = null
    private var mPopupMenu: PopupMenu? = null
    private var mToken: Token? = null

    constructor(context: Context?) : super(context!!)
    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs
    )

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context!!, attrs, defStyle
    )

    override fun onFinishInflate() {
        super.onFinishInflate()
        mImage = findViewById(R.id.image)
        mCode = findViewById(R.id.code)
        mIssuer = findViewById(R.id.issuer)
        mLabel = findViewById(R.id.label)
        val mMenu = findViewById<ImageView>(R.id.menu)
        mPopupMenu = PopupMenu(context, mMenu)
        mMenu.setOnClickListener(this)
    }

    fun bind(token: Token, menu: Int, menuItemClickListener: PopupMenu.OnMenuItemClickListener?) {
        // Setup menu.
        this.mToken = token
        mPopupMenu!!.menu.clear()
        mPopupMenu!!.menuInflater.inflate(menu, mPopupMenu!!.menu)
        mPopupMenu!!.setOnMenuItemClickListener(menuItemClickListener)

        // Set the labels.
        mLabel!!.text = token.label
        mIssuer!!.text = token.issuer
        mCode!!.text = token.generateCodes().currentCode
        if (mIssuer!!.text.isEmpty()) {
            mIssuer!!.text = token.label
            mLabel!!.visibility = GONE
        } else {
            mLabel!!.visibility = VISIBLE
        }
        post(this)
    }

    override fun onClick(v: View) {
        mPopupMenu!!.show()
    }

    override fun run() {
        mCode!!.text = mToken!!.generateCodes().currentCode
        postDelayed(this, 100)
    }
}