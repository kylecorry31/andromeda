package com.kylecorry.andromeda.permissions

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.core.content.getSystemService
import com.kylecorry.andromeda.core.system.Intents
import com.kylecorry.andromeda.core.system.Package
import com.kylecorry.andromeda.core.tryOrDefault

object Permissions {

    fun isBackgroundLocationEnabled(
        context: Context,
        requireFineLocation: Boolean = false
    ): Boolean {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            (requireFineLocation && canGetFineLocation(context)) || canGetLocation(context)
        } else {
            hasPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
    }

    fun canGetLocation(context: Context): Boolean {
        return canGetFineLocation(context) || canGetCoarseLocation(context)
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
        return tryOrDefault(null) {
            val info = context.packageManager.getPermissionInfo(permission, 0)
            info.loadLabel(context.packageManager).toString()
        }
    }

    fun getRequestedPermissions(context: Context): List<String> {
        val info = Package.getPackageInfo(context, flags = PackageManager.GET_PERMISSIONS)
        return info.requestedPermissions.asList()
    }

    fun getGrantedPermissions(context: Context): List<String> {
        val info = Package.getPackageInfo(context, flags = PackageManager.GET_PERMISSIONS)
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
        // The first call seems to check for notifications permission in addition, and the second checks for ops
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED && PermissionChecker.checkSelfPermission(
            context,
            permission
        ) == PermissionChecker.PERMISSION_GRANTED
    }

    fun hasPermission(context: Context, permission: SpecialPermission): Boolean {
        return when (permission) {
            SpecialPermission.SCHEDULE_EXACT_ALARMS -> canScheduleExactAlarms(context)
            SpecialPermission.IGNORE_BATTERY_OPTIMIZATIONS -> isIgnoringBatteryOptimizations(context)
        }
    }

    fun requestPermission(context: Context, permission: SpecialPermission) {
        when (permission) {
            SpecialPermission.SCHEDULE_EXACT_ALARMS -> requestScheduleExactAlarms(context)
            SpecialPermission.IGNORE_BATTERY_OPTIMIZATIONS -> requestIgnoreBatteryOptimization(
                context
            )
        }
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
            // Can't directly request this permission, so send the user to the settings page
            context.startActivity(Intents.batteryOptimizationSettings())
            return
        }

        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
        val uri = Uri.fromParts("package", Package.getPackageName(context), null)
        intent.data = uri
        context.startActivity(intent)
    }

    fun canScheduleExactAlarms(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return true
        }

        val alarm = context.getSystemService<AlarmManager>()

        return try {
            alarm?.canScheduleExactAlarms() ?: false
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Requests that the user allow the app to schedule exact alarms
     * You should display a notice with instructions before calling this - unfortunately Android does not do that by default
     */
    @SuppressLint("NewApi")
    fun requestScheduleExactAlarms(context: Context) {
        if (canScheduleExactAlarms(context)) {
            return
        }

        context.startActivity(Intents.alarmAndReminderSettings(context))
    }

    fun canCreateForegroundServices(
        context: Context,
        fromBackground: Boolean = false,
        foregroundServiceTypes: List<Int> = emptyList()
    ): Boolean {
        // Nothing special was needed before Android P
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            return true
        }

        val hasPermission = hasPermission(context, Manifest.permission.FOREGROUND_SERVICE)

        // Before Android S, the permission was enough to create foreground services at any time
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return hasPermission
        }

        if (!hasPermission) {
            return false
        }

        // On Android S+, the permission is not enough to create foreground services from the background
        // unless the app is ignoring battery optimizations
        if (fromBackground && !isIgnoringBatteryOptimizations(context)) {
            return false
        }

        // Only SDK 34+ requires a foreground service type check
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            return true
        }

        // Check the foreground service types
        if (foregroundServiceTypes.isEmpty()) {
            return true
        }

        val permissionMap = mapOf(
            ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA to Manifest.permission.FOREGROUND_SERVICE_CAMERA,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE to Manifest.permission.FOREGROUND_SERVICE_CONNECTED_DEVICE,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC to Manifest.permission.FOREGROUND_SERVICE_DATA_SYNC,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH to Manifest.permission.FOREGROUND_SERVICE_HEALTH,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION to Manifest.permission.FOREGROUND_SERVICE_LOCATION,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK to Manifest.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION to Manifest.permission.FOREGROUND_SERVICE_MEDIA_PROJECTION,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE to Manifest.permission.FOREGROUND_SERVICE_MICROPHONE,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL to Manifest.permission.FOREGROUND_SERVICE_PHONE_CALL,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_REMOTE_MESSAGING to Manifest.permission.FOREGROUND_SERVICE_REMOTE_MESSAGING,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE to Manifest.permission.FOREGROUND_SERVICE_SPECIAL_USE,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SYSTEM_EXEMPTED to Manifest.permission.FOREGROUND_SERVICE_SYSTEM_EXEMPTED,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SHORT_SERVICE to null
        )

        // Verify the manifest permission is granted for each type
        for (type in foregroundServiceTypes) {
            val permission = permissionMap[type] ?: continue
            if (!hasPermission(context, permission)) {
                return false
            }
        }

        // Extra permission checks for specific types
        val runtimePermissions = mapOf(
            ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA to listOf(Manifest.permission.CAMERA),
            // TODO: Also if the user grants access to a USB device
            ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE to listOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.UWB_RANGING,
                Manifest.permission.CHANGE_NETWORK_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_MULTICAST_STATE,
                Manifest.permission.NFC,
                Manifest.permission.TRANSMIT_IR
            ),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC to null,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH to listOf(
                Manifest.permission.ACTIVITY_RECOGNITION,
                Manifest.permission.BODY_SENSORS,
                Manifest.permission.HIGH_SAMPLING_RATE_SENSORS
            ),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION to listOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK to null,
            // TODO: The user must have allowed screen capture for the app
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION to null,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE to listOf(
                Manifest.permission.RECORD_AUDIO
            ),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL to listOf(
                Manifest.permission.MANAGE_OWN_CALLS
            ),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_REMOTE_MESSAGING to null,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE to null,
            // TODO: There are checks that can be done for this other than permissions
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SYSTEM_EXEMPTED to null,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SHORT_SERVICE to null
        )

        for (type in foregroundServiceTypes) {
            val permissions = runtimePermissions[type] ?: continue
            // Any of the permissions must be granted
            if (permissions.none { hasPermission(context, it) }) {
                return false
            }
        }

        return true
    }
}