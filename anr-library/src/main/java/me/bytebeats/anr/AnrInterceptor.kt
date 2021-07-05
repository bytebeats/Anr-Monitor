package me.bytebeats.anr

/**
 * Created by bytebeats on 2021/6/30 : 11:29
 * E-mail: happychinapc@gmail.com
 * Quote: Peasant. Educated. Worker
 */

/**
 * To report ANR immediately or lazily when ANRs is detected.
 */
interface AnrInterceptor {
    /**
     *
     * Called when main thread has froze more time than defined by the timeout.
     * @param duration The minimum time (in ms) the main thread has been frozen (may be more).
     * @return 0 or negative if the ANR should be reported immediately. A positive number of ms to postpone the reporting.
     */
    fun intercept(duration: Long): Long
}