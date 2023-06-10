package com.kylecorry.andromeda.torch

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.getSystemService
import com.kylecorry.andromeda.core.system.Android
import com.kylecorry.andromeda.core.tryOrDefault
import com.kylecorry.andromeda.core.tryOrLog
import com.kylecorry.andromeda.core.tryOrNothing
import kotlin.math.roundToInt

class Torch(private val context: Context) : ITorch {
    private val cameraService by lazy { getCameraManager(context) }
    private val cameraId by lazy { getRearCameraId(context) }

    override val brightnessLevels: Int
        @SuppressLint("NewApi")
        get() = if (isDimmable()) getMaxBrightnessLevel() else 1

    override fun on() {
        if (!isAvailable()) {
            return
        }
        tryOrLog {
            cameraId?.let {
                cameraService?.setTorchMode(it, true)
            }
        }
    }

    @SuppressLint("NewApi")
    override fun on(brightness: Float) {
        if (!isAvailable()) {
            return
        }
        tryOrLog {
            if (brightness <= 0f) {
                off()
            } else if (!isDimmable()) {
                on()
            } else {
                val maxLevel = getMaxBrightnessLevel()
                val level = (brightness * maxLevel).roundToInt().coerceIn(1, maxLevel)
                cameraId?.let {
                    cameraService?.turnOnTorchWithStrengthLevel(it, level)
                }
            }
        }
    }

    override fun off() {
        tryOrLog {
            cameraId?.let {
                cameraService?.setTorchMode(it, false)
            }
        }
    }

    override fun isAvailable(): Boolean {
        return isAvailable(context)
    }

    @SuppressLint("NewApi")
    override fun isDimmable(): Boolean {
        if (!isAvailable()) {
            return false
        }

        if (Android.sdk < Build.VERSION_CODES.TIRAMISU) {
            return false
        }

        return getMaxBrightnessLevel() > 1
    }

    @RequiresApi(33)
    private fun getMaxBrightnessLevel(): Int {
        return tryOrDefault(1) {
            cameraId?.let {
                val characteristics = cameraService?.getCameraCharacteristics(it)
                characteristics?.get(CameraCharacteristics.FLASH_INFO_STRENGTH_MAXIMUM_LEVEL)
            } ?: 1
        }
    }

    companion object {

        private fun getCameraManager(context: Context): CameraManager? {
            return tryOrDefault(null) {
                context.getSystemService()
            }
        }

        /**
         * Checks if the device has a flashlight
         * @param context the context
         * @param onlyCheckSystemFeature true to only check if the device has the system feature, false to check if the device has a rear camera with flash. Checking the system feature only is faster.
         */
        fun isAvailable(context: Context, onlyCheckSystemFeature: Boolean = false): Boolean {
            tryOrNothing {
                if (!context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
                    return false
                }
            }

            if (onlyCheckSystemFeature) {
                return true
            }

            return tryOrDefault(false) {
                getRearCameraId(context) != null
            }
        }

        private fun getRearCameraId(context: Context): String? {
            return tryOrDefault(null) {
                val cs = getCameraManager(context)
                cs?.cameraIdList?.firstOrNull {
                    val hasFlash = cs.getCameraCharacteristics(it)
                        .get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                    val facingBack = cs.getCameraCharacteristics(it)
                        .get(CameraCharacteristics.LENS_FACING) == CameraMetadata.LENS_FACING_BACK
                    hasFlash && facingBack
                }
            }
        }
    }
}