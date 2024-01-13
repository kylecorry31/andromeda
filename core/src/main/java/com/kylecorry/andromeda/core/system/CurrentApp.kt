package com.kylecorry.andromeda.core.system

import android.app.ActivityManager
import android.content.Context
import android.os.Build

object CurrentApp {

    fun restart(context: Context) {
        val intent = Intents.restart(context)
        context.startActivity(intent)
        kill()
    }

    fun kill() {
        Runtime.getRuntime().exit(0)
    }

    fun isInForeground(includeForegroundServices: Boolean = true): Boolean {
        val processInfo = ActivityManager.RunningAppProcessInfo()
        ActivityManager.getMyMemoryState(processInfo)
        return processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND ||
                (includeForegroundServices && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND_SERVICE)
    }

}