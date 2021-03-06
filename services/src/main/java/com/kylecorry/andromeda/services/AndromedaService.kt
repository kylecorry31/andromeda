package com.kylecorry.andromeda.services

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import com.kylecorry.andromeda.core.system.Wakelocks
import java.time.Duration

abstract class AndromedaService: Service() {

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        releaseWakelock()
        super.onDestroy()
    }

    private var wakelock: PowerManager.WakeLock? = null

    @SuppressLint("WakelockTimeout")
    fun acquireWakelock(tag: String, duration: Duration? = null){
        try {
            if (wakelock?.isHeld != true) {
                wakelock = Wakelocks.get(this, tag)
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