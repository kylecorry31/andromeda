package com.kylecorry.andromeda.sense.v2.clinometer

import com.kylecorry.andromeda.sense.v2.SensorFlow
import com.kylecorry.andromeda.sense.v2.orientation.OrientationFlowMath
import com.kylecorry.andromeda.sense.v2.readings.QuaternionReading
import com.kylecorry.andromeda.sense.v2.readings.SensorAccuracyReading
import com.kylecorry.sol.math.trigonometry.Trigonometry.normalizeAngle
import com.kylecorry.sol.science.geology.Geology
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ClinometerSensorFlow<T>(
    private val orientation: SensorFlow<T>,
    var isAugmentedReality: Boolean = false
) : SensorFlow<ClinometerReading>() where T : SensorAccuracyReading, T : QuaternionReading {
    override suspend fun ProducerScope<ClinometerReading>.setupListener() {
        val readingJob = launch {
            orientation.flow.collect { reading ->
                val angle = if (isAugmentedReality) {
                    val pitch = OrientationFlowMath.getOrientation(reading.quaternion)[1]
                    val arPitch = OrientationFlowMath.getOrientation(
                        reading.quaternion,
                        augmentedReality = true
                    )[1]
                    if (pitch < 0f) normalizeAngle(arPitch) else normalizeAngle(-arPitch - 180f)
                } else {
                    val roll = OrientationFlowMath.getOrientation(
                        reading.quaternion,
                        augmentedReality = true
                    )[2]
                    normalizeAngle(-roll + 90f)
                }
                trySend(
                    ClinometerReading(
                        eventTime = reading.eventTime,
                        eventTimeElapsedNanos = reading.eventTimeElapsedNanos,
                        angle = angle,
                        incline = Geology.getInclination(angle),
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
