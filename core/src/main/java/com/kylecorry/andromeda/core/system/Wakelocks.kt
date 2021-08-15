package com.kylecorry.andromeda.core.system

import android.content.Context
import android.os.PowerManager
import androidx.core.content.getSystemService

object Wakelocks {

    fun get(context: Context, tag: String): PowerManager.WakeLock? {
        val powerManager = context.getSystemService<PowerManager>()
        return powerManager?.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, tag)
    }

}