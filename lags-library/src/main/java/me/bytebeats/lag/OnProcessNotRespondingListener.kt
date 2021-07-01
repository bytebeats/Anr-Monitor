package me.bytebeats.lag

/**
 * Created by Pan Chen on 2021/7/1 : 16:26
 * E-mail: panchen@itiger.com
 * Company: https://www.itiger.com
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