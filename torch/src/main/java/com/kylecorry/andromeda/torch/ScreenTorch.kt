package com.kylecorry.andromeda.torch

import android.view.Window
import com.kylecorry.andromeda.core.system.Screen


class ScreenTorch(private val window: Window) : ITorch {
    override fun on() {
        Screen.setBrightness(window, 1f)
    }

    fun on(brightness: Float){
        Screen.setBrightness(window, brightness.coerceIn(0f, 1f))
    }

    override fun off() {
        Screen.resetBrightness(window)
    }

    override fun isAvailable(): Boolean {
        return true
    }
}