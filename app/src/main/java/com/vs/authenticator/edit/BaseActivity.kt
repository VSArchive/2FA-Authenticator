package com.vs.authenticator.edit

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity : AppCompatActivity() {
    protected var position = 0
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get the position of the token. This MUST exist.
        position = intent.getIntExtra(EXTRA_POSITION, -1)
    }

    companion object {
        const val EXTRA_POSITION = "position"
    }
}