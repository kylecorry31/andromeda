package com.kylecorry.andromeda.jobs

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.content.getSystemService
import com.kylecorry.andromeda.core.system.Intents
import com.kylecorry.andromeda.permissions.Permissions
import com.kylecorry.sol.time.Time.toEpochMillis
import java.time.LocalDateTime

object Alarms {

    /**
     * Create an alarm
     * @param time The time to fire the alarm
     * @param pendingIntent The pending intent to launch when the alarm fires
     * @param exact True if the alarm needs to fire at exactly the time specified, false otherwise
     */
    @SuppressLint("MissingPermission")
    fun set(
        context: Context,
        time: LocalDateTime,
        pendingIntent: PendingIntent,
        exact: Boolean = true,
        allowWhileIdle: Boolean = false
    ) {
        val alarmManager = getAlarmManager(context)

        if (!allowWhileIdle || Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            if (exact && Permissions.canScheduleExactAlarms(context)) {
                alarmManager?.setExact(AlarmManager.RTC_WAKEUP, time.toEpochMillis(), pendingIntent)
            } else {
                alarmManager?.set(AlarmManager.RTC_WAKEUP, time.toEpochMillis(), pendingIntent)
            }
        } else {
            if (exact && Permissions.canScheduleExactAlarms(context)) {
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
     * Create an alarm
     * @param time The time to fire the alarm
     * @param pendingIntent The pending intent to launch when the alarm fires
     * @param viewAlarmPendingIntent The pending intent to launch when the user wants to view or edit the alarm
     */
    @SuppressLint("MissingPermission")
    fun setAlarmClock(
        context: Context,
        time: LocalDateTime,
        pendingIntent: PendingIntent,
        viewAlarmPendingIntent: PendingIntent
    ) {
        val alarmManager = getAlarmManager(context) ?: return
        val info = AlarmManager.AlarmClockInfo(time.toEpochMillis(), viewAlarmPendingIntent)
        alarmManager.setAlarmClock(info, pendingIntent)
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