package hackathon.morton.jonathan.uncommonhacks2017

import android.util.Log
import java.util.*

object L {

    val TAG = ".tips"

    private var ENABLE_DEBUG_LOGGING = true

    fun enableDebugLogging(enabled: Boolean) {
        ENABLE_DEBUG_LOGGING = enabled
    }

    fun v(msg: String) {
        if (ENABLE_DEBUG_LOGGING) {
            val logMsg = debugInfo() + msg
            Log.v(TAG, logMsg)
        }
    }

    fun d(msg: String) {
        if (ENABLE_DEBUG_LOGGING) {
            val logMsg = debugInfo() + msg
            Log.d(TAG, logMsg)
        }
    }

    fun i(msg: String) {
        val logMsg = debugInfo() + msg
        Log.i(TAG, logMsg)
    }

    fun w(msg: String) {
        val logMsg = debugInfo() + msg
        Log.w(TAG, logMsg)
    }

    fun e(msg: String) {
        val logMsg = debugInfo() + msg
        Log.e(TAG, logMsg)
    }

    fun e(msg: String, e: Throwable) {
        val logMsg = debugInfo() + msg
        Log.e(TAG, logMsg, e)
    }

    fun wtf(msg: String) {
        val logMsg = debugInfo() + msg
        Log.wtf(TAG, logMsg)
    }

    fun wtf(msg: String, exception: Exception) {
        val logMsg = debugInfo() + msg
        Log.wtf(TAG, logMsg, exception)
    }

    private fun debugInfo(): String {
        val stackTrace = Thread.currentThread().stackTrace
        val className = stackTrace[4].className
        val methodName = Thread.currentThread().stackTrace[4].methodName
        val lineNumber = stackTrace[4].lineNumber
        return String.format(Locale.US, "%s.%s:%d ", className, methodName, lineNumber)
    }
}
