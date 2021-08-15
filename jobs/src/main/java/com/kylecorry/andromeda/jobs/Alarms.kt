package com.kylecorry.andromeda.jobs

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.content.getSystemService
import com.kylecorry.andromeda.core.system.Intents
import com.kylecorry.andromeda.core.time.toEpochMillis
import java.time.LocalDateTime

object Alarms {

    /**
     * Create an alarm
     * @param time The time to fire the alarm
     * @param pendingIntent The pending intent to launch when the alarm fires
     * @param exact True if the alarm needs to fire at exactly the time specified, false otherwise
     */
    fun set(
        context: Context,
        time: LocalDateTime,
        pendingIntent: PendingIntent,
        exact: Boolean = true,
        allowWhileIdle: Boolean = false
    ) {
        val alarmManager = getAlarmManager(context)

        if (!allowWhileIdle || Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            if (exact) {
                alarmManager?.setExact(AlarmManager.RTC_WAKEUP, time.toEpochMillis(), pendingIntent)
            } else {
                alarmManager?.set(AlarmManager.RTC_WAKEUP, time.toEpochMillis(), pendingIntent)
            }
        } else {
            if (exact) {
                alarmManager?.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    time.toEpochMillis(),
                    pendingIntent
                )
            } else {
                alarmManager?.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    time.toEpochMillis(),
                    pendingIntent
                )
            }
        }
    }

    /**
     * Cancel the alarm associated with the pending intent
     * @param pendingIntent The pending intent to cancel
     */
    fun cancel(context: Context, pendingIntent: PendingIntent) {
        try {
            val alarmManager = getAlarmManager(context)
            alarmManager?.cancel(pendingIntent)
            pendingIntent.cancel()
        } catch (e: Exception) {
            Log.e("SystemUtils", "Could not cancel alarm", e)
        }
    }

    /**
     * Determines if an alarm is running
     * @param requestCode The request code of the pending intent
     * @param intent The intent used for the pending intent
     * @return true if the alarm is running, false otherwise
     */
    fun isAlarmRunning(context: Context, requestCode: Int, intent: Intent): Boolean {
        return Intents.pendingIntentExists(context, requestCode, intent)
    }

    private fun getAlarmManager(context: Context): AlarmManager? {
        return context.getSystemService()
    }

}