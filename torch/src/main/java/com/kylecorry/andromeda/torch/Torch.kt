package com.kylecorry.andromeda.torch

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import androidx.core.content.getSystemService

class Torch(private val context: Context) : ITorch {
    private val cameraService by lazy { getCameraManager(context) }
    private val cameraId by lazy { getRearCameraId(context) }

    override fun on() {
        if (!isAvailable()) {
            return
        }
        try {
            cameraService?.setTorchMode(cameraId, true)
        } catch (e: Exception) {
            // No flash, ignoring
        }
    }

    override fun off() {
        try {
            cameraService?.setTorchMode(cameraId, false)
        } catch (e: Exception) {
            // No flash, ignoring
        }
    }

    override fun isAvailable(): Boolean {
        return isAvailable(context)
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