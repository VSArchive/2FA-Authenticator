package com.vs.authenticator.edit;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.vs.authenticator.R;
import com.vs.authenticator.Token;
import com.vs.authenticator.TokenPersistence;

public class DeleteActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.delete);

        final Token token = new TokenPersistence(this).get(getPosition());
        ((TextView) findViewById(R.id.issuer)).setText(token.getIssuer());
        ((TextView) findViewById(R.id.label)).setText(token.getLabel());
        Picasso.with(this)
                .load(token.getImage())
                .placeholder(R.mipmap.ic_freeotp_logo_foreground)
                .into((ImageView) findViewById(R.id.image));

        findViewById(R.id.cancel).setOnClickListener(v -> finish());

        findViewById(R.id.delete).setOnClickListener(v -> {
            //delete the image that was copied to storage, before deleting the token
            token.deleteImage();
            new TokenPersistence(DeleteActivity.this).delete(getPosition());
            finish();
        });
    }
}
