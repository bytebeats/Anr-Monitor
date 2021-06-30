package me.bytebeats.anrmonitor

import android.app.Application
import android.util.Log
import me.bytebeats.anr.AnrError
import me.bytebeats.anr.AnrInterceptor
import me.bytebeats.anr.AnrListener
import me.bytebeats.anr.AnrMonitor

/**
 * Created by Pan Chen on 2021/6/30 : 15:23
 * E-mail: panchen@itiger.com
 * Company: https://www.itiger.com
 */
class APMApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AnrMonitor().setIgnoreDebugger(true)
            .setReportAllThreads()
            .setAnrListener(object : AnrListener {
                override fun onAppNotResponding(error: AnrError) {
                    for (element in error.stackTrace) {
                    }
                }
            }).setAnrInterceptor(object : AnrInterceptor {
                override fun intercept(duration: Long): Long {
                    val ret = 5000 - duration
                    if (ret > 0) {
                        Log.w(
                            "AAA",
                            "Intercepted ANR that is too short (" + duration + " ms), postponing for " + ret + " ms."
                        )
                    }
                    return ret
                }
            }).start()
    }
}