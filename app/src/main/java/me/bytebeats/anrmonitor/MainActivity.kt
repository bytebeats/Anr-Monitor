package me.bytebeats.anrmonitor

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import me.bytebeats.anr.AnrLog

class MainActivity : AppCompatActivity() {
    private val deadLockAnr by lazy { findViewById<TextView>(R.id.dead_lock_anr) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        deadLockAnr.setOnClickListener {
            AnrLog.logd("createDeadLockAnr")
            DeadLockUtils.createDeadLockAnr()
        }
    }
}