package com.kylecorry.andromeda.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import com.kylecorry.andromeda.core.system.PowerUtils
import java.time.Duration

abstract class CustomService: Service() {

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        releaseWakelock()
        super.onDestroy()
    }

    private var wakelock: PowerManager.WakeLock? = null

    fun acquireWakelock(tag: String, duration: Duration? = null){
        try {
            if (wakelock?.isHeld != true) {
                wakelock = PowerUtils.getWakelock(this, tag)
                releaseWakelock()
                if (wakelock?.isHeld == false) {
                    if (duration == null) {
                        wakelock?.acquire()
                    } else {
                        wakelock?.acquire(duration.toMillis())
                    }
                }
            }
        } catch (e: Exception) {
            // DO NOTHING
        }
    }

    fun releaseWakelock() {
        try {
            if (wakelock?.isHeld == true) {
                wakelock?.release()
            }
        } catch (e: Exception) {
            // DO NOTHING
        }
    }

}