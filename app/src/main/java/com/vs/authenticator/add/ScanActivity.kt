package com.vs.authenticator.add

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.vs.authenticator.R
import com.vs.authenticator.Token
import com.vs.authenticator.Token.TokenUriInvalidException
import com.vs.authenticator.TokenPersistence
import io.fotoapparat.Fotoapparat
import io.fotoapparat.parameter.ScaleType
import io.fotoapparat.parameter.selector.FocusModeSelectors
import io.fotoapparat.parameter.selector.LensPositionSelectors
import io.fotoapparat.parameter.selector.Selectors
import io.fotoapparat.parameter.selector.SizeSelectors
import io.fotoapparat.view.CameraView

class ScanActivity : Activity() {
    private var fotoapparat: Fotoapparat? = null
    private fun addTokenAndFinish(text: String?) {
        var token: Token? = null
        try {
            token = Token(text)
        } catch (e: TokenUriInvalidException) {
            e.printStackTrace()
        }

        //do not receive any more broadcasts
        unregisterReceiver(receiver)
        if (token == null){
            finish()
            return
        }
        if (TokenPersistence(this@ScanActivity).tokenExists(token)) {
            finish()
            return
        }
        TokenPersistence.saveAsync(this, token)
        if (token.image == null) {
            finish()
            return
        }
        val image = findViewById<ImageView>(R.id.image)
        Picasso.with(this@ScanActivity)
            .load(token.image)
            .placeholder(R.drawable.scan)
            .into(image, object : Callback {
                override fun onSuccess() {
                    findViewById<View>(R.id.progress).visibility = View.INVISIBLE
                    image.alpha = 0.9f
                    image.postDelayed({ finish() }, 2000)
                }

                override fun onError() {
                    finish()
                }
            })
    }

    public override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(receiver)
        } catch (e: IllegalArgumentException) {
            // catch exception, when trying to unregister receiver again
            // there seems to be no way to check, if receiver if registered
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        receiver = ScanBroadcastReceiver()
        this.registerReceiver(receiver, IntentFilter(ACTION))
        setContentView(R.layout.scan)
        val cameraView: CameraView = findViewById(R.id.camera_view)
        fotoapparat = Fotoapparat
            .with(this)
            .into(cameraView)
            .previewScaleType(ScaleType.CENTER_CROP)
            .photoSize(SizeSelectors.biggestSize())
            .lensPosition(LensPositionSelectors.back())
            .focusMode(
                Selectors.firstAvailable(
                    FocusModeSelectors.continuousFocus(),
                    FocusModeSelectors.autoFocus(),
                    FocusModeSelectors.fixed()
                )
            )
            .frameProcessor(ScanFrameProcessor(this))
            .build()
    }

    override fun onStart() {
        super.onStart()
        fotoapparat!!.start()
    }

    override fun onStop() {
        super.onStop()
        fotoapparat!!.stop()
    }

    inner class ScanBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val text = intent.getStringExtra("scanResult")
            addTokenAndFinish(text)
        }
    }

    companion object {
        const val ACTION = "ACTION_CODE_SCANNED"
        private var receiver: ScanBroadcastReceiver? = null
        fun hasCamera(context: Context): Boolean {
            val pm = context.packageManager
            return pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
        }
    }
}