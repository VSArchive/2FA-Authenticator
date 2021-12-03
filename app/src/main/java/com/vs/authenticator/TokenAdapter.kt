package com.vs.authenticator

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.database.DataSetObserver
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.vs.authenticator.edit.BaseActivity.Companion.EXTRA_POSITION
import com.vs.authenticator.edit.DeleteActivity
import com.vs.authenticator.edit.EditActivity
import java.util.*

class TokenAdapter(ctx: Context) : BaseReorderAdapter() {
    private val mTokenPersistence: TokenPersistence = TokenPersistence(ctx)
    private val mLayoutInflater: LayoutInflater =
        ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private val mClipMan: ClipboardManager =
        ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    private val mTokenCodes: MutableMap<String, TokenCode>
    override fun getCount(): Int {
        return mTokenPersistence.length()
    }

    override fun getItem(position: Int): Token {
        return mTokenPersistence[position]!!
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun move(fromPosition: Int, toPosition: Int) {
        mTokenPersistence.move(fromPosition, toPosition)
        notifyDataSetChanged()
    }

    override fun bindView(view: View?, position: Int) {
        val ctx = view!!.context
        val tokenLayout = view as TokenLayout?
        val token = getItem(position)
        tokenLayout!!.bind(token, R.menu.token) { item: MenuItem ->
            val i: Intent
            when (item.itemId) {
                R.id.action_edit -> {
                    i = Intent(ctx, EditActivity::class.java)
                    i.putExtra(EXTRA_POSITION, position)
                    ctx.startActivity(i)
                }
                R.id.action_delete -> {
                    i = Intent(ctx, DeleteActivity::class.java)
                    i.putExtra(EXTRA_POSITION, position)
                    ctx.startActivity(i)
                }
            }
            true
        }
        tokenLayout.setOnClickListener { v: View ->
            val tp = TokenPersistence(ctx)

            // Increment the token.
            val token1 = tp[position]
            val codes = token1!!.generateCodes()
            //save token. Image wasn't changed here, so just save it in sync
            TokenPersistence(ctx).save(token1)

            // Copy code to clipboard.
            mClipMan.setPrimaryClip(ClipData.newPlainText(null, codes.currentCode))
            Toast.makeText(
                v.context.applicationContext,
                R.string.code_copied,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun createView(parent: ViewGroup?, type: Int): View {
        return mLayoutInflater.inflate(R.layout.token, parent, false)
    }

    init {
        mTokenCodes = HashMap()
        registerDataSetObserver(object : DataSetObserver() {
            override fun onChanged() {
                mTokenCodes.clear()
            }

            override fun onInvalidated() {
                mTokenCodes.clear()
            }
        })
    }
}