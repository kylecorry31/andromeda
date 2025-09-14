package com.kylecorry.andromeda.widgets

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.PersistableBundle
import androidx.annotation.DrawableRes
import androidx.core.app.PendingIntentCompat
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat

object Shortcuts {

    /**
     * Adds a shortcut to the home screen.
     * @param context The context.
     * @param id The unique ID of the shortcut.
     * @param shortLabel The short label of the shortcut.
     * @param icon The icon resource of the shortcut.
     * @param intent The intent to launch when the shortcut is clicked.
     * @param longLabel The long label of the shortcut (optional).
     * @param extras Additional extras to add to the shortcut (optional).
     * @param requestCode The request code for the callback intent (optional).
     * @param broadcastFlags Flags for the broadcast PendingIntent (optional).
     * @return True if the shortcut request was sent, false otherwise.
     */
    fun addToHomescreen(
        context: Context,
        id: String,
        shortLabel: String,
        @DrawableRes icon: Int,
        intent: Intent,
        longLabel: String? = null,
        extras: PersistableBundle? = null,
        requestCode: Int = 0,
        @PendingIntentCompat.Flags broadcastFlags: Int = 0
    ): Boolean {
        if (!canAddToHomescreen(context)) {
            return false
        }

        val shortcutInfoBuilder = ShortcutInfoCompat.Builder(context, id)
            .setIcon(
                IconCompat.createFromIcon(
                    context,
                    Icon.createWithResource(context, icon)
                )
            )
            .setIntent(intent)
            .setShortLabel(shortLabel)

        if (longLabel != null) {
            shortcutInfoBuilder.setLongLabel(longLabel)
        }

        if (extras != null) {
            shortcutInfoBuilder.setExtras(extras)
        }

        val shortcutInfo = shortcutInfoBuilder.build()

        val callbackIntent =
            ShortcutManagerCompat.createShortcutResultIntent(context, shortcutInfo)
        val successCallback = PendingIntent.getBroadcast(
            context, requestCode, callbackIntent,
            PendingIntent.FLAG_IMMUTABLE or broadcastFlags
        )
        return ShortcutManagerCompat.requestPinShortcut(
            context,
            shortcutInfo,
            successCallback.intentSender
        )
    }

    /**
     * Checks if the device supports adding shortcuts to the home screen.
     * @param context The context.
     * @return True if the device supports adding shortcuts, false otherwise.
     */
    fun canAddToHomescreen(context: Context): Boolean {
        return ShortcutManagerCompat.isRequestPinShortcutSupported(context)
    }

}