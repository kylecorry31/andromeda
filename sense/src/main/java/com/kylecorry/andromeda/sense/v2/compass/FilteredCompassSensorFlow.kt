package com.kylecorry.andromeda.sense.v2.compass

import com.kylecorry.andromeda.sense.v2.SensorFlow
import com.kylecorry.sol.math.filters.IFilter
import com.kylecorry.sol.math.trigonometry.Trigonometry.deltaAngle
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class FilteredCompassSensorFlow(
    private val compass: SensorFlow<CompassReading>,
    private val filter: IFilter
) : SensorFlow<CompassReading>() {
    private var filteredBearing = 0f
    private var bearing = 0f

    override suspend fun ProducerScope<CompassReading>.setupListener() {
        val readingJob = launch {
            compass.flow.collect { reading ->
                bearing += deltaAngle(bearing, reading.rawBearing)
                filteredBearing = filter.filter(bearing)
                trySend(reading.copy(rawBearing = Bearing.getBearing(filteredBearing)))
            }
        }
        val activeJob = launch { compass.isActive.collect(::setIsActive) }
        awaitClose {
            readingJob.cancel()
            activeJob.cancel()
            setIsActive(false)
        }
    }
}
