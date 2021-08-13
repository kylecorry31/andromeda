package com.kylecorry.andromeda.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import com.kylecorry.andromeda.core.system.PackageUtils

class PermissionService(private val context: Context) {

    fun isBackgroundLocationEnabled(): Boolean {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            canGetFineLocation()
        } else {
            hasPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
    }

    fun canGetFineLocation(): Boolean {
        return hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    fun canGetCoarseLocation(): Boolean {
        return hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    fun canUseFlashlight(): Boolean {
        return hasPermission("android.permission.FLASHLIGHT")
    }

    fun isCameraEnabled(): Boolean {
        return hasPermission(Manifest.permission.CAMERA)
    }

    fun canUseBluetooth(): Boolean {
        return hasPermission(Manifest.permission.BLUETOOTH)
    }

    fun canRecognizeActivity(): Boolean {
        return hasPermission(Manifest.permission.ACTIVITY_RECOGNITION)
    }

    fun canVibrate(): Boolean {
        return hasPermission(Manifest.permission.VIBRATE)
    }

    fun canRecordAudio(): Boolean {
        return hasPermission(Manifest.permission.RECORD_AUDIO)
    }

    fun getPermissionName(permission: String): String? {
        return try {
            val info = context.packageManager.getPermissionInfo(permission, 0)
            info.loadLabel(context.packageManager).toString()
        } catch (e: Exception) {
            null
        }
    }

    fun getRequestedPermissions(): List<String> {
        val info = context.packageManager.getPackageInfo(
            context.packageName,
            PackageManager.GET_PERMISSIONS
        )
        return info.requestedPermissions.asList()
    }

    fun getGrantedPermissions(): List<String> {
        val info = context.packageManager.getPackageInfo(
            context.packageName,
            PackageManager.GET_PERMISSIONS
        )
        return info.requestedPermissions.filterIndexed { i, _ -> (info.requestedPermissionsFlags[i] and PackageInfo.REQUESTED_PERMISSION_GRANTED) == PackageInfo.REQUESTED_PERMISSION_GRANTED }
    }

    fun isIgnoringBatteryOptimizations(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context.getSystemService<PowerManager>()
                ?.isIgnoringBatteryOptimizations(PackageUtils.getPackageName(context)) ?: false
        } else {
            true
        }
    }

    fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }
}