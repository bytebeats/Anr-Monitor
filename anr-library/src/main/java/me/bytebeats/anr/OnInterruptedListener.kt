package me.bytebeats.anr

/**
 * Created by bytebeats on 2021/6/30 : 11:36
 * E-mail: happychinapc@gmail.com
 * Quote: Peasant. Educated. Worker
 */

/**
 * Anr-monitor run on a Thread which may be interrupted.
 */
interface OnInterruptedListener {
    /**
     * Called when Anr-monitor Thread is interrupted.
     */
    fun onInterrupted(e: InterruptedException)
}