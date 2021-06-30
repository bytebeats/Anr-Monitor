package me.bytebeats.anrmonitor

import android.os.Bundle
import android.os.Handler
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import me.bytebeats.anr.AnrLog

class MainActivity : AppCompatActivity() {
    private val deadLockAnr by lazy { findViewById<TextView>(R.id.dead_lock_anr) }
    private val minAnrDuration by lazy { findViewById<TextView>(R.id.min_anr_duration) }
    private val reportMode by lazy { findViewById<TextView>(R.id.report_mode) }
    private val behavior by lazy { findViewById<TextView>(R.id.behavior) }
    private val threadSleep by lazy { findViewById<TextView>(R.id.thread_sleep) }
    private val infiniteLoop by lazy { findViewById<TextView>(R.id.infinite_loop) }
    private val deadLock by lazy { findViewById<TextView>(R.id.dead_lock) }

    private val mMutex = Any()

    private fun sleepFor8s() {
        try {
            Thread.sleep(8L * 1000L)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    private fun infiniteLoop() {
        var i = 0
        while (true) {
            i++
        }
    }

    private inner class LockerThread() : Thread("Activity: Locker") {
        override fun run() {
            synchronized(mMutex) {
                while (true) sleepFor8s()
            }
        }
    }

    private fun deadLock() {
        LockerThread().start()
        Handler().post { synchronized(mMutex) { AnrLog.logd("There should be a dead lock before this message") } }
    }

    private var mode = 0
    private var crash = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val anrApp = application as APMApplication
        deadLockAnr.setOnClickListener {
            AnrLog.logd("createDeadLockAnr")
            DeadLockUtils.createDeadLockAnr()
        }
        deadLock.setOnClickListener { deadLock() }
        infiniteLoop.setOnClickListener { infiniteLoop() }
        threadSleep.setOnClickListener { sleepFor8s() }
        behavior.text = "Crash"
        behavior.setOnClickListener {
            crash = !crash
            if (crash) {
                behavior.text = "Crash"
                anrApp.anrMonitor.setAnrListener(null)
            } else {
                behavior.text = "Silent"
                anrApp.anrMonitor.setAnrListener(anrApp.silentAnrListener)
            }
        }
        reportMode.text = "All Threads"
        reportMode.setOnClickListener {
            mode = (mode + 1) % 3
            when (mode) {
                0 -> {
                    reportMode.text = "All Threads"
                    anrApp.anrMonitor.setReportAllThreads()
                }
                1 -> {
                    reportMode.text = "Main Thread only"
                    anrApp.anrMonitor.setReportMainThreadOnly()
                }
                2 -> {
                    reportMode.text = "Filtered"
                    anrApp.anrMonitor.setReportThreadNamePrefix("Activity:")
                }
            }
        }
        minAnrDuration.text = "${anrApp.duration} seconds"
        minAnrDuration.setOnClickListener {
            anrApp.duration = anrApp.duration % 6 + 2
            minAnrDuration.text = "${anrApp.duration} seconds"
        }
    }
}