package com.kylecorry.andromeda.torch

import android.view.Window
import com.kylecorry.andromeda.core.system.Screen


class ScreenTorch(private val window: Window) : ITorch {

    override val brightnessLevels: Int = 255

    override fun on() {
        Screen.setBrightness(window, 1f)
    }

    override fun on(brightness: Float) {
        Screen.setBrightness(window, brightness.coerceIn(0f, 1f))
    }

    override fun off() {
        Screen.resetBrightness(window)
    }

    override fun isAvailable(): Boolean {
        return true
    }

    override fun isDimmable(): Boolean {
        return true
    }
}