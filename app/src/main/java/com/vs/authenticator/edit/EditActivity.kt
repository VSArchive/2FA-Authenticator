package com.vs.authenticator.edit

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.activity.result.contract.ActivityResultContracts
import com.squareup.picasso.Picasso
import com.vs.authenticator.R
import com.vs.authenticator.Token
import com.vs.authenticator.TokenPersistence

class EditActivity : BaseActivity(), TextWatcher, View.OnClickListener {
    private lateinit var mLabel: EditText
    private lateinit var mIssuer: EditText
    private var mSave: Button? = null
    private var imageButton: ImageButton? = null
    private var mIssuerCurrent: String? = null
    private var mLabelCurrent: String? = null
    private var mImageCurrent: Uri? = null
    private var token: Token? = null

    private fun showImage(uri: Uri?) {
        if (uri != null) {
            Picasso.with(this)
                .load(uri)
                .placeholder(R.mipmap.ic_launcher_foreground)
                .into(imageButton)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit)

        // Get token values.
        token = TokenPersistence(this)[position]
        mIssuerCurrent = token!!.issuer
        mLabelCurrent = token!!.label
        mImageCurrent = token!!.image

        // Get references to widgets.
        mIssuer = findViewById(R.id.issuer)
        mLabel = findViewById(R.id.label)
        mSave = findViewById(R.id.save)
        imageButton = findViewById(R.id.image)

        // Setup click callbacks.
        findViewById<View>(R.id.cancel).setOnClickListener(this)
        findViewById<View>(R.id.save).setOnClickListener(this)

        imageButton!!.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.type = "image/*"
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            imagePicker.launch(intent)
        }

        // Setup initial state.
        showImage(mImageCurrent)
        mLabel.setText(mLabelCurrent)
        mIssuer.setText(mIssuerCurrent)
        mIssuer.setSelection(mIssuer.text.length)

        // Setup text changed listeners.
        mIssuer.addTextChangedListener(this)
        mLabel.addTextChangedListener(this)
    }

    private var imagePicker =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                mImageCurrent = result.data!!.data
                showImage(mImageCurrent)
                token!!.image = mImageCurrent
                mSave!!.isEnabled = true
            }
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
