package com.vs.authenticator.add;

import static io.fotoapparat.parameter.selector.FocusModeSelectors.autoFocus;
import static io.fotoapparat.parameter.selector.FocusModeSelectors.fixed;
import static io.fotoapparat.parameter.selector.LensPositionSelectors.back;
import static io.fotoapparat.parameter.selector.Selectors.firstAvailable;
import static io.fotoapparat.parameter.selector.SizeSelectors.biggestSize;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.vs.authenticator.R;
import com.vs.authenticator.Token;
import com.vs.authenticator.TokenPersistence;

import io.fotoapparat.Fotoapparat;
import io.fotoapparat.parameter.ScaleType;
import io.fotoapparat.parameter.selector.FocusModeSelectors;
import io.fotoapparat.view.CameraView;

public class ScanActivity extends Activity {
    private static ScanBroadcastReceiver receiver;
    private Fotoapparat fotoapparat;

    public static boolean hasCamera(Context context) {
        PackageManager pm = context.getPackageManager();
        return pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
    }

    private void addTokenAndFinish(String text) {
        Token token = null;
        try {
            token = new Token(text);
        } catch (Token.TokenUriInvalidException e) {
            e.printStackTrace();
        }

        //do not receive any more broadcasts
        this.unregisterReceiver(receiver);

        //check if token already exists
        assert token != null;
        if (new TokenPersistence(ScanActivity.this).tokenExists(token)) {
            finish();
            return;
        }

        TokenPersistence.saveAsync(ScanActivity.this, token);
        if (token.getImage() == null) {
            finish();
            return;
        }

        final ImageView image = findViewById(R.id.image);
        Picasso.with(ScanActivity.this)
                .load(token.getImage())
                .placeholder(R.drawable.scan)
                .into(image, new Callback() {
                    @Override
                    public void onSuccess() {
                        findViewById(R.id.progress).setVisibility(View.INVISIBLE);
                        image.setAlpha(0.9f);
                        image.postDelayed(() -> finish(), 2000);
                    }

                    @Override
                    public void onError() {
                        finish();
                    }
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            this.unregisterReceiver(receiver);
        } catch (IllegalArgumentException e) {
            // catch exception, when trying to unregister receiver again
            // there seems to be no way to check, if receiver if registered
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        receiver = new ScanBroadcastReceiver();
        this.registerReceiver(receiver, new IntentFilter(ScanBroadcastReceiver.ACTION));
        setContentView(R.layout.scan);
        CameraView cameraView = findViewById(R.id.camera_view);

        fotoapparat = Fotoapparat
                .with(this)
                .into(cameraView)
                .previewScaleType(ScaleType.CENTER_CROP)
                .photoSize(biggestSize())
                .lensPosition(back())
                .focusMode(firstAvailable(
                        FocusModeSelectors.continuousFocus(),
                        autoFocus(),
                        fixed()
                ))
                .frameProcessor(new ScanFrameProcessor(this))
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        fotoapparat.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        fotoapparat.stop();
    }

    public class ScanBroadcastReceiver extends BroadcastReceiver {
        public static final String ACTION = "ACTION_CODE_SCANNED";

        @Override
        public void onReceive(Context context, Intent intent) {
            String text = intent.getStringExtra("scanResult");
            addTokenAndFinish(text);
        }
    }
}
