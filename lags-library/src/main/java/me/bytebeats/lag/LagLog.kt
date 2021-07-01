package me.bytebeats.lag

import android.util.Log

/**
 * Created by Pan Chen on 2021/7/1 : 14:27
 * E-mail: panchen@itiger.com
 * Company: https://www.itiger.com
 */
object LagLog {
    var logStackTrace = true
    var enabled = true
    private const val TAG = "lag-log"

    private enum class LEVEL {
        V, D, I, W, E;
    }

    fun logv(message: Any?) = logv(TAG, message)
    fun logd(message: Any?) = logd(TAG, message)
    fun logi(message: Any?) = logi(TAG, message)
    fun logw(message: Any?) = logw(TAG, message)
    fun loge(message: Any?) = loge(TAG, message)

    fun logv(tag: String, message: Any?) = log(LEVEL.V, tag, message.toString())
    fun logd(tag: String, message: Any?) = log(LEVEL.D, tag, message.toString())
    fun logi(tag: String, message: Any?) = log(LEVEL.I, tag, message.toString())
    fun logw(tag: String, message: Any?) = log(LEVEL.W, tag, message.toString())
    fun loge(tag: String, message: Any?) = log(LEVEL.E, tag, message.toString())

    private fun log(level: LEVEL, tag: String, message: String) {
        if (!enabled) {
            return
        }
        val tagBuilder = StringBuilder()
        tagBuilder.append(tag)
        if (logStackTrace) {
            val stackTrace = Thread.currentThread().stackTrace[5]
            tagBuilder.append(" ${stackTrace.methodName}(${stackTrace.fileName}:${stackTrace.lineNumber})")
        }
        when (level) {
            LEVEL.V -> Log.v(tagBuilder.toString(), message)
            LEVEL.D -> Log.d(tagBuilder.toString(), message)
            LEVEL.I -> Log.i(tagBuilder.toString(), message)
            LEVEL.W -> Log.w(tagBuilder.toString(), message)
            LEVEL.E -> Log.e(tagBuilder.toString(), message)
        }
    }

}