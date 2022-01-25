package com.vs.authenticator

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.database.DataSetObserver
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.GridView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.vs.authenticator.Token.TokenUriInvalidException
import com.vs.authenticator.add.ScanActivity

class MainActivity : AppCompatActivity(), MenuItem.OnMenuItemClickListener {
    private var mTokenAdapter: TokenAdapter? = null
    private var mDataSetObserver: DataSetObserver? = null
    private var receiver: RefreshListBroadcastReceiver? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onNewIntent(intent)
        setContentView(R.layout.main)
        mTokenAdapter = TokenAdapter(this)
        receiver = RefreshListBroadcastReceiver()
        registerReceiver(receiver, IntentFilter(ACTION_IMAGE_SAVED))
        (findViewById<View>(R.id.grid) as GridView).adapter = mTokenAdapter

        // Don't permit screenshots since these might contain OTP codes.
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        mDataSetObserver = object : DataSetObserver() {
            override fun onChanged() {
                super.onChanged()
                if (mTokenAdapter!!.count == 0) findViewById<View>(android.R.id.empty).visibility =
                    View.VISIBLE else findViewById<View>(android.R.id.empty).visibility = View.GONE
            }
        }
        mTokenAdapter!!.registerDataSetObserver(mDataSetObserver)
    }

    override fun onResume() {
        super.onResume()
        mTokenAdapter!!.notifyDataSetChanged()
    }

    override fun onPause() {
        super.onPause()
        mTokenAdapter!!.notifyDataSetChanged()
    }

    override fun onDestroy() {
        super.onDestroy()
        mTokenAdapter!!.unregisterDataSetObserver(mDataSetObserver)
        unregisterReceiver(receiver)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        menu.findItem(R.id.action_scan).isVisible = ScanActivity.hasCamera(this)
        menu.findItem(R.id.action_scan).setOnMenuItemClickListener(this)
        return true
    }

    private fun tryOpenCamera() {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), PERMISSIONS_REQUEST_CAMERA)
        } else {
            // permission is already granted
            openCamera()
        }
    }

    private fun openCamera() {
        startActivity(Intent(this, ScanActivity::class.java))
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        return if (item.itemId == R.id.action_scan) {
            tryOpenCamera()
            true
        } else {
            false
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CAMERA) {
            if (grantResults.isNotEmpty()
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                openCamera()
            } else {
                Toast.makeText(
                    this@MainActivity,
                    R.string.error_permission_camera_open,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val uri = intent.data
        if (uri != null) {
            try {
                TokenPersistence.saveAsync(this, Token(uri))
            } catch (e: TokenUriInvalidException) {
                e.printStackTrace()
            }
        }
    }

    private inner class RefreshListBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            mTokenAdapter!!.notifyDataSetChanged()
        }
    }

    companion object {
        const val ACTION_IMAGE_SAVED = "ACTION_IMAGE_SAVED"
        const val PERMISSIONS_REQUEST_CAMERA = 1
    }
}