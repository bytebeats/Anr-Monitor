package me.bytebeats.anrmonitor

import android.app.Application
import android.util.Log
import androidx.lifecycle.ProcessLifecycleOwner
import me.bytebeats.anr.AnrError
import me.bytebeats.anr.AnrInterceptor
import me.bytebeats.anr.AnrListener
import me.bytebeats.anr.AnrLog
import me.bytebeats.anr.AnrMonitor
import me.bytebeats.anr.OnInterruptedListener
import me.bytebeats.lag.LagMonitor
import me.bytebeats.lag.OnFrameJankListener
import me.bytebeats.lag.OnProcessNotRespondingListener
import me.bytebeats.lag.OnUIThreadBlockListener
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.ObjectOutputStream

/**
 * Created by bytebeats on 2021/6/30 : 15:23
 * E-mail: happychinapc@gmail.com
 * Quote: Peasant. Educated. Worker
 */

class APMApplication : Application() {
    val anrMonitor = AnrMonitor(3000)

    val silentAnrListener = object : AnrListener {
        override fun onAppNotResponding(error: AnrError) {
            Log.d("anr-log", "onAppNotResponding", error)
        }
    }

    var duration = 4L

    override fun onCreate() {
        super.onCreate()
        AnrLog.logStackTrace = false
        anrMonitor.setIgnoreDebugger(true)
            .setReportAllThreads()
            .setAnrListener(object : AnrListener {
                override fun onAppNotResponding(error: AnrError) {
                    AnrLog.logd("onAppNotResponding")
                    try {
                        ObjectOutputStream(ByteArrayOutputStream()).writeObject(error)
                    } catch (e: IOException) {
                        throw RuntimeException(e)
                    }
                    AnrLog.logd("Anr Error was successfully serialized")
                    throw error
                }
            }).setAnrInterceptor(object : AnrInterceptor {
                override fun intercept(duration: Long): Long {
                    val ret = this@APMApplication.duration - duration
                    if (ret > 0) {
                        AnrLog.logd(
                            "Intercepted ANR that is too short ($duration ms), postponing for $ret ms."
                        )
                    }
                    return ret
                }
            }).setOnInterruptedListener(object : OnInterruptedListener {
                override fun onInterrupted(e: InterruptedException) {
                    throw e
                }
            })
//        ProcessLifecycleOwner.get().lifecycle.addObserver(anrMonitor)

        val lagMonitor = LagMonitor.Builder(this.applicationContext)
            .setThresholdTimeMillis(3L)
            .setLagLogEnabled(true)
            .setMonitorMode(LagMonitor.MonitorMode.UI)
            .setOnFrameJankListener(object : OnFrameJankListener {
                override fun onJank(janks: Int) {
                    Log.d("lag-log", "janks: $janks")
                }
            })
            .setOnUIThreadRunListener(object : OnUIThreadBlockListener {
                override fun onBlock(lagTime: Long, uiRunTime: Long) {
                    Log.d("lag-log", "lagTime: $lagTime,  uiRunTime: $uiRunTime")
                }
            })
            .setOnProcessNotRespondingListener(object : OnProcessNotRespondingListener {
                override fun onNotResponding(processInfo: String?) {
                    Log.d("lag-log", "processInfo: $processInfo")
                }
            })
            .build()
//        ProcessLifecycleOwner.get().lifecycle.addObserver(lagMonitor)
    }
}