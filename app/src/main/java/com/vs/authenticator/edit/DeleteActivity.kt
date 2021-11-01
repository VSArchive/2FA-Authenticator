package com.vs.authenticator.edit

import android.os.Bundle
import android.view.View
import com.vs.authenticator.R
import com.vs.authenticator.TokenPersistence

class DeleteActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.delete)
        val token = TokenPersistence(this)[position]
        findViewById<View>(R.id.cancel).setOnClickListener { finish() }
        findViewById<View>(R.id.delete).setOnClickListener {
            //delete the image that was copied to storage, before deleting the token
            token!!.deleteImage()
            TokenPersistence(this@DeleteActivity).delete(position)
            finish()
        }
    }
}