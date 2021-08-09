package com.kylecorry.andromeda.core.system

interface IScreenService {
    fun setKeepScreenOn(keepOn: Boolean)
    fun setBrightness(brightness: Float)
    fun resetBrightness()
    fun setAllowScreenshots(allowed: Boolean)
}