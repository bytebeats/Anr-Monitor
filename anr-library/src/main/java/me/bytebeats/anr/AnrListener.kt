package me.bytebeats.anr

/**
 * Created by Pan Chen on 2021/6/30 : 12:24
 * E-mail: panchen@itiger.com
 * Company: https://www.itiger.com
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