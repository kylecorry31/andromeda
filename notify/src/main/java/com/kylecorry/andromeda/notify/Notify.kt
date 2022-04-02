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
import androidx.core.graphics.drawable.IconCompat
import androidx.core.graphics.drawable.toBitmap
import com.kylecorry.andromeda.core.system.Resources

object Notify {

    fun isActive(context: Context, notificationId: Int): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getNotificationManager(context)?.activeNotifications?.any { it.id == notificationId }
                ?: false
        } else {
            // TODO: Determine if the notification exists
            false
        }
    }

    fun send(context: Context, notificationId: Int, notification: Notification) {
        getNotificationManager(context)?.notify(notificationId, notification)
    }

    fun cancel(context: Context, notificationId: Int) {
        getNotificationManager(context)?.cancel(notificationId)
    }

    fun createChannel(
        context: Context,
        id: String,
        name: String,
        description: String,
        importance: Int,
        muteSound: Boolean = false,
        showBadge: Boolean = true
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
            setShowBadge(showBadge)
        }
        getNotificationManager(context)?.createNotificationChannel(channel)
    }

    fun channels(context: Context): List<NotificationChannel> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return try {
                getNotificationManager(context)?.notificationChannels ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }

        return emptyList()
    }

    /**
     * Determines if a channel is blocked
     */
    fun isChannelBlocked(context: Context, channelId: String): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return false
        }

        if (areNotificationsBlocked(context)) {
            return true
        }

        try {
            val channel =
                getNotificationManager(context)?.getNotificationChannel(channelId) ?: return false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val groupId = channel.group
                val groupBlocked =
                    getNotificationManager(context)?.getNotificationChannelGroup(groupId)?.isBlocked == true
                if (groupBlocked) {
                    return true
                }
            }

            return channel.importance == NotificationManager.IMPORTANCE_NONE
        } catch (e: Exception) {
            return false
        }
    }

    /**
     * Determines if notifications are blocked for the app
     */
    fun areNotificationsBlocked(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            getNotificationManager(context)?.areNotificationsEnabled() == false
        } else {
            false
        }
    }

    /**
     * Used for alerts which require the user's attention
     */
    fun alert(
        context: Context,
        channel: String,
        title: String,
        contents: String?,
        @DrawableRes icon: Int,
        autoCancel: Boolean = false,
        alertOnlyOnce: Boolean = false,
        showBigIcon: Boolean = false,
        group: String? = null,
        intent: PendingIntent? = null,
        actions: List<NotificationCompat.Action> = listOf()
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
            val drawable = Resources.drawable(context, icon)
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

    /**
     * Used to convey a status message
     *
     * Basically alerts that don't require the user's immediate attention
     */
    fun status(
        context: Context,
        channel: String,
        title: String,
        contents: String?,
        @DrawableRes icon: Int,
        autoCancel: Boolean = false,
        alertOnlyOnce: Boolean = false,
        showBigIcon: Boolean = false,
        group: String? = null,
        intent: PendingIntent? = null,
        actions: List<NotificationCompat.Action> = listOf()
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
            val drawable = Resources.drawable(context, icon)
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

    /**
     * Used for notifications connected to a process which give the user useful information
     */
    fun persistent(
        context: Context,
        channel: String,
        title: String,
        contents: String?,
        @DrawableRes icon: Int,
        autoCancel: Boolean = false,
        alertOnlyOnce: Boolean = true,
        showBigIcon: Boolean = false,
        group: String? = null,
        intent: PendingIntent? = null,
        actions: List<NotificationCompat.Action> = listOf(),
        showForegroundImmediate: Boolean = false
    ): Notification {

        val builder = NotificationCompat.Builder(context, channel)
            .setContentTitle(title)
            .setSmallIcon(icon)
            .setAutoCancel(autoCancel)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setSilent(true)
            .setOnlyAlertOnce(alertOnlyOnce)

        if (showForegroundImmediate) {
            builder.foregroundServiceBehavior = NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE
        }

        if (contents != null) {
            builder.setContentText(contents)
        }

        if (showBigIcon) {
            val drawable = Resources.drawable(context, icon)
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

    /**
     * Used for notifications which are connected to a process (aka required) but the user doesn't care about them
     */
    fun background(
        context: Context,
        channel: String,
        title: String,
        contents: String?,
        @DrawableRes icon: Int,
        group: String? = null,
        actions: List<NotificationCompat.Action> = listOf(),
        showForegroundImmediate: Boolean = false
    ): Notification {
        val builder = NotificationCompat.Builder(context, channel)
            .setContentTitle(title)
            .setSmallIcon(icon)
            .setAutoCancel(false)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setSilent(true)
            .setOnlyAlertOnce(true)

        if (showForegroundImmediate) {
            builder.foregroundServiceBehavior = NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE
        }

        if (group != null) {
            builder.setGroup(group)
        }

        if (contents != null) {
            builder.setContentText(contents)
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

    fun action(
        name: String,
        intent: PendingIntent,
        @DrawableRes icon: Int? = null
    ): NotificationCompat.Action {
        return NotificationCompat.Action(icon ?: 0, name, intent)
    }

    fun action(
        name: String,
        intent: PendingIntent,
        icon: IconCompat? = null
    ): NotificationCompat.Action {
        return NotificationCompat.Action(icon, name, intent)
    }

    private fun getNotificationManager(context: Context): NotificationManager? {
        return context.getSystemService()
    }
    
    val CHANNEL_IMPORTANCE_HIGH =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) NotificationManager.IMPORTANCE_HIGH else 4
    val CHANNEL_IMPORTANCE_DEFAULT =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) NotificationManager.IMPORTANCE_DEFAULT else 3
    val CHANNEL_IMPORTANCE_LOW =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) NotificationManager.IMPORTANCE_LOW else 2
}