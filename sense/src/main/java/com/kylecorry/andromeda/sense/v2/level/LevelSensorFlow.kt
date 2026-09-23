package com.kylecorry.andromeda.sense.v2.level

import android.view.Surface
import com.kylecorry.andromeda.sense.v2.SensorFlow
import com.kylecorry.andromeda.sense.v2.orientation.OrientationFlowMath
import com.kylecorry.andromeda.sense.v2.readings.QuaternionReading
import com.kylecorry.andromeda.sense.v2.readings.SensorAccuracyReading
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class LevelSensorFlow<T>(
    private val orientation: SensorFlow<T>,
    var surfaceRotation: Int = Surface.ROTATION_0
) : SensorFlow<LevelReading>() where T : SensorAccuracyReading, T : QuaternionReading {
    override suspend fun ProducerScope<LevelReading>.setupListener() {
        val readingJob = launch {
            orientation.flow.collect { reading ->
                val angles = OrientationFlowMath.getOrientation(
                    reading.quaternion,
                    surfaceRotation
                )
                trySend(
                    LevelReading(
                        eventTime = reading.eventTime,
                        eventTimeElapsedNanos = reading.eventTimeElapsedNanos,
                        x = -angles[2].coerceIn(-90f, 90f),
                        y = -angles[1].coerceIn(-90f, 90f),
                        accuracy = reading.accuracy
                    )
                )
            }
        }
        val activeJob = launch { orientation.isActive.collect(::setIsActive) }
        awaitClose {
            readingJob.cancel()
            activeJob.cancel()
            setIsActive(false)
        }
    }
}
