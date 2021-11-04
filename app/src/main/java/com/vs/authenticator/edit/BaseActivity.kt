package com.vs.authenticator.edit

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.fotoapparat.BuildConfig

abstract class BaseActivity : AppCompatActivity() {
    protected var position = 0
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get the position of the token. This MUST exist.
        position = intent.getIntExtra(EXTRA_POSITION, -1)
        if (BuildConfig.DEBUG && position < 0) throw RuntimeException("Could not create BaseActivity")
    }

    companion object {
        const val EXTRA_POSITION = "position"
    }
}