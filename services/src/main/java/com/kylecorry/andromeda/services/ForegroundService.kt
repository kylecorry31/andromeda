package com.kylecorry.andromeda.services

import android.app.Notification
import android.content.Intent
import android.os.Build

abstract class ForegroundService: AndromedaService() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(foregroundNotificationId, getForegroundNotification())
        return onServiceStarted(intent, flags, startId)
    }

    fun stopService(removeNotification: Boolean = true){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(if (removeNotification) STOP_FOREGROUND_REMOVE else STOP_FOREGROUND_DETACH)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(removeNotification)
        }
        stopSelf()
    }

    abstract fun onServiceStarted(intent: Intent?, flags: Int, startId: Int): Int
    abstract fun getForegroundNotification(): Notification
    abstract val foregroundNotificationId: Int
}