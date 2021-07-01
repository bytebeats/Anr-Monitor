package me.bytebeats.lag

/**
 * Created by Pan Chen on 2021/7/1 : 17:04
 * E-mail: panchen@itiger.com
 * Company: https://www.itiger.com
 */
interface OnUIThreadBlockListener {
    fun onBlock(lagTime: Long, uiRunTime: Long)
}