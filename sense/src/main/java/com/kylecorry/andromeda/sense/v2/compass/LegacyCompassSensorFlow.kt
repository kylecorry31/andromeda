package com.kylecorry.andromeda.sense.v2.compass

import android.content.Context
import android.hardware.SensorManager
import com.kylecorry.andromeda.sense.v2.SensorFlow
import com.kylecorry.andromeda.sense.v2.SensorManagerSensorFlowConfig
import com.kylecorry.andromeda.sense.v2.orientation.OrientationReading
import com.kylecorry.andromeda.sense.v2.orientation.OrientationSensorFlow
import com.kylecorry.sol.units.Bearing
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class LegacyCompassSensorFlow(
    private val orientation: SensorFlow<OrientationReading>,
    private val useTrueNorth: Boolean = false,
    var declination: Float = 0f
) : SensorFlow<CompassReading>() {
    constructor(
        context: Context,
        useTrueNorth: Boolean,
        config: SensorManagerSensorFlowConfig = SensorManagerSensorFlowConfig(
        sensorDelayUs = SensorManager.SENSOR_DELAY_GAME
    )
    ) : this(OrientationSensorFlow(context, config), useTrueNorth)

    override suspend fun ProducerScope<CompassReading>.setupListener() {
        val readingJob = launch {
            orientation.flow.collect { reading ->
                val bearing = reading.orientation.yaw + if (useTrueNorth) declination else 0f
                trySend(
                    CompassReading(
                        eventTime = reading.eventTime,
                        eventTimeElapsedNanos = reading.eventTimeElapsedNanos,
                        rawBearing = Bearing.getBearing(bearing),
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
