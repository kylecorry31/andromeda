package com.kylecorry.andromeda.sense.v2

import android.hardware.SensorManager

enum class SensorAccuracy {
    NoContact,
    Unreliable,
    Low,
    Medium,
    High;

    companion object {
        fun fromAndroid(accuracy: Int): SensorAccuracy {
            return when (accuracy) {
                SensorManager.SENSOR_STATUS_NO_CONTACT -> NoContact
                SensorManager.SENSOR_STATUS_UNRELIABLE -> Unreliable
                SensorManager.SENSOR_STATUS_ACCURACY_LOW -> Low
                SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> Medium
                SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> High
                else -> Unreliable
            }
        }
    }
}
