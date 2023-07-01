package com.kylecorry.andromeda.background.services

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import com.kylecorry.andromeda.core.system.Wakelocks
import java.time.Duration

/**
 * To run as a foreground service, override [getForegroundInfo] and return a [ForegroundInfo] object.
 */
abstract class AndromedaService: Service() {
    open val tag: String
        get() = javaClass.name

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val foregroundInfo = getForegroundInfo()
        if (foregroundInfo != null) {
            startForeground(foregroundInfo.id, foregroundInfo.notification)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    fun stopService(removeNotification: Boolean = true) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(if (removeNotification) STOP_FOREGROUND_REMOVE else STOP_FOREGROUND_DETACH)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(removeNotification)
        }
        stopSelf()
    }

    override fun onDestroy() {
        releaseWakelock()
        super.onDestroy()
    }

    private var wakelock: PowerManager.WakeLock? = null

    @SuppressLint("WakelockTimeout")
    fun acquireWakelock(tag: String = this.tag, duration: Duration? = null){
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

    open fun getForegroundInfo(): ForegroundInfo? {
        return null
    }
}