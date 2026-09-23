package com.kylecorry.andromeda.sense.v2.altitude

import android.content.Context
import android.hardware.SensorManager
import com.kylecorry.andromeda.sense.v2.SensorFlow
import com.kylecorry.andromeda.sense.v2.SensorManagerSensorFlowConfig
import com.kylecorry.andromeda.sense.v2.barometer.BarometerReading
import com.kylecorry.andromeda.sense.v2.barometer.BarometerSensorFlow
import com.kylecorry.sol.units.Distance
import com.kylecorry.sol.units.Pressure
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class BarometricAltimeterSensorFlow(
    private val barometer: SensorFlow<BarometerReading>,
    var seaLevelPressure: Pressure = Pressure.hpa(SensorManager.PRESSURE_STANDARD_ATMOSPHERE)
) : SensorFlow<BarometricAltimeterReading>() {

    constructor(
        context: Context,
        seaLevelPressure: Pressure = Pressure.hpa(SensorManager.PRESSURE_STANDARD_ATMOSPHERE),
        config: SensorManagerSensorFlowConfig = SensorManagerSensorFlowConfig()
    ) : this(BarometerSensorFlow(context, config), seaLevelPressure)

    override suspend fun ProducerScope<BarometricAltimeterReading>.setupListener() {
        val readingJob = launch {
            barometer.flow.collect { reading ->
                val altitude = SensorManager.getAltitude(
                    seaLevelPressure.hpa().value,
                    reading.pressure.hpa().value
                )
                trySend(
                    BarometricAltimeterReading(
                        eventTime = reading.eventTime,
                        eventTimeElapsedNanos = reading.eventTimeElapsedNanos,
                        altitude = Distance.meters(altitude),
                        accuracy = reading.accuracy
                    )
                )
            }
        }
        val activeJob = launch {
            barometer.isActive.collect(::setIsActive)
        }

        awaitClose {
            readingJob.cancel()
            activeJob.cancel()
            setIsActive(false)
        }
    }
}
