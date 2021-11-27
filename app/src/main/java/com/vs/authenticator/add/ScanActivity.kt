package com.vs.authenticator.add

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Size
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.common.util.concurrent.ListenableFuture
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.vs.authenticator.R
import com.vs.authenticator.Token
import com.vs.authenticator.TokenPersistence
import java.util.concurrent.Executors

class ScanActivity : AppCompatActivity() {
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>

    private val cameraExecutor = Executors.newSingleThreadExecutor()
    private lateinit var previewView: PreviewView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.scan)

        previewView = findViewById(R.id.previewView)
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProvider()
    }

    private fun cameraProvider() {
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            startCamera(cameraProvider)
        }, ContextCompat.getMainExecutor(this))
    }

    private fun startCamera(cameraProvider: ProcessCameraProvider) {
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        val preview = Preview.Builder().apply {
            setTargetResolution(Size(previewView.width, previewView.height))
        }.build()

        val imageAnalysis = ImageAnalysis.Builder()
            .setTargetResolution(Size(previewView.width, previewView.height))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also {
                it.setAnalyzer(cameraExecutor, QrCodeAnalyzer { qrResult ->
                    previewView.post {
                        addTokenAndFinish(qrResult.text)
                    }
                })
            }

        cameraProvider.unbindAll()

        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis)
        preview.setSurfaceProvider(previewView.surfaceProvider)
    }

    private fun addTokenAndFinish(text: String?) {
        var token: Token? = null
        try {
            token = Token(text)
        } catch (e: Token.TokenUriInvalidException) {
            e.printStackTrace()
        }

        if (token == null) {
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

    companion object {
        fun hasCamera(context: Context): Boolean {
            val pm = context.packageManager
            return pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
        }
    }
}