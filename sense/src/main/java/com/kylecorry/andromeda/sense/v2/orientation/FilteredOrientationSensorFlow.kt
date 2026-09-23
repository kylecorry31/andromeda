package com.kylecorry.andromeda.sense.v2.orientation

import com.kylecorry.andromeda.sense.orientation.filter.OrientationSensorFilter
import com.kylecorry.andromeda.sense.v2.SensorFlow
import com.kylecorry.andromeda.sense.v2.readings.QuaternionReading
import com.kylecorry.andromeda.sense.v2.readings.SensorAccuracyReading
import com.kylecorry.sol.math.Quaternion
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/** Filters quaternion orientation readings while preserving their event metadata. */
class FilteredOrientationSensorFlow<T>(
    private val source: SensorFlow<T>,
    private val filter: OrientationSensorFilter,
    private val maintainStateOnRestart: Boolean = false
) : SensorFlow<QuaternionOrientationReading>() where T : SensorAccuracyReading, T : QuaternionReading {
    private val reading = Quaternion.zero.toFloatArray()
    private var isFilterInitialized = false

    override suspend fun ProducerScope<QuaternionOrientationReading>.setupListener() {
        if (!maintainStateOnRestart) Quaternion.zero.toFloatArray().copyInto(reading)
        var hasReading = false

        val readingJob = launch {
            source.flow.collect { input ->
                val inputQuaternion = input.quaternion.toFloatArray()
                if (!isFilterInitialized || (!hasReading && !maintainStateOnRestart)) {
                    inputQuaternion.copyInto(reading)
                    hasReading = true
                    isFilterInitialized = true
                    filter.reset(reading)
                } else {
                    hasReading = true
                    filter.filter(inputQuaternion, reading)
                }
                trySend(
                    QuaternionOrientationReading(
                        eventTime = input.eventTime,
                        eventTimeElapsedNanos = input.eventTimeElapsedNanos,
                        quaternion = Quaternion.from(reading),
                        accuracy = input.accuracy
                    )
                )
            }
        }
        val activeJob = launch { source.isActive.collect(::setIsActive) }
        awaitClose {
            readingJob.cancel()
            activeJob.cancel()
            setIsActive(false)
        }
    }
}
