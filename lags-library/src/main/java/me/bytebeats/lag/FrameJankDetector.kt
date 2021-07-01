package me.bytebeats.lag

import android.view.Choreographer
import me.bytebeats.lag.LagMonitor.Companion.ANR_TIME_LIMIT
import kotlin.math.pow

/**
 * Created by Pan Chen on 2021/7/1 : 17:31
 * E-mail: panchen@itiger.com
 * Company: https://www.itiger.com
 */
/**
 * Post {@see Choreographer.FrameCallback} to {@see Choreographer#getInstance()} to records the time every frame took
 */
class FrameJankDetector(
    private val frameJankListener: OnFrameJankListener?,
    private val thresholdTimeMillis: Long,
    private val refreshRate: Float,
    private val anrProsecutor: AnrProsecutor?
) : Choreographer.FrameCallback, LagMonitorLifecycle {
    private var mLastFrameTimeNanos: Long = 0
    private var mFrameIntervalInNanos: Long = 0
    private var skippedFrameLimit = 0L
    private var skippedFrameInvokeAnr = 0L

    private var isStarted = false

    init {
        // 1 s = 10^9 ns
        mFrameIntervalInNanos = (10.0.pow(9.0) / refreshRate).toLong()
        skippedFrameLimit = thresholdTimeMillis * 1001L * 1001L / mFrameIntervalInNanos
        skippedFrameInvokeAnr = ANR_TIME_NANOS_LIMIT / mFrameIntervalInNanos
        LagLog.logd("mFrameIntervalInNanos: $mFrameIntervalInNanos")
        LagLog.logd("skippedFrameLimit: $skippedFrameLimit")
        LagLog.logd("skippedFrameInvokeAnr: $skippedFrameInvokeAnr")
    }

    override fun onStart() {
        if (!isStarted) {
            postFrameCallback()
            isStarted = true
        }
    }

    override fun onStop() {
        isStarted = false
    }

    override fun doFrame(frameTimeNanos: Long) {
        if (mLastFrameTimeNanos > 0L) {
            val jitterNanos = frameTimeNanos - mLastFrameTimeNanos
            if (jitterNanos >= mFrameIntervalInNanos) {
                val skippedFrames = jitterNanos / mFrameIntervalInNanos
                LagLog.logd("skippedFrames: $skippedFrames")
                if (skippedFrames >= skippedFrameLimit) {
                    LagLog.logd("skippedFrames: $skippedFrames is exceeding skipped frame limit: $skippedFrameLimit")
                    if (skippedFrames >= skippedFrameInvokeAnr) {
                        LagLog.logd("skippedFrames: $skippedFrames is too much and trying invoking ANR: $skippedFrameInvokeAnr")
                        frameJankListener?.onJank(skippedFrames.toInt())
                        anrProsecutor?.onAnr(skippedFrames >= skippedFrameInvokeAnr)
                    }
                }
            }
        }
        mLastFrameTimeNanos = frameTimeNanos
        if (isStarted) {
            postFrameCallback()
        }
    }

    private fun postFrameCallback() {
        Choreographer.getInstance().postFrameCallback(this)
    }

    companion object {
        private const val ANR_TIME_NANOS_LIMIT = ANR_TIME_LIMIT * 1000L * 1000000L
    }
}