package me.bytebeats.anr

/**
 * Created by bytebeats on 2021/6/30 : 12:24
 * E-mail: happychinapc@gmail.com
 * Quote: Peasant. Educated. Worker
 */

/**
 * To detect whether ANR happened
 */
interface AnrListener {
    /**
     * Called when an ANR is detected.
     *
     * @param error The error describing the ANR.
     */
    fun onAppNotResponding(error: AnrError)
}