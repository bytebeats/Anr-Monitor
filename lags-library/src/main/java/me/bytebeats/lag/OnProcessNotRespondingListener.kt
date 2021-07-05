package me.bytebeats.lag

/**
 * Created by bytebeats on 2021/7/1 : 16:26
 * E-mail: happychinapc@gmail.com
 * Quote: Peasant. Educated. Worker
 */

/**
 * To provide information of processes which are not responding when lags happen on UI thread.
 */
interface OnProcessNotRespondingListener {
    /**
     * provide information for process not responding
     */
    fun onNotResponding(processInfo: String?)
}