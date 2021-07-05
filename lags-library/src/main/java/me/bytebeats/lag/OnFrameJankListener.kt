package me.bytebeats.lag

/**
 * Created by bytebeats on 2021/7/1 : 16:31
 * E-mail: happychinapc@gmail.com
 * Quote: Peasant. Educated. Worker
 */

/**
 * To detect whether frame janks happened
 */
interface OnFrameJankListener {
    /**
     * Called when frame janks happened
     */
    fun onJank(janks: Int)
}