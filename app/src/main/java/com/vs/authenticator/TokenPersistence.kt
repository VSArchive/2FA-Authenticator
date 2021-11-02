package com.vs.authenticator

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.squareup.picasso.Picasso
import com.vs.authenticator.Token.TokenUriInvalidException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.*
import kotlin.coroutines.CoroutineContext

class TokenPersistence(ctx: Context) {
    private val prefs: SharedPreferences =
        ctx.applicationContext.getSharedPreferences(NAME, Context.MODE_PRIVATE)
    private val gson: Gson = Gson()
    private val tokenOrder: List<String>
        get() {
            val type = object : TypeToken<List<String?>?>() {}.type
            val str = prefs.getString(ORDER, "[]")
            val order = gson.fromJson<List<String>>(str, type)
            return order ?: LinkedList()
        }

    private fun setTokenOrder(order: List<String>): SharedPreferences.Editor {
        return prefs.edit().putString(ORDER, gson.toJson(order))
    }

    fun length(): Int {
        return tokenOrder.size
    }

    fun tokenExists(token: Token): Boolean {
        return prefs.contains(token.id)
    }

    operator fun get(position: Int): Token? {
        val key = tokenOrder[position]
        val str = prefs.getString(key, null)
        try {
            return gson.fromJson(str, Token::class.java)
        } catch (jse: JsonSyntaxException) {
            // Backwards compatibility for URL-based persistence.
            try {
                return Token(str, true)
            } catch (tokenUriInvalid: TokenUriInvalidException) {
                tokenUriInvalid.printStackTrace()
            }
        }
        return null
    }

    fun save(token: Token) {
        val key = token.id

        //if token exists, just update it
        if (prefs.contains(key)) {
            prefs.edit().putString(token.id, gson.toJson(token)).apply()
            return
        }
        val order = tokenOrder.toMutableList()
        order.add(0, key)
        setTokenOrder(order).putString(key, gson.toJson(token)).apply()
    }

    fun move(fromPosition: Int, toPosition: Int) {
        if (fromPosition == toPosition) return
        val order = tokenOrder.toMutableList()
        if (fromPosition < 0 || fromPosition > order.size) return
        if (toPosition < 0 || toPosition > order.size) return
        order.add(toPosition, order.removeAt(fromPosition))
        setTokenOrder(order).apply()
    }

    fun delete(position: Int) {
        val order = tokenOrder.toMutableList()
        val key: String = order.removeAt(position)
        setTokenOrder(order).remove(key).apply()
    }

    /**
     * Data class for SaveTokenTask
     */
    private class ReturnParams(val token: Token, val context: Context)

    /**
     * Data class for SaveTokenTask
     */
    private class TaskParams(val token: Token, val outFile: File?, val context: Context)

    /**
     * Downloads/copies images to FreeOTP storage
     * Saves token in PostExecute
     */

    private class SaveTokenTask(override val coroutineContext: CoroutineContext) : CoroutineScope {
        suspend fun doInBackground(params: TaskParams): ReturnParams = withContext(Dispatchers.IO) {
            if (params.token.image != null) {
                try {
                    val bitmap = Picasso.with(params.context)
                        .load(
                            params.token.image
                        )
                        .resize(200, 200) // it's just an icon
                        .onlyScaleDown() //resize image, if bigger than 200x200
                        .get()
                    val outFile = params.outFile
                    //saveAsync image
                    val out = FileOutputStream(outFile, false)
                    bitmap.compress(Bitmap.CompressFormat.PNG, 50, out)
                    out.close()
                    params.token.image = Uri.fromFile(outFile)
                } catch (e: Exception) {
                    e.printStackTrace()
                    //set image to null to prevent internet link in image, in case image
                    //was scanned, when no connection existed
                    params.token.image = null
                }
            }
            return@withContext ReturnParams(params.token, params.context)
        }
    }

    companion object {
        private const val NAME = "tokens"
        private const val ORDER = "tokenOrder"


        fun saveAsync(context: Context, token: Token) {
            var outFile: File? = null
            if (token.image != null) outFile =
                File(context.filesDir, "img_" + UUID.randomUUID().toString() + ".png")

            var returnParams: ReturnParams
            runBlocking {
                returnParams =
                    SaveTokenTask(CoroutineScope(Dispatchers.IO).coroutineContext).doInBackground(
                        TaskParams(token, outFile, context)
                    )
            }

            //we downloaded the image, now save it normally
            TokenPersistence(returnParams.context).save(returnParams.token)
            //refresh TokenAdapter
            returnParams.context.sendBroadcast(Intent(MainActivity.ACTION_IMAGE_SAVED))
        }
    }
}