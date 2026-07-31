package com.kylecorry.andromeda.notify

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.PendingIntent
import android.content.Context
import android.graphics.Color
import android.media.AudioAttributes
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationChannelGroupCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.graphics.drawable.toBitmap
import com.kylecorry.andromeda.core.system.Resources

object Notify {

    fun getSoundUri(context: Context, channelId: String): Uri? {
        return getNotificationManagerCompat(context).getNotificationChannelCompat(channelId)?.sound
    }

    fun isActive(context: Context, notificationId: Int): Boolean {
        return getNotificationManagerCompat(context).activeNotifications.any { it.id == notificationId }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun send(
        context: Context,
        notificationId: Int,
        notification: Notification,
        overrideSystemGrouping: Boolean = false,
        groupSummaryNotificationId: Int = notificationId
    ) {
        val manager = getNotificationManagerCompat(context)

        if (overrideSystemGrouping && Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
            val summary = NotificationCompat
                .Builder(context, notification)
                .setGroupSummary(true)
                .setSound(null)
                .setVibrate(null)
                .build()
            manager.notify(groupSummaryNotificationId, summary)
        }

        manager.notify(notificationId, notification)
    }

    /**
     * Sends a notification if the notification is already shown, otherwise it is a no-op
     */
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun update(context: Context, notificationId: Int, notification: Notification) {
        if (!isActive(context, notificationId)) {
            return
        }
        send(context, notificationId, notification)
    }

    fun cancel(context: Context, notificationId: Int) {
        getNotificationManagerCompat(context).cancel(notificationId)
    }

    fun createChannel(
        context: Context,
        id: String,
        name: String,
        description: String,
        importance: Int,
        muteSound: Boolean = false,
        showBadge: Boolean = true,
        isAlarm: Boolean = false,
        groupId: String? = null
    ) {
        val channel = NotificationChannelCompat.Builder(id, importance).apply {
            setName(name)
            setDescription(description)
            setGroup(groupId)
            if (muteSound) {
                setSound(null, null)
                setVibrationEnabled(false)
            } else if (isAlarm) {
                setSound(
                    Settings.System.DEFAULT_NOTIFICATION_URI,
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setLegacyStreamType(AudioManager.STREAM_ALARM)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build()
                )
                setVibrationEnabled(true)
            }
            setShowBadge(showBadge)
        }.build()
        getNotificationManagerCompat(context).createNotificationChannel(channel)
    }

    fun deleteChannel(context: Context, id: String) {
        getNotificationManagerCompat(context).deleteNotificationChannel(id)
    }

    fun channels(context: Context): List<NotificationChannel> {
        return getNotificationManagerCompat(context).notificationChannels
    }

    fun createChannelGroup(
        context: Context,
        id: String,
        name: CharSequence?,
        description: String? = null
    ) {
        val group = NotificationChannelGroupCompat.Builder(id).apply {
            setName(name)
            setDescription(description)
        }.build()
        getNotificationManagerCompat(context).createNotificationChannelGroup(group)
    }

    fun deleteChannelGroup(context: Context, id: String) {
        getNotificationManagerCompat(context).deleteNotificationChannelGroup(id)
    }

    fun channelGroups(context: Context): List<NotificationChannelGroupCompat> {
        return getNotificationManagerCompat(context).notificationChannelGroupsCompat
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

        val manager = getNotificationManagerCompat(context)
        val channel = manager.getNotificationChannelCompat(channelId) ?: return false
        val group = channel.group?.let { manager.getNotificationChannelGroupCompat(it) }
        val groupBlocked = group?.isBlocked == true
        return groupBlocked || channel.importance == NotificationManagerCompat.IMPORTANCE_NONE
    }

    /**
     * Determines if notifications are blocked for the app
     */
    fun areNotificationsBlocked(context: Context): Boolean {
        return !getNotificationManagerCompat(context).areNotificationsEnabled()
    }

    /**
     * Used for alerts which require the user's attention
     * @param category the category of the notification. If isAlarm is true, this will be ignored.
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
        isAlarm: Boolean = false,
        category: String? = null
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
        } else {
            builder.setCategory(category)
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
        actions: List<NotificationCompat.Action> = listOf(),
        category: String? = NotificationCompat.CATEGORY_STATUS
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

        builder.setCategory(category)

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
        showForegroundImmediate: Boolean = false,
        category: String? = NotificationCompat.CATEGORY_SERVICE
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

        builder.setCategory(category)

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
        showForegroundImmediate: Boolean = false,
        category: String? = NotificationCompat.CATEGORY_SERVICE
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

        builder.setCategory(category)

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

    private fun getNotificationManagerCompat(context: Context): NotificationManagerCompat {
        return NotificationManagerCompat.from(context)
    }

    val CHANNEL_IMPORTANCE_MAX = NotificationManagerCompat.IMPORTANCE_MAX
    val CHANNEL_IMPORTANCE_HIGH = NotificationManagerCompat.IMPORTANCE_HIGH
    val CHANNEL_IMPORTANCE_DEFAULT = NotificationManagerCompat.IMPORTANCE_DEFAULT
    val CHANNEL_IMPORTANCE_LOW = NotificationManagerCompat.IMPORTANCE_LOW
    val CHANNEL_IMPORTANCE_MIN = NotificationManagerCompat.IMPORTANCE_MIN
    val CHANNEL_IMPORTANCE_NONE = NotificationManagerCompat.IMPORTANCE_NONE
}
