package me.bytebeats.anrmonitor

import me.bytebeats.anr.AnrLog
import kotlin.concurrent.thread

/**
 * Created by Pan Chen on 2021/6/30 : 18:30
 * E-mail: panchen@itiger.com
 * Company: https://www.itiger.com
 */
/**
 * utility class to create dead lock and anr
 */
object DeadLockUtils {
    private const val TAG = "DeadLockUtils"

    fun createDeadLockAnr() {
        val lock1 = Any()
        val lock2 = Any()
        thread() {
            synchronized(lock1) {
                Thread.sleep(100)
                synchronized(lock2) {
                    AnrLog.logd("ANR: getLock2")
                }
            }
        }
        synchronized(lock2) {
            Thread.sleep(100)
            synchronized(lock1) {
                AnrLog.logd("ANR: getLock1")
            }
        }
    }

    fun createDeadLock() {
        val lock1 = Any()
        val lock2 = Any()
        thread(start = true) {
            synchronized(lock1) {
                Thread.sleep(100)
                synchronized(lock2) {
                    AnrLog.logd("ANR: thead1")
                }
            }
        }
        thread(start = true) {
            synchronized(lock2) {
                Thread.sleep(100)
                synchronized(lock1) {
                    AnrLog.logd("ANR: thead2")
                }
            }
        }
    }
}