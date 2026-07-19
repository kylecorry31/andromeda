package com.kylecorry.andromeda.notify

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.graphics.Color
import android.media.AudioAttributes
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import androidx.core.graphics.drawable.IconCompat
import androidx.core.graphics.drawable.toBitmap
import com.kylecorry.andromeda.core.system.Resources
import com.kylecorry.andromeda.core.tryOrLog

object Notify {

    fun getSoundUri(context: Context, channelId: String): Uri? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return null
        }

        val channel = getNotificationManager(context)?.getNotificationChannel(channelId)
        return channel?.sound
    }

    fun isActive(context: Context, notificationId: Int): Boolean {
        return getNotificationManager(context)?.activeNotifications?.any { it.id == notificationId }
            ?: false
    }

    fun send(
        context: Context,
        notificationId: Int,
        notification: Notification,
        overrideSystemGrouping: Boolean = false,
        groupSummaryNotificationId: Int = notificationId
    ) {
        val manager = getNotificationManager(context)
        if (overrideSystemGrouping && Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
            val summary = NotificationCompat
                .Builder(context, notification)
                .setGroupSummary(true)
                .setSound(null)
                .setVibrate(null)
                .build()
            manager?.notify(groupSummaryNotificationId, summary)
        }

        manager?.notify(notificationId, notification)
    }

    /**
     * Sends a notification if the notification is already shown, otherwise it is a no-op
     */
    fun update(context: Context, notificationId: Int, notification: Notification) {
        if (!isActive(context, notificationId)) {
            return
        }
        send(context, notificationId, notification)
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
        showBadge: Boolean = true,
        isAlarm: Boolean = false
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }
        val channel = NotificationChannel(id, name, importance).apply {
            this.description = description
            if (muteSound) {
                setSound(null, null)
                enableVibration(false)
            } else if (isAlarm) {
                setSound(
                    Settings.System.DEFAULT_NOTIFICATION_URI,
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setLegacyStreamType(AudioManager.STREAM_ALARM)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build()
                )
                enableVibration(true)
            }
            setShowBadge(showBadge)
        }
        getNotificationManager(context)?.createNotificationChannel(channel)
    }

    fun deleteChannel(context: Context, id: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }
        tryOrLog {
            getNotificationManager(context)?.deleteNotificationChannel(id)
        }
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
        actions: List<NotificationCompat.Action> = listOf(),
        mute: Boolean = false,
        isAlarm: Boolean = false
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

        if (mute) {
            builder.setSilent(true)
        }

        if (isAlarm) {
            builder.setCategory(Notification.CATEGORY_ALARM)
        }

        val notification = builder.build()
        notification.smallIcon.setTint(Color.WHITE)

        if (isAlarm && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            // On Android O+, the sound comes from the notification channel
            @Suppress("DEPRECATION")
            notification.sound = Settings.System.DEFAULT_NOTIFICATION_URI
            @Suppress("DEPRECATION")
            notification.audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setLegacyStreamType(AudioManager.STREAM_ALARM)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()
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
        notification.smallIcon.setTint(Color.WHITE)
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
        notification.smallIcon.setTint(Color.WHITE)
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
        intent: PendingIntent? = null,
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

        if (intent != null) {
            builder.setContentIntent(intent)
        }

        for (action in actions) {
            builder.addAction(action)
        }

        val notification = builder.build()
        notification.smallIcon.setTint(Color.WHITE)
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
