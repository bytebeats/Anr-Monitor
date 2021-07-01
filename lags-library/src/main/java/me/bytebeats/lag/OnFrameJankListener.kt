package me.bytebeats.lag

/**
 * Created by Pan Chen on 2021/7/1 : 16:31
 * E-mail: panchen@itiger.com
 * Company: https://www.itiger.com
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