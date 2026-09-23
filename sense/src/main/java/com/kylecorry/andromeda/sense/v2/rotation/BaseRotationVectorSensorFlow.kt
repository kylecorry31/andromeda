package com.kylecorry.andromeda.sense.v2.rotation

import android.content.Context
import android.hardware.SensorEvent
import android.hardware.SensorManager
import com.kylecorry.andromeda.sense.v2.BaseSensorManagerSensorFlow
import com.kylecorry.andromeda.sense.v2.SensorAccuracy
import com.kylecorry.andromeda.sense.v2.SensorManagerSensorFlowConfig
import com.kylecorry.sol.math.Quaternion
import java.time.Instant
import kotlin.math.PI

abstract class BaseRotationVectorSensorFlow(
    context: Context,
    sensorType: Int,
    config: SensorManagerSensorFlowConfig,
    private val supportsHeadingAccuracy: Boolean
) : BaseSensorManagerSensorFlow<RotationVectorReading>(context, sensorType, config) {
    private val quaternion = FloatArray(4)

    final override fun map(event: SensorEvent): RotationVectorReading {
        SensorManager.getQuaternionFromVector(quaternion, event.values)
        val headingAccuracy = if (
            supportsHeadingAccuracy && event.values.size > 4 && event.values[4] > 0f
        ) {
            (event.values[4] * 180f / PI.toFloat())
        } else {
            null
        }

        return RotationVectorReading(
            eventTime = Instant.now(),
            eventTimeElapsedNanos = event.timestamp,
            quaternion = Quaternion(quaternion[1], quaternion[2], quaternion[3], quaternion[0]),
            headingAccuracy = headingAccuracy,
            accuracy = SensorAccuracy.fromAndroid(event.accuracy)
        )
    }
}
