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
import android.widget.Toast
import com.squareup.picasso.Picasso
import com.vs.authenticator.R
import com.vs.authenticator.Token
import com.vs.authenticator.TokenPersistence

class EditActivity : BaseActivity(), TextWatcher, View.OnClickListener {
    companion object {
        private const val REQUEST_IMAGE_OPEN = 1
    }

    private lateinit var mLabel: EditText
    private lateinit var mIssuer: EditText
    private var mRestore: Button? = null
    private var mSave: Button? = null
    private var mIssuerCurrent: String? = null
    private var mIssuerDefault: String? = null
    private var mLabelCurrent: String? = null
    private var mLabelDefault: String? = null
    private var mImageCurrent: Uri? = null
    private var mImageDefault: Uri? = null
    private var mImageDisplay: Uri? = null
    private var token: Token? = null

    private fun showImage(uri: Uri?) {
        if (uri != null) {
            mImageDisplay = uri
            onTextChanged("", 0, 0, 0)
            Picasso.with(this)
                .load(uri)
                .placeholder(R.mipmap.ic_launcher_foreground)
                .into(findViewById<ImageButton>(R.id.image))
        }
    }

    private fun isImage(uri: Uri?): Boolean {
        if (uri == null) {
            return false
        }
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit)

        // Get token values.
        token = TokenPersistence(this)[position]
        mIssuerCurrent = token!!.issuer
        mLabelCurrent = token!!.label
        mImageCurrent = token!!.image
        mIssuerDefault = token!!.issuer
        mLabelDefault = token!!.label
        mImageDefault = token!!.image

        // Get references to widgets.
        mIssuer = findViewById(R.id.issuer)
        mLabel = findViewById(R.id.label)
        mRestore = findViewById(R.id.restore)
        mSave = findViewById(R.id.save)

        // Setup text changed listeners.
        mIssuer.addTextChangedListener(this)
        mLabel.addTextChangedListener(this)

        // Setup click callbacks.
        findViewById<View>(R.id.cancel).setOnClickListener(this)
        findViewById<View>(R.id.save).setOnClickListener(this)
        findViewById<View>(R.id.restore).setOnClickListener(this)

        val imageButton: ImageButton? = findViewById(R.id.image)

        imageButton!!.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.type = "image/*"
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            startActivityForResult(intent, REQUEST_IMAGE_OPEN)
        }

        // Setup initial state.
        showImage(mImageCurrent)
        mLabel.setText(mLabelCurrent)
        mIssuer.setText(mIssuerCurrent)
        mIssuer.setSelection(mIssuer.text.length)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_OPEN) {
                //mImageDisplay is set in showImage
                showImage(data!!.data)
                token!!.image = mImageDisplay
            } else {
                Toast.makeText(this@EditActivity, R.string.error_image_open, Toast.LENGTH_LONG)
                    .show()
                showImage(null)
                token!!.image = mImageDisplay
            }
        } else {
            Toast.makeText(this@EditActivity, R.string.error_image_open, Toast.LENGTH_LONG)
                .show()
            showImage(null)
            token!!.image = mImageDisplay
        }
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        val label = mLabel.text.toString()
        val issuer = mIssuer.text.toString()
        mSave!!.isEnabled =
            label != mLabelCurrent || issuer != mIssuerCurrent || isImage(mImageCurrent)
        mRestore!!.isEnabled =
            label != mLabelDefault || issuer != mIssuerDefault || isImage(mImageDefault)
    }

    override fun afterTextChanged(s: Editable) {}
    override fun onClick(v: View) {
        when (v.id) {
            R.id.image -> {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intent.type = "image/*"
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                startActivityForResult(intent, REQUEST_IMAGE_OPEN)
            }
            R.id.restore -> {
                mLabel.setText(mLabelDefault)
                mIssuer.setText(mIssuerDefault)
                mIssuer.setSelection(mIssuer.text.length)
                showImage(mImageDefault)
            }
            R.id.save -> {
                val tp = TokenPersistence(this)
                val token = tp[position]
                token!!.issuer = mIssuer.text.toString()
                token.label = mLabel.text.toString()
                token.image = mImageDisplay
                TokenPersistence.saveAsync(this, token)
                finish()
            }
            R.id.cancel -> finish()
        }
    }
}
