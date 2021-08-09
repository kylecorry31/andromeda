package com.kylecorry.andromeda.torch

import com.kylecorry.andromeda.core.system.IScreenService

class ScreenTorch(private val screenService: IScreenService) : ITorch {
    override fun on() {
        screenService.setBrightness(1f)
    }

    override fun off() {
        screenService.resetBrightness()
    }

    override fun isAvailable(): Boolean {
        return true
    }
}