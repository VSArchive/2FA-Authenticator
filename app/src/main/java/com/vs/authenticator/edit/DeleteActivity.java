package com.vs.authenticator.edit;

import android.os.Bundle;

import com.vs.authenticator.R;
import com.vs.authenticator.Token;
import com.vs.authenticator.TokenPersistence;

public class DeleteActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.delete);

        final Token token = new TokenPersistence(this).get(getPosition());

        findViewById(R.id.cancel).setOnClickListener(v -> finish());

        findViewById(R.id.delete).setOnClickListener(v -> {
            //delete the image that was copied to storage, before deleting the token
            token.deleteImage();
            new TokenPersistence(DeleteActivity.this).delete(getPosition());
            finish();
        });
    }
}
