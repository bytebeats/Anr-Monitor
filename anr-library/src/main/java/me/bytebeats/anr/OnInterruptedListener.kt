package me.bytebeats.anr

/**
 * Created by Pan Chen on 2021/6/30 : 11:36
 * E-mail: panchen@itiger.com
 * Company: https://www.itiger.com
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