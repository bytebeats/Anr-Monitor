package me.bytebeats.anrmonitor

import android.app.Application
import android.util.Log
import me.bytebeats.anr.AnrError
import me.bytebeats.anr.AnrInterceptor
import me.bytebeats.anr.AnrListener
import me.bytebeats.anr.AnrLog
import me.bytebeats.anr.AnrMonitor

/**
 * Created by Pan Chen on 2021/6/30 : 15:23
 * E-mail: panchen@itiger.com
 * Company: https://www.itiger.com
 */
class APMApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AnrLog.logStackTrace = true
        AnrMonitor(2000).setIgnoreDebugger(true)
            .setReportAllThreads()
            .setAnrListener(object : AnrListener {
                override fun onAppNotResponding(error: AnrError) {
                    Log.d("anr-log", "onAppNotResponding", error)
                }
            }).setAnrInterceptor(object : AnrInterceptor {
                override fun intercept(duration: Long): Long {
                    val ret = 4000 - duration
                    if (ret > 0) {
                        AnrLog.logd(
                            "Intercepted ANR that is too short ($duration ms), postponing for $ret ms."
                        )
                    }
                    return ret
                }
            }).start()
    }
}