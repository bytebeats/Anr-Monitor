package me.bytebeats.lag

import android.os.Looper
import android.os.SystemClock
import android.util.Printer
import me.bytebeats.lag.LagMonitor.Companion.ANR_TIME_LIMIT

/**
 * Created by Pan Chen on 2021/7/1 : 19:55
 * E-mail: panchen@itiger.com
 * Company: https://www.itiger.com
 */
/**
 * Changed Looper#mLogging to {@see UILooperPrinter}, so we can record the time every {@see Message} took
 */
class UILooperPrinter(
    private val uiThreadBlockListener: OnUIThreadBlockListener?,
    private val thresholdTimeMillis: Long,
    private val anrProsecutor: AnrProsecutor?
) : Printer, LagMonitorLifecycle {
    private var mLastMessageTimeMillis = 0L
    private var mLastThreadTimeMillis = 0L

    override fun onStart() {
        Looper.getMainLooper().setMessageLogging(this)
    }

    override fun onStop() {
        Looper.getMainLooper().setMessageLogging(null)
    }

    override fun println(x: String?) {
        if (x?.startsWith(LOG_PREFIX) == true) {
            mLastMessageTimeMillis = SystemClock.elapsedRealtime()
            mLastThreadTimeMillis = SystemClock.currentThreadTimeMillis()
        } else if (x?.startsWith(LOG_SUFFIX) == true) {
            if (mLastMessageTimeMillis > 0) {
                val msgElapsedTimeMillis = SystemClock.elapsedRealtime() - mLastMessageTimeMillis
                val thrdElapsedTimeMillis =
                    SystemClock.currentThreadTimeMillis() - mLastThreadTimeMillis
                if (msgElapsedTimeMillis >= thresholdTimeMillis && msgElapsedTimeMillis > ANR_TIME_MILLIS_LIMIT) {
                    uiThreadBlockListener?.onBlock(
                        msgElapsedTimeMillis,
                        thrdElapsedTimeMillis
                    )
                    anrProsecutor?.onAnr(msgElapsedTimeMillis > ANR_TIME_MILLIS_LIMIT)
                }
            }
        }
    }

    companion object {
        private const val ANR_TIME_MILLIS_LIMIT = ANR_TIME_LIMIT * 1000L
        private const val LOG_PREFIX = ">>>>> Dispatching to"
        private const val LOG_SUFFIX = "<<<<< Finished to"
    }
}