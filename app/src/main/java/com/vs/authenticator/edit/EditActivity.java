package com.vs.authenticator.edit;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.vs.authenticator.R;
import com.vs.authenticator.Token;
import com.vs.authenticator.TokenPersistence;

public class EditActivity extends BaseActivity implements TextWatcher, View.OnClickListener {
    private final int REQUEST_IMAGE_OPEN = 1;
    private EditText mIssuer;
    private EditText mLabel;
    private ImageButton mImage;
    private Button mRestore;
    private Button mSave;
    private String mIssuerCurrent;
    private String mIssuerDefault;
    private String mLabelCurrent;
    private String mLabelDefault;
    private Uri mImageCurrent;
    private Uri mImageDefault;
    private Uri mImageDisplay;
    private Token token;

    private void showImage(Uri uri) {
        mImageDisplay = uri;
        onTextChanged(null, 0, 0, 0);
        Picasso.with(this)
                .load(uri)
                .placeholder(R.mipmap.ic_freeotp_logo_foreground)
                .into(mImage);
    }

    private boolean isImage(Uri uri) {
        if (uri == null)
            return mImageDisplay != null;

        return !uri.equals(mImageDisplay);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit);

        // Get token values.
        token = new TokenPersistence(this).get(getPosition());
        mIssuerCurrent = token.getIssuer();
        mLabelCurrent = token.getLabel();
        mImageCurrent = token.getImage();
        mIssuerDefault = token.getIssuer();
        mLabelDefault = token.getLabel();
        mImageDefault = token.getImage();

        // Get references to widgets.
        mIssuer = findViewById(R.id.issuer);
        mLabel = findViewById(R.id.label);
        mImage = findViewById(R.id.image);
        mRestore = findViewById(R.id.restore);
        mSave = findViewById(R.id.save);

        // Setup text changed listeners.
        mIssuer.addTextChangedListener(this);
        mLabel.addTextChangedListener(this);

        // Setup click callbacks.
        findViewById(R.id.cancel).setOnClickListener(this);
        findViewById(R.id.save).setOnClickListener(this);
        findViewById(R.id.restore).setOnClickListener(this);
        mImage.setOnClickListener(this);

        // Setup initial state.
        showImage(mImageCurrent);
        mLabel.setText(mLabelCurrent);
        mIssuer.setText(mIssuerCurrent);
        mIssuer.setSelection(mIssuer.getText().length());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_OPEN) {
                //mImageDisplay is set in showImage
                showImage(data.getData());
                token.setImage(mImageDisplay);
            } else {
                Toast.makeText(EditActivity.this, R.string.error_image_open, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        String label = mLabel.getText().toString();
        String issuer = mIssuer.getText().toString();
        mSave.setEnabled(!label.equals(mLabelCurrent) || !issuer.equals(mIssuerCurrent) || isImage(mImageCurrent));
        mRestore.setEnabled(!label.equals(mLabelDefault) || !issuer.equals(mIssuerDefault) || isImage(mImageDefault));
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image:
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.setType("image/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, REQUEST_IMAGE_OPEN);
                break;

            case R.id.restore:
                mLabel.setText(mLabelDefault);
                mIssuer.setText(mIssuerDefault);
                mIssuer.setSelection(mIssuer.getText().length());
                showImage(mImageDefault);
                break;

            case R.id.save:
                TokenPersistence tp = new TokenPersistence(this);
                Token token = tp.get(getPosition());
                token.setIssuer(mIssuer.getText().toString());
                token.setLabel(mLabel.getText().toString());
                token.setImage(mImageDisplay);
                TokenPersistence.saveAsync(this, token);

            case R.id.cancel:
                finish();
                break;
        }
    }
}
