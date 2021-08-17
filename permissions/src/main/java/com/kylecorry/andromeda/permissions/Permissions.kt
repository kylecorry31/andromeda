package com.kylecorry.andromeda.permissions

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import com.kylecorry.andromeda.core.system.Package

object Permissions {

    fun isBackgroundLocationEnabled(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            canGetFineLocation(context)
        } else {
            hasPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
    }

    fun canGetFineLocation(context: Context): Boolean {
        return hasPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
    }

    fun canGetCoarseLocation(context: Context): Boolean {
        return hasPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    fun canUseFlashlight(context: Context): Boolean {
        return hasPermission(context, "android.permission.FLASHLIGHT") || isCameraEnabled(context)
    }

    fun isCameraEnabled(context: Context): Boolean {
        return hasPermission(context, Manifest.permission.CAMERA)
    }

    fun canUseBluetooth(context: Context): Boolean {
        return hasPermission(context, Manifest.permission.BLUETOOTH)
    }

    fun canRecognizeActivity(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            hasPermission(context, Manifest.permission.ACTIVITY_RECOGNITION)
        } else {
            true
        }
    }

    fun canVibrate(context: Context): Boolean {
        return hasPermission(context, Manifest.permission.VIBRATE)
    }

    fun canRecordAudio(context: Context): Boolean {
        return hasPermission(context, Manifest.permission.RECORD_AUDIO)
    }

    fun getPermissionName(context: Context, permission: String): String? {
        return try {
            val info = context.packageManager.getPermissionInfo(permission, 0)
            info.loadLabel(context.packageManager).toString()
        } catch (e: Exception) {
            null
        }
    }

    fun getRequestedPermissions(context: Context): List<String> {
        val info = context.packageManager.getPackageInfo(
            context.packageName,
            PackageManager.GET_PERMISSIONS
        )
        return info.requestedPermissions.asList()
    }

    fun getGrantedPermissions(context: Context): List<String> {
        val info = context.packageManager.getPackageInfo(
            context.packageName,
            PackageManager.GET_PERMISSIONS
        )
        return info.requestedPermissions.filterIndexed { i, _ -> (info.requestedPermissionsFlags[i] and PackageInfo.REQUESTED_PERMISSION_GRANTED) == PackageInfo.REQUESTED_PERMISSION_GRANTED }
    }

    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context.getSystemService<PowerManager>()
                ?.isIgnoringBatteryOptimizations(Package.getPackageName(context)) ?: false
        } else {
            true
        }
    }

    fun hasPermission(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("BatteryLife")
    fun requestIgnoreBatteryOptimization(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return
        }

        if (isIgnoringBatteryOptimizations(context)) {
            return
        }

        if (!hasPermission(context, Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)) {
            return
        }

        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
        val uri = Uri.fromParts("package", Package.getPackageName(context), null)
        intent.data = uri
        context.startActivity(intent)
    }
}