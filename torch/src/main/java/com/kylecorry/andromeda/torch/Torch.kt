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
import kotlin.math.roundToInt

class Torch(private val context: Context) : ITorch {
    private val cameraService by lazy { getCameraManager(context) }
    private val cameraId by lazy { getRearCameraId(context) }

    override fun on() {
        if (!isAvailable()) {
            return
        }
        tryOrLog {
            cameraService?.setTorchMode(cameraId, true)
        }
    }

    @SuppressLint("NewApi")
    override fun on(brightness: Float) {
        if (!isAvailable()) {
            return
        }
        tryOrLog {
            if (brightness <= 0f){
                off()
            } else if (!isDimmable()){
                on()
            } else {
                val maxLevel = getMaxBrightnessLevel()
                val level = (brightness * maxLevel).roundToInt().coerceIn(1, maxLevel)
                cameraService?.turnOnTorchWithStrengthLevel(cameraId, level)
            }
        }
    }

    override fun off() {
        tryOrLog {
            cameraService?.setTorchMode(cameraId, false)
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
            val characteristics = cameraService?.getCameraCharacteristics(cameraId)
            characteristics?.get(CameraCharacteristics.FLASH_INFO_STRENGTH_MAXIMUM_LEVEL) ?: 1
        }
    }

    companion object {

        private fun getCameraManager(context: Context): CameraManager? {
            return try {
                context.getSystemService()
            } catch (e: Exception) {
                null
            }
        }

        fun isAvailable(context: Context): Boolean {
            try {
                if (!context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
                    return false
                }
            } catch (e: Exception) {
                // Could not check the package manager - do nothing
            }

            try {
                val cs = getCameraManager(context)
                val rearCamera = getRearCameraId(context)
                if (rearCamera.isEmpty() || cs == null) {
                    return false
                }

                val hasFlash = cs.getCameraCharacteristics(rearCamera)
                    .get(CameraCharacteristics.FLASH_INFO_AVAILABLE)
                val facing = cs.getCameraCharacteristics(rearCamera)
                    .get(CameraCharacteristics.LENS_FACING)

                return hasFlash == true && facing == CameraMetadata.LENS_FACING_BACK
            } catch (e: Exception) {
                return false
            }
        }

        private fun getRearCameraId(context: Context): String {
            val cs = getCameraManager(context)
            val cameraList = cs?.cameraIdList
            if (cameraList == null || cameraList.isEmpty()) return ""
            for (camera in cameraList) {
                val hasFlash = cs.getCameraCharacteristics(camera)
                    .get(CameraCharacteristics.FLASH_INFO_AVAILABLE)
                val facing = cs.getCameraCharacteristics(camera)
                    .get(CameraCharacteristics.LENS_FACING)
                if (hasFlash == true && facing == CameraMetadata.LENS_FACING_BACK) {
                    return camera
                }

            }
            return cameraList[0]
        }
    }
}