package com.kylecorry.andromeda.notify

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap

class Notify(private val context: Context) : INotify {

    private val manager by lazy { getNotificationManager() }

    override fun isActive(notificationId: Int): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            manager?.activeNotifications?.any { it.id == notificationId } ?: false
        } else {
            // TODO: Determine if the notification exists
            false
        }
    }

    override fun send(notificationId: Int, notification: Notification) {
        manager?.notify(notificationId, notification)
    }

    override fun cancel(notificationId: Int) {
        manager?.cancel(notificationId)
    }

    override fun createChannel(
        id: String,
        name: String,
        description: String,
        importance: Int,
        muteSound: Boolean
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }
        val channel = NotificationChannel(id, name, importance).apply {
            this.description = description
            if (muteSound) {
                setSound(null, null)
                enableVibration(false)
            }
        }
        manager?.createNotificationChannel(channel)
    }

    override fun alert(
        channel: String,
        title: String,
        contents: String?,
        icon: Int,
        autoCancel: Boolean,
        alertOnlyOnce: Boolean,
        showBigIcon: Boolean,
        group: String?,
        intent: PendingIntent?,
        actions: List<NotificationCompat.Action>
    ): Notification {
        val builder = NotificationCompat.Builder(context, channel)
            .setContentTitle(title)
            .setSmallIcon(icon)
            .setAutoCancel(autoCancel)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOnlyAlertOnce(alertOnlyOnce)

        if (contents != null) {
            builder.setContentText(contents)
        }

        if (showBigIcon) {
            val drawable = drawable(context, icon)
            val bitmap = drawable?.toBitmap()
            builder.setLargeIcon(bitmap)
        }

        if (group != null) {
            builder.setGroup(group)
        }

        if (intent != null) {
            builder.setContentIntent(intent)
        }

        for (action in actions) {
            builder.addAction(action)
        }

        val notification = builder.build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notification.smallIcon.setTint(Color.WHITE)
        }
        return notification
    }

    override fun status(
        channel: String,
        title: String,
        contents: String?,
        icon: Int,
        autoCancel: Boolean,
        alertOnlyOnce: Boolean,
        showBigIcon: Boolean,
        group: String?,
        intent: PendingIntent?,
        actions: List<NotificationCompat.Action>
    ): Notification {
        val builder = NotificationCompat.Builder(context, channel)
            .setContentTitle(title)
            .setSmallIcon(icon)
            .setAutoCancel(autoCancel)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setSilent(true)
            .setOnlyAlertOnce(alertOnlyOnce)

        if (contents != null) {
            builder.setContentText(contents)
        }

        if (showBigIcon) {
            val drawable = drawable(context, icon)
            val bitmap = drawable?.toBitmap()
            builder.setLargeIcon(bitmap)
        }

        if (group != null) {
            builder.setGroup(group)
        }

        if (intent != null) {
            builder.setContentIntent(intent)
        }

        for (action in actions) {
            builder.addAction(action)
        }

        val notification = builder.build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notification.smallIcon.setTint(Color.WHITE)
        }
        return notification
    }

    override fun persistent(
        channel: String,
        title: String,
        contents: String?,
        icon: Int,
        autoCancel: Boolean,
        alertOnlyOnce: Boolean,
        showBigIcon: Boolean,
        group: String?,
        intent: PendingIntent?,
        actions: List<NotificationCompat.Action>
    ): Notification {
        val builder = NotificationCompat.Builder(context, channel)
            .setContentTitle(title)
            .setSmallIcon(icon)
            .setAutoCancel(autoCancel)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setSilent(true)
            .setOnlyAlertOnce(alertOnlyOnce)

        if (contents != null) {
            builder.setContentText(contents)
        }

        if (showBigIcon) {
            val drawable = drawable(context, icon)
            val bitmap = drawable?.toBitmap()
            builder.setLargeIcon(bitmap)
        }

        if (group != null) {
            builder.setGroup(group)
        }

        if (intent != null) {
            builder.setContentIntent(intent)
        }

        for (action in actions) {
            builder.addAction(action)
        }

        val notification = builder.build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notification.smallIcon.setTint(Color.WHITE)
        }
        return notification
    }

    override fun background(
        channel: String,
        title: String,
        contents: String?,
        icon: Int
    ): Notification {
        val builder = NotificationCompat.Builder(context, channel)
            .setContentTitle(title)
            .setSmallIcon(icon)
            .setAutoCancel(false)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setSilent(true)
            .setOnlyAlertOnce(true)

        if (contents != null) {
            builder.setContentText(contents)
        }

        val notification = builder.build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notification.smallIcon.setTint(Color.WHITE)
        }
        return notification
    }

    override fun action(
        name: String,
        intent: PendingIntent,
        icon: Int?
    ): NotificationCompat.Action {
        return NotificationCompat.Action(icon ?: 0, name, intent)
    }

    private fun getNotificationManager(): NotificationManager? {
        return context.getSystemService()
    }

    // TODO: Extract the UI Utils from TS Core
    private fun drawable(context: Context, @DrawableRes drawableId: Int): Drawable? {
        return ResourcesCompat.getDrawable(context.resources, drawableId, null)
    }

    companion object {
        val CHANNEL_IMPORTANCE_HIGH =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) NotificationManager.IMPORTANCE_HIGH else 4
        val CHANNEL_IMPORTANCE_DEFAULT =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) NotificationManager.IMPORTANCE_DEFAULT else 3
        val CHANNEL_IMPORTANCE_LOW =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) NotificationManager.IMPORTANCE_LOW else 2
    }
}