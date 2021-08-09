package com.kylecorry.andromeda.camera

import android.annotation.SuppressLint
import androidx.camera.core.ImageProxy
import com.kylecorry.andromeda.core.sensors.ISensor
import com.kylecorry.andromeda.core.units.PixelCoordinate

interface ICamera: ISensor {
    val image: ImageProxy?
    fun setZoom(zoom: Float)

    @SuppressLint("UnsafeExperimentalUsageError")
    fun setExposure(index: Int)
    fun setTorch(isOn: Boolean)
    fun getFOV(): Pair<Float, Float>?
    fun stopFocusAndMetering()
    fun startFocusAndMetering(point: PixelCoordinate)
}