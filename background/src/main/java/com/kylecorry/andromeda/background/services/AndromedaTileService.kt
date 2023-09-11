package com.kylecorry.andromeda.background.services

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
}