package com.kylecorry.andromeda.sense.v2

import android.hardware.SensorManager

data class SensorManagerSensorFlowConfig(
    val sensorDelayUs: Int = SensorManager.SENSOR_DELAY_NORMAL,
    val maxReportLatencyUs: Int = 0
)
