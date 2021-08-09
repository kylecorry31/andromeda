package com.kylecorry.andromeda.services

import android.graphics.Color
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import com.kylecorry.andromeda.core.time.Intervalometer

@RequiresApi(Build.VERSION_CODES.N)
abstract class CustomTileService : TileService() {

    private val stateChecker = Intervalometer {
        val lastState = qsTile.state
        val newState = when {
            isOn() -> Tile.STATE_ACTIVE
            isDisabled() -> Tile.STATE_UNAVAILABLE
            isOff() -> Tile.STATE_INACTIVE
            else -> lastState
        }

        if (lastState != newState) {
            qsTile.state = newState
            qsTile.updateTile()
        }

        onInterval()
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

    abstract fun isOn(): Boolean

    open fun isDisabled(): Boolean {
        return false
    }

    open fun isOff(): Boolean {
        return !isOn() && !isDisabled()
    }

    abstract fun start()

    abstract fun stop()

    open fun onInterval(){

    }

    override fun onClick() {
        super.onClick()
        when {
            isOn() -> {
                stop()
                qsTile.state = Tile.STATE_INACTIVE
            }
            isOff() -> {
                start()
                qsTile.state = Tile.STATE_ACTIVE
            }
        }
        qsTile.updateTile()
    }

    override fun onStartListening() {
        super.onStartListening()
        stateChecker.interval(16)
    }

    override fun onStopListening() {
        super.onStopListening()
        stateChecker.stop()
    }

}