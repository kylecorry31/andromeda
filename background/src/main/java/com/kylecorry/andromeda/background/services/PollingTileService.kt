package com.kylecorry.andromeda.background.services

import android.os.Build
import android.service.quicksettings.Tile
import androidx.annotation.RequiresApi
import com.kylecorry.andromeda.core.time.Timer
import java.time.Duration

@RequiresApi(Build.VERSION_CODES.N)
abstract class PollingTileService(private val interval: Duration = Duration.ofMillis(16)) :
    AndromedaTileService() {

    private val stateChecker = Timer {
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

    abstract fun isOn(): Boolean

    open fun isDisabled(): Boolean {
        return false
    }

    open fun isOff(): Boolean {
        return !isOn() && !isDisabled()
    }

    open fun onInterval() {

    }

    override fun onStartListening() {
        super.onStartListening()
        stateChecker.interval(interval)
    }

    override fun onStopListening() {
        super.onStopListening()
        stateChecker.stop()
    }

}