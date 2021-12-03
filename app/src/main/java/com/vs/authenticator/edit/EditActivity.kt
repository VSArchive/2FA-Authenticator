package com.vs.authenticator.edit

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import com.vs.authenticator.R
import com.vs.authenticator.Token
import com.vs.authenticator.TokenPersistence

class EditActivity : BaseActivity(), TextWatcher, View.OnClickListener {
    private lateinit var mLabel: EditText
    private lateinit var mIssuer: EditText
    private var mSave: Button? = null
    private var mIssuerCurrent: String? = null
    private var mLabelCurrent: String? = null
    private var token: Token? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit)

        // Get token values.
        token = TokenPersistence(this)[position]
        mIssuerCurrent = token!!.issuer
        mLabelCurrent = token!!.label

        // Get references to widgets.
        mIssuer = findViewById(R.id.issuer)
        mLabel = findViewById(R.id.label)
        mSave = findViewById(R.id.save)

        // Setup click callbacks.
        findViewById<View>(R.id.cancel).setOnClickListener(this)
        findViewById<View>(R.id.save).setOnClickListener(this)

        // Setup initial state.
        mLabel.setText(mLabelCurrent)
        mIssuer.setText(mIssuerCurrent)
        mIssuer.setSelection(mIssuer.text.length)

        // Setup text changed listeners.
        mIssuer.addTextChangedListener(this)
        mLabel.addTextChangedListener(this)
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        val label = mLabel.text.toString()
        val issuer = mIssuer.text.toString()
        mSave!!.isEnabled =
            label != mLabelCurrent || issuer != mIssuerCurrent
    }

    override fun afterTextChanged(s: Editable) {}
    override fun onClick(v: View) {
        when (v.id) {
            R.id.save -> {
                token!!.issuer = mIssuer.text.toString()
                token!!.label = mLabel.text.toString()
                TokenPersistence.saveAsync(this, token!!)
                finish()
            }
            R.id.cancel -> finish()
        }
    }
}
