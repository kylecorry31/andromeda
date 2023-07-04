package com.kylecorry.andromeda.background

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
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime

object Alarms {

    /**
     * Create an alarm
     * @param time The time to fire the alarm
     * @param pendingIntent The pending intent to launch when the alarm fires
     * @param exact True if the alarm needs to fire at exactly the time specified, false otherwise. If exact alarms are not allowed, an inexact alarm will be used.
     * @param allowWhileIdle True if the alarm can fire while the device is idle, false otherwise
     * @param inexactWindow The window of time in which the inexact alarm can fire. If false, the system will decide. Minimum is 10 minutes on most devices.
     * @param isWindowCentered True if the inexact window should be centered around the time, false if the window should be after the time.
     */
    @SuppressLint("MissingPermission")
    fun set(
        context: Context,
        time: Instant,
        pendingIntent: PendingIntent,
        exact: Boolean = true,
        allowWhileIdle: Boolean = false,
        inexactWindow: Duration? = null,
        isWindowCentered: Boolean = false
    ) {
        if (exact) {
            exactAlarm(context, time, allowWhileIdle, pendingIntent)
        } else if (inexactWindow != null) {
            windowedAlarm(context, time, inexactWindow, isWindowCentered, pendingIntent)
        } else {
            inexactAlarm(context, time, allowWhileIdle, pendingIntent)
        }
    }

    @SuppressLint("MissingPermission")
    private fun exactAlarm(
        context: Context,
        time: Instant,
        allowWhileIdle: Boolean,
        pendingIntent: PendingIntent
    ) {

        if (!Permissions.canScheduleExactAlarms(context)) {
            // Fall back to an inexact alarm
            inexactAlarm(context, time, allowWhileIdle, pendingIntent)
            return
        }

        val alarmManager = getAlarmManager(context)
        if (allowWhileIdle && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager?.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                time.toEpochMilli(),
                pendingIntent
            )
        } else {
            alarmManager?.setExact(AlarmManager.RTC_WAKEUP, time.toEpochMilli(), pendingIntent)
        }
    }

    private fun windowedAlarm(
        context: Context,
        time: Instant,
        window: Duration,
        isWindowCentered: Boolean,
        pendingIntent: PendingIntent,
    ) {
        val alarmManager = getAlarmManager(context)
        val start = if (isWindowCentered) {
            time.minus(window.dividedBy(2))
        } else {
            time
        }
        alarmManager?.setWindow(
            AlarmManager.RTC_WAKEUP,
            start.toEpochMilli(),
            window.toMillis(),
            pendingIntent
        )
    }

    private fun inexactAlarm(
        context: Context,
        time: Instant,
        allowWhileIdle: Boolean,
        pendingIntent: PendingIntent
    ) {
        val alarmManager = getAlarmManager(context)
        if (allowWhileIdle && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager?.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                time.toEpochMilli(),
                pendingIntent
            )
        } else {
            alarmManager?.set(AlarmManager.RTC_WAKEUP, time.toEpochMilli(), pendingIntent)
        }
    }

    /**
     * Create a repeating alarm (inexact)
     * @param time The time to fire the alarm
     * @param interval The interval to fire the alarm
     * @param pendingIntent The pending intent to launch when the alarm fires
     */
    @SuppressLint("MissingPermission")
    fun setRepeating(
        context: Context,
        time: Instant,
        interval: Duration,
        pendingIntent: PendingIntent
    ) {
        val alarmManager = getAlarmManager(context)
        alarmManager?.setRepeating(
            AlarmManager.RTC_WAKEUP,
            time.toEpochMilli(),
            interval.toMillis(),
            pendingIntent
        )
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
        time: Instant,
        pendingIntent: PendingIntent,
        viewAlarmPendingIntent: PendingIntent
    ) {
        val alarmManager = getAlarmManager(context) ?: return
        val info = AlarmManager.AlarmClockInfo(time.toEpochMilli(), viewAlarmPendingIntent)
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