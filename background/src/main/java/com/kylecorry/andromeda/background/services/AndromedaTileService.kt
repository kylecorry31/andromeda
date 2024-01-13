package com.kylecorry.andromeda.background.services

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Icon
import android.os.Build
import android.os.IBinder
import android.service.quicksettings.TileService
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import com.kylecorry.andromeda.core.coroutines.onMain
import com.kylecorry.andromeda.core.system.CurrentApp
import com.kylecorry.andromeda.permissions.Permissions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.N)
abstract class AndromedaTileService : TileService() {

    // Potential fix for a bug in Android where it fails to reach the IQSService
    // https://github.com/firemaples/EverTranslator/issues/130
    override fun onBind(intent: Intent?): IBinder? {
        return try {
            super.onBind(intent)
        } catch (e: Exception) {
            null
        }
    }

    fun setState(state: Int) {
        if (state != qsTile.state) {
            qsTile.state = state
            qsTile.updateTile()
        }
    }

    fun setTitle(title: String) {
        qsTile.label = title
        qsTile.updateTile()
    }

    fun setSubtitle(subtitle: String?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            qsTile.subtitle = subtitle
            qsTile.updateTile()
        }
    }

    fun setIcon(icon: Icon) {
        qsTile.icon = icon
        qsTile.updateTile()
    }

    fun setIcon(@DrawableRes icon: Int) {
        val drawable = ResourcesCompat.getDrawable(this.resources, icon, null)
        val bitmap = drawable?.toBitmap() ?: return
        val realIcon = Icon.createWithBitmap(bitmap)
        realIcon.setTint(Color.WHITE)
        setIcon(realIcon)
    }

    private fun AndromedaTileService.isForegroundWorkaroundNeeded(): Boolean {
        // The bug only happens on Android 14+
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            return false
        }

        if (CurrentApp.isInForeground(includeForegroundServices = true)) {
            return false
        }

        return !Permissions.isIgnoringBatteryOptimizations(this)
    }

    private inline fun AndromedaTileService.startWorkaround(crossinline action: suspend () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val dialog = Dialog(this)
            dialog.setOnShowListener {
                val scope = CoroutineScope(Dispatchers.Default)
                scope.launch {
                    try {
                        action()
                    } finally {
                        onMain {
                            dialog.dismiss()
                        }
                    }
                }
            }
            showDialog(dialog)
        }
    }

    /**
     * Allows the starting of a foreground service.
     * This is a workaround for a bug in Android 14+, and in some cases the notification panel may be dismissed in order to start the service: https://issuetracker.google.com/issues/299506164
     * @param action the action which will start a foreground service
     */
    fun startForegroundService(action: suspend () -> Unit) {
        if (isForegroundWorkaroundNeeded()) {
            startWorkaround {
                action()
            }
        } else {
            val scope = CoroutineScope(Dispatchers.Default)
            scope.launch { action() }
        }
    }
}