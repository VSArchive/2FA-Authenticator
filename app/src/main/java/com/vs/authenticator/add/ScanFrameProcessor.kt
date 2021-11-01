package com.vs.authenticator.add

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import com.google.zxing.BinaryBitmap
import com.google.zxing.LuminanceSource
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.Reader
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import io.fotoapparat.preview.Frame
import io.fotoapparat.preview.FrameProcessor

class ScanFrameProcessor(private val scanActivityContext: Context) : FrameProcessor {
    private var reader: Reader? = null
    override fun processFrame(frame: Frame) {
        MAIN_THREAD_HANDLER.post {
            try {
                reader = QRCodeReader()
                val ls: LuminanceSource = PlanarYUVLuminanceSource(
                    frame.image, frame.size.width, frame.size.height,
                    0, 0, frame.size.width, frame.size.height, false
                )
                val r = (reader as QRCodeReader).decode(BinaryBitmap(HybridBinarizer(ls)))
                sendTextToActivity(r.text)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun sendTextToActivity(text: String) {
        val intent = Intent()
        intent.action = ScanActivity.ACTION
        intent.putExtra("scanResult", text)
        scanActivityContext.sendBroadcast(intent)
    }

    companion object {
        private val MAIN_THREAD_HANDLER = Handler(Looper.getMainLooper())
    }
}