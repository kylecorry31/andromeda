package com.kylecorry.andromeda.camera

import android.annotation.SuppressLint
import androidx.camera.core.ImageProxy
import com.kylecorry.andromeda.core.sensors.ISensor
import com.kylecorry.andromeda.core.units.PixelCoordinate
import java.io.File
import java.io.OutputStream

interface ICamera: ISensor {
    val image: ImageProxy?
    val zoom: ZoomInfo?
    val sensorRotation: Float
    fun setLinearZoom(zoom: Float)
    fun setZoomRatio(zoom: Float)

    @SuppressLint("UnsafeExperimentalUsageError")
    fun setExposure(index: Int)
    fun setTorch(isOn: Boolean)
    fun getFOV(): Pair<Float, Float>?
    fun stopFocusAndMetering()
    fun startFocusAndMetering(point: PixelCoordinate)

    fun takePhoto(callback: (image: ImageProxy?) -> Unit)
    suspend fun takePhoto(): ImageProxy?
    suspend fun takePhoto(file: File): Boolean
    suspend fun takePhoto(stream: OutputStream): Boolean
}