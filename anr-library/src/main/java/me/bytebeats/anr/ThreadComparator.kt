package me.bytebeats.anr

import android.os.Looper
import java.util.*

/**
 * Created by bytebeats on 2021/6/30 : 12:08
 * E-mail: happychinapc@gmail.com
 * Quote: Peasant. Educated. Worker
 */

/**
 * Thread Comparator: Main Thread is first, others ordered by Thread names.
 */
class ThreadComparator : Comparator<Thread> {
    override fun compare(o1: Thread, o2: Thread): Int {
        if (o1 == o2) return 0
        if (o1 == Looper.getMainLooper().thread) return 1
        if (o2 == Looper.getMainLooper().thread) return -1
        return o2.name.compareTo(o1.name)
    }
}