package com.vs.authenticator

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.vs.authenticator.Token.TokenUriInvalidException
import java.util.*

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

    companion object {
        private const val NAME = "tokens"
        private const val ORDER = "tokenOrder"

        fun saveAsync(context: Context, token: Token) {
            TokenPersistence(context).save(token)
        }
    }
}