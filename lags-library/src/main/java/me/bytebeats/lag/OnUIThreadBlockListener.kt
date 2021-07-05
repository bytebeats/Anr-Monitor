package me.bytebeats.lag

/**
 * Created by bytebeats on 2021/7/1 : 17:04
 * E-mail: happychinapc@gmail.com
 * Quote: Peasant. Educated. Worker
 */

interface OnUIThreadBlockListener {
    fun onBlock(lagTime: Long, uiRunTime: Long)
}