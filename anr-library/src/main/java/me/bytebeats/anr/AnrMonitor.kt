package me.bytebeats.anr

import android.os.Debug
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

/**
 * Created by bytebeats on 2021/6/30 : 12:26
 * E-mail: happychinapc@gmail.com
 * Quote: Peasant. Educated. Worker
 */

/**
 * Constructs a watchdog that checks the ui thread every given interval
 *
 * @param timeoutInterval The interval, in milliseconds, between to checks of the UI thread.
 *                        It is therefore the maximum time the UI may freeze before being reported as ANR.
 */
class AnrMonitor(private val timeoutInterval: Long = DEFAULT_ANR_TIMEOUT) : Thread(),
    LifecycleEventObserver {

    init {
        name = "||ANR-Monitor||"
    }

    private var mAnrInterceptor: AnrInterceptor? = DEFAULT_ANR_INTERCEPTOR
    private var mAnrListener: AnrListener? = DEFAULT_ANR_LISTENER
    private var mInterruptedListener: OnInterruptedListener? = DEFAULT_INTERRUPTION_LISTENER

    private val mainHandler = Handler(Looper.getMainLooper())

    private var mPrefix: String? = ""
    private var mLogThreadWithoutStackTrace = false
    private var mIgnoreDebugger = false

    @Volatile
    private var mTick = 0L

    @Volatile
    private var mReported = false

    @Volatile
    private var mStopped = false

    private val mTicker = Runnable {
        mTick = 0
        mReported = false
    }

    fun setAnrListener(anrListener: AnrListener?): AnrMonitor {
        mAnrListener = anrListener ?: DEFAULT_ANR_LISTENER
        return this
    }

    fun setAnrInterceptor(anrInterceptor: AnrInterceptor?): AnrMonitor {
        mAnrInterceptor = anrInterceptor ?: DEFAULT_ANR_INTERCEPTOR
        return this
    }

    fun setOnInterruptedListener(onInterruptedListener: OnInterruptedListener?): AnrMonitor {
        mInterruptedListener = onInterruptedListener ?: DEFAULT_INTERRUPTION_LISTENER
        return this
    }

    /**
     * Set the prefix that a thread's name must have for the thread to be reported.
     * Note that the main thread is always reported.
     * Default "".
     *
     * @param prefix The thread name's prefix for a thread to be reported.
     * @return itself for chaining.
     */
    fun setReportThreadNamePrefix(prefix: String): AnrMonitor {
        mPrefix = prefix
        return this
    }

    /**
     * Set that only the main thread will be reported.
     *
     * @return itself for chaining.
     */
    fun setReportMainThreadOnly(): AnrMonitor {
        mPrefix = null
        return this
    }

    /**
     * Set that all threads will be reported (default behaviour).
     *
     * @return itself for chaining.
     */
    fun setReportAllThreads(): AnrMonitor {
        mPrefix = ""
        return this
    }

    /**
     * Set that all running threads will be reported,
     * even those from which no stack trace could be extracted.
     * Default false.
     *
     * @param logThreadsWithoutStackTrace Whether or not all running threads should be reported
     * @return itself for chaining.
     */
    fun setLogThreadWithoutStackTrace(logThreadsWithoutStackTrace: Boolean): AnrMonitor {
        mLogThreadWithoutStackTrace = logThreadsWithoutStackTrace
        return this
    }

    /**
     * Set whether to ignore the debugger when detecting ANRs.
     * When ignoring the debugger, ANRWatchdog will detect ANRs even if the debugger is connected.
     * By default, it does not, to avoid interpreting debugging pauses as ANRs.
     * Default false.
     *
     * @param ignoreDebugger Whether to ignore the debugger.
     * @return itself for chaining.
     */
    fun setIgnoreDebugger(ignoreDebugger: Boolean): AnrMonitor {
        mIgnoreDebugger = ignoreDebugger
        return this
    }

    override fun run() {
        var interval = timeoutInterval
        while (!isInterrupted) {
            if (mStopped) {
                try {
                    Thread.sleep(timeoutInterval)
                } catch (e: InterruptedException) {
                    mInterruptedListener?.onInterrupted(e)
                    return
                }
                continue
            }
            val needPost = mTick == 0L
            mTick += interval
            if (needPost) {
                mainHandler.post(mTicker)
            }
            try {
                Thread.sleep(timeoutInterval)
            } catch (e: InterruptedException) {
                mInterruptedListener?.onInterrupted(e)
                return
            }
            // If the main thread has not handled _ticker, it is blocked. ANR.
            if (mTick != 0L && !mReported) {
                //noinspection ConstantConditions
                if (!mIgnoreDebugger && (Debug.isDebuggerConnected() || Debug.waitingForDebugger())) {
                    AnrLog.logd("An ANR was detected but ignored because the debugger is connected (you can prevent this with setIgnoreDebugger(true))")
                    mReported = true
                    continue
                }
                interval = mAnrInterceptor?.intercept(mTick) ?: 0
                if (interval > 0) {
                    continue
                }
                val anrError = if (mPrefix == null) {
                    AnrError.newMainInstance(mTick)
                } else {
                    AnrError.newInstance(mTick, mPrefix!!, mLogThreadWithoutStackTrace)
                }
                mAnrListener?.onAppNotResponding(anrError)
                interval = timeoutInterval
                mReported = true
            }
        }
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_CREATE) {//app is created
            onAppCreate()
        } else if (event == Lifecycle.Event.ON_STOP) {//no activities in stack
            onAppStop()
        } else if (event == Lifecycle.Event.ON_START) {
            onAppStart()
        }
    }

    private fun onAppStart() {
        mStopped = false
    }

    private fun onAppCreate() {
        this.start()
    }

    private fun onAppStop() {
        mStopped = true
    }

    @Synchronized
    override fun start() {
        if (isStarted()) {
            return
        }
        super.start()
    }

    private fun isStarted(): Boolean {
        return state.ordinal > State.NEW.ordinal && state.ordinal < State.TERMINATED.ordinal
    }

    companion object {
        const val DEFAULT_ANR_TIMEOUT = 5000L

        private val DEFAULT_ANR_LISTENER = object : AnrListener {
            override fun onAppNotResponding(error: AnrError) {
                throw error
            }
        }

        private val DEFAULT_INTERRUPTION_LISTENER = object : OnInterruptedListener {
            override fun onInterrupted(e: InterruptedException) {
                AnrLog.logd(e.message)
            }
        }

        private val DEFAULT_ANR_INTERCEPTOR = object : AnrInterceptor {
            override fun intercept(duration: Long): Long {
                return 0
            }
        }
    }
}