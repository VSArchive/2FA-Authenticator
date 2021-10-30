package com.vs.authenticator.add;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import io.fotoapparat.preview.Frame;
import io.fotoapparat.preview.FrameProcessor;

public class ScanFrameProcessor implements FrameProcessor {

    private static final Handler MAIN_THREAD_HANDLER = new Handler(Looper.getMainLooper());
    private final Context scanActivityContext;
    private Reader reader;

    public ScanFrameProcessor(Context context) {
        scanActivityContext = context;
    }

    @Override
    public void processFrame(final Frame frame) {
        MAIN_THREAD_HANDLER.post(() -> {
            try {
                reader = new QRCodeReader();
                LuminanceSource ls = new PlanarYUVLuminanceSource(
                        frame.image, frame.size.width, frame.size.height,
                        0, 0, frame.size.width, frame.size.height, false);
                Result r = reader.decode(new BinaryBitmap(new HybridBinarizer(ls)));
                sendTextToActivity(r.getText());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void sendTextToActivity(String text) {
        Intent intent = new Intent();
        intent.setAction(ScanActivity.ScanBroadcastReceiver.ACTION);
        intent.putExtra("scanResult", text);
        scanActivityContext.sendBroadcast(intent);
    }
}
