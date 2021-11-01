package com.vs.authenticator

class TokenCode(private val mCode: String?, private val mStart: Long, private val mUntil: Long) {
    private var mNext: TokenCode? = null

    constructor(code: String?, start: Long, until: Long, next: TokenCode?) : this(
        code,
        start,
        until
    ) {
        mNext = next
    }

    val currentCode: String?
        get() {
            val active = getActive(System.currentTimeMillis()) ?: return null
            return active.mCode
        }
    val totalProgress: Int
        get() {
            val cur = System.currentTimeMillis()
            val total = last.mUntil - mStart
            val state = total - (cur - mStart)
            return (state * 1000 / total).toInt()
        }
    val currentProgress: Int
        get() {
            val cur = System.currentTimeMillis()
            val active = getActive(cur) ?: return 0
            val total = active.mUntil - active.mStart
            val state = total - (cur - active.mStart)
            return (state * 1000 / total).toInt()
        }

    private fun getActive(curTime: Long): TokenCode? {
        if (curTime in mStart until mUntil) return this
        return if (mNext == null) null else mNext!!.getActive(curTime)
    }

    private val last: TokenCode
        get() = if (mNext == null) this else mNext!!.last
}