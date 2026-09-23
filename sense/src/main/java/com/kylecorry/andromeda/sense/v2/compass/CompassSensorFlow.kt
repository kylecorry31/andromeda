package com.kylecorry.andromeda.sense.v2.compass

import android.view.Surface
import com.kylecorry.andromeda.sense.v2.SensorFlow
import com.kylecorry.andromeda.sense.v2.orientation.OrientationFlowMath
import com.kylecorry.andromeda.sense.v2.readings.QuaternionReading
import com.kylecorry.andromeda.sense.v2.readings.SensorAccuracyReading
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class CompassSensorFlow<T>(
    private val orientation: SensorFlow<T>,
    var useTrueNorth: Boolean = false,
    var isAugmentedReality: Boolean = false,
    var surfaceRotation: Int = Surface.ROTATION_0,
    var offset: Float = 0f
) : SensorFlow<CompassReading>() where T : SensorAccuracyReading, T : QuaternionReading {
    var declination: Float = 0f

    override suspend fun ProducerScope<CompassReading>.setupListener() {
        val readingJob = launch {
            orientation.flow.collect { reading ->
                val orientationValues = OrientationFlowMath.getOrientation(
                    reading.quaternion,
                    surfaceRotation,
                    isAugmentedReality,
                    if (useTrueNorth) declination else 0f
                )
                trySend(
                    CompassReading(
                        eventTime = reading.eventTime,
                        eventTimeElapsedNanos = reading.eventTimeElapsedNanos,
                        rawBearing = Bearing.getBearing(orientationValues[0] + offset),
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
