package com.kylecorry.andromeda.exceptions

import android.content.Context
import com.kylecorry.andromeda.core.system.Android

class DeviceDetailsBugReportGenerator : IBugReportGenerator {
    override fun generate(context: Context, throwable: Throwable): String {
        val device = "${Android.fullDeviceName} (${Android.model})"
        return "Device: $device"
    }
}