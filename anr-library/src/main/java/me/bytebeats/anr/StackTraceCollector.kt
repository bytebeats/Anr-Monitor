package me.bytebeats.anr

import java.io.Serializable

/**
 * Created by Pan Chen on 2021/6/30 : 11:47
 * E-mail: panchen@itiger.com
 * Company: https://www.itiger.com
 */
class StackTraceCollector(
    private val name: String,
    private val stackTraces: Array<StackTraceElement>
) : Serializable {
    inner class StackTraceThrowable(private val other: StackTraceThrowable?) :
        Throwable(name, other) {
        override fun fillInStackTrace(): Throwable {
            stackTrace = stackTraces
            return this
        }
    }
}