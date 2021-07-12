package me.bytebeats.lag

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.view.WindowManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import kotlin.math.max
import kotlin.math.min

/**
 * Created by bytebeats on 2021/7/1 : 16:22
 * E-mail: happychinapc@gmail.com
 * Quote: Peasant. Educated. Worker
 */

class LagMonitor private constructor(
    private val context: Context,
    private var thresholdTimeMillis: Long,
    private val monitorMode: MonitorMode,
    private val logWithStackTrace: Boolean,
    private val frameJankListener: OnFrameJankListener?,
    private val uiThreadBlockListener: OnUIThreadBlockListener?,
    private val processNotRespondingListener: OnProcessNotRespondingListener?
) : AnrProsecutor, LifecycleEventObserver {

    private var lagMonitorLifecycle: LagMonitorLifecycle? = null

    init {
        thresholdTimeMillis = min(max(MIN_THREAD_TIME, thresholdTimeMillis), MAX_THREAD_TIME)
        LagLog.logStackTrace = logWithStackTrace
        lagMonitorLifecycle = create()
    }


    private fun create(): LagMonitorLifecycle {
        return if (monitorMode == MonitorMode.FRAME && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            FrameJankDetector(frameJankListener, thresholdTimeMillis, getRefreshRate(), this)
        } else {
            UILooperPrinter(uiThreadBlockListener, thresholdTimeMillis, this)
        }
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_START -> start()
            Lifecycle.Event.ON_STOP -> stop()
        }
    }

    fun start() {
        lagMonitorLifecycle?.onStart()
        LagLog.logd("LagMonitor started")
    }

    fun stop() {
        lagMonitorLifecycle?.onStop()
        LagLog.logd("LagMonitor stopped")
    }

    private fun getRefreshRate(): Float {
        return (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.refreshRate
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {//only if context is Activity
//            context.display?.refreshRate ?: 16F
//        } else {
//        }
    }

    override fun onAnr(suspecting: Boolean) {
        if (suspecting) {
            processNotRespondingListener?.onNotResponding(getNotRespondingProcessInfo())
        }
    }

    private fun getNotRespondingProcessInfo(): String? {
        val mng = (context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
        val errorProcessInforms = mng.processesInErrorState
        if (errorProcessInforms != null) {
            for (processInfo in errorProcessInforms) {
                if (processInfo.condition == ActivityManager.ProcessErrorStateInfo.NOT_RESPONDING) {
                    val info = StringBuilder(processInfo.processName)
                    info.append("\n").append(processInfo.tag)
                        .append("\n").append(processInfo.shortMsg)
                        .append("\n").append(processInfo.longMsg)
                        .append("\n").append(processInfo.stackTrace)
                    LagLog.logd(info)
                    return info.toString()
                }
            }
        }
        return null
    }

    enum class MonitorMode(val value: Int) {
        UI(0), FRAME(1);
    }

    class Builder(val mContext: Context) {
        private var mThresholdTimeMillis: Long = MAX_THREAD_TIME
        private var mMonitorMode: MonitorMode = DEFAULT_MONITOR_MODE
        private var mFrameJankListener: OnFrameJankListener? = null
        private var mProcessNotRespondingListener: OnProcessNotRespondingListener? = null
        private var mUiThreadBlockListener: OnUIThreadBlockListener? = null
        private var mLogWithStackTrace: Boolean = true

        fun setThresholdTimeMillis(thresholdTimeMillis: Long): Builder {
            mThresholdTimeMillis = thresholdTimeMillis
            return this
        }

        fun setMonitorMode(mode: MonitorMode): Builder {
            mMonitorMode = mode
            return this
        }

        fun setOnFrameJankListener(listener: OnFrameJankListener?): Builder {
            mFrameJankListener = listener
            return this
        }

        fun setOnProcessNotRespondingListener(listener: OnProcessNotRespondingListener?): Builder {
            mProcessNotRespondingListener = listener
            return this
        }

        fun setOnUIThreadRunListener(listener: OnUIThreadBlockListener?): Builder {
            mUiThreadBlockListener = listener
            return this
        }

        fun setLogWithStackTrace(logWithStackTrace: Boolean): Builder {
            mLogWithStackTrace = logWithStackTrace
            return this
        }

        fun setLagLogEnabled(enabled: Boolean): Builder {
            LagLog.enabled = enabled
            return this
        }

        fun build(): LagMonitor {
            return LagMonitor(
                mContext,
                mThresholdTimeMillis,
                mMonitorMode,
                mLogWithStackTrace,
                mFrameJankListener,
                mUiThreadBlockListener,
                mProcessNotRespondingListener
            )
        }
    }

    companion object {
        private const val MIN_THREAD_TIME = 500L
        private const val MAX_THREAD_TIME = 500L
        private val DEFAULT_MONITOR_MODE = MonitorMode.UI
        const val ANR_TIME_LIMIT = 5L
    }
}