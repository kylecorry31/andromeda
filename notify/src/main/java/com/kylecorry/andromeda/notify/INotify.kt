package com.kylecorry.andromeda.notify

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat

interface INotify {

    // Low level
    fun isActive(notificationId: Int): Boolean

    fun send(notificationId: Int, notification: Notification)

    fun cancel(notificationId: Int)

    // Channel
    fun createChannel(
        id: String,
        name: String,
        description: String,
        importance: Int,
        muteSound: Boolean = false
    )

    // Notification types
    /**
     * Used for alerts which require the user's attention
     */
    fun alert(
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
    ): Notification

    /**
     * Used to convey a status message
     *
     * Basically alerts that don't require the user's immediate attention
     */
    fun status(
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
    ): Notification

    /**
     * Used for notifications connected to a process which give the user useful information
     */
    fun persistent(
        channel: String,
        title: String,
        contents: String?,
        @DrawableRes icon: Int,
        autoCancel: Boolean = false,
        alertOnlyOnce: Boolean = true,
        showBigIcon: Boolean = false,
        group: String? = null,
        intent: PendingIntent? = null,
        actions: List<NotificationCompat.Action> = listOf()
    ): Notification

    /**
     * Used for notifications which are connected to a process (aka required) but the user doesn't care about them
     */
    fun background(
        channel: String,
        title: String,
        contents: String?,
        @DrawableRes icon: Int
    ): Notification

    fun action(
        name: String,
        intent: PendingIntent,
        @DrawableRes icon: Int? = null
    ): NotificationCompat.Action

}