package com.kylecorry.andromeda.camera

import android.annotation.SuppressLint
import android.graphics.RectF
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraMetadata
import android.util.Size
import androidx.camera.core.ImageProxy
import com.kylecorry.andromeda.core.annotations.ExperimentalUsage
import com.kylecorry.andromeda.core.sensors.ISensor
import com.kylecorry.andromeda.core.units.PixelCoordinate
import com.kylecorry.sol.math.Range
import java.io.File
import java.io.OutputStream
import java.time.Duration

interface ICamera : ISensor {
    val image: ImageProxy?
    val zoom: ZoomInfo?
    val sensorRotation: Float
    fun setLinearZoom(zoom: Float)
    fun setZoomRatio(zoom: Float)

    fun setExposure(index: Int)
    fun setTorch(isOn: Boolean)

    /**
     * Get the field of view of the camera in degrees (horizontal, vertical) if available.
     * The FOV direction is based on when the phone is in portrait mode.
     *
     * Note: The chosen camera will be the first that matches the orientation of this camera. This should correspond to the zoom ratio of 1.
     */
    fun getFOV(): Pair<Float, Float>?

    /**
     * Get the field of view of the camera in degrees (horizontal, vertical) adjusted by the zoom ratio if available.
     * The FOV direction is based on when the phone is in portrait mode.
     *
     * Note: The chosen camera will be the first that matches the orientation of this camera.
     */
    fun getZoomedFOV(): Pair<Float, Float>?

    fun stopFocusAndMetering()
    fun startFocusAndMetering(point: PixelCoordinate)

    fun takePhoto(callback: (image: ImageProxy?) -> Unit)
    suspend fun takePhoto(): ImageProxy?
    suspend fun takePhoto(file: File): Boolean
    suspend fun takePhoto(stream: OutputStream): Boolean

    /**
     * Set the stabilization mode of the camera. This may not be supported on all devices.
     * @param opticalStabilization true to enable optical stabilization, false to disable
     * @param videoStabilization true to enable video stabilization, false to disable
     */
    @ExperimentalUsage("This uses unstable APIs and may not work on all devices")
    fun setStabilization(opticalStabilization: Boolean, videoStabilization: Boolean)

    /**
     * Set the focus distance of the camera as a percentage of the focus range [0, 1]. This may not be supported on all devices.
     * @param distance the distance to focus to as a percentage of the focus range [0, 1]
     */
    @ExperimentalUsage("This uses unstable APIs and may not work on all devices")
    fun setFocusDistancePercentage(distance: Float?)

    /**
     * Set the exposure time of the camera. This may not be supported on all devices.
     * @param exposureTime the exposure time to set
     */
    @ExperimentalUsage("This uses unstable APIs and may not work on all devices")
    fun setExposureTime(exposureTime: Duration?)

    /**
     * Set the sensitivity of the camera. This may not be supported on all devices.
     * @param isoSensitivity the sensitivity to set (ISO)
     */
    @ExperimentalUsage("This uses unstable APIs and may not work on all devices")
    fun setSensitivity(isoSensitivity: Int?)

    /**
     * Get the supported exposure time range of the camera. This may not be supported on all devices.
     * @return the exposure time range or null if not supported
     */
    @ExperimentalUsage("This uses unstable APIs and may not work on all devices")
    fun getExposureTimeRange(): Range<Duration>?

    /**
     * Get the supported sensitivity range of the camera. This may not be supported on all devices.
     * @return the sensitivity range (ISO) or null if not supported
     */
    @ExperimentalUsage("This uses unstable APIs and may not work on all devices")
    fun getSensitivityRange(): Range<Int>?

    /**
     * Get the supported exposure compensation range of the camera. This may not be supported on all devices.
     * @return the exposure compensation range or null if not supported
     */
    @ExperimentalUsage("This uses unstable APIs and may not work on all devices")
    fun getExposureCompensationRange(): Range<Int>?

    /**
     * Determines if optical stabilization is supported.
     * @return true if optical stabilization is supported
     */
    @ExperimentalUsage("This uses unstable APIs and may not work on all devices")
    fun isOpticalStabilizationSupported(): Boolean

    /**
     * Determines if video stabilization is supported.
     * @return true if video stabilization is supported
     */
    @ExperimentalUsage("This uses unstable APIs and may not work on all devices")
    fun isVideoStabilizationSupported(): Boolean

    /**
     * Get the size of the camera preview.
     * @param cropToView true to crop the preview to the view, false to include the entire preview
     * @return the size of the preview or null if not available
     */
    fun getPreviewSize(cropToView: Boolean = true): Size?

    /**
     * Get the rect of the camera preview.
     * @param cropToView true to crop the preview to the view, false to include the entire preview
     * @return the rect of the preview or null if not available
     */
    fun getPreviewRect(cropToView: Boolean = true): RectF?

    /**
     * Get the field of view of the camera preview in degrees (horizontal, vertical)
     * @param cropToView true to crop the preview to the view, false to include the entire preview
     * @return the FOV of the preview or null if not available
     */
    fun getPreviewFOV(cropToView: Boolean): Pair<Float, Float>?
}