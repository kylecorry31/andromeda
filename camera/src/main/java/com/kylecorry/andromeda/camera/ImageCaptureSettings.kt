package com.kylecorry.andromeda.camera

import androidx.camera.core.ImageCapture

data class ImageCaptureSettings(
    val quality: Int? = null,
    val flashMode: Int = ImageCapture.FLASH_MODE_OFF,
    val captureMode: Int = ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY,
    val rotation: Int? = null
)