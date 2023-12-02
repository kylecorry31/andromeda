package com.kylecorry.andromeda.sense

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.core.content.getSystemService
import com.kylecorry.andromeda.core.topics.generic.ITopic
import com.kylecorry.andromeda.core.topics.generic.Topic
import com.kylecorry.andromeda.core.topics.generic.filter
import com.kylecorry.andromeda.core.topics.generic.map
import com.kylecorry.andromeda.core.tryOrLog
import com.kylecorry.luna.coroutines.ListenerFlowWrapper
import java.util.concurrent.Flow

class FlowSensor(
    context: Context,
    private val sensorType: Int,
    private val sensorDelay: Int
) : ListenerFlowWrapper<SensorReading>() {

    val value: FloatArray?
        get() = lastValue?.clone()

    val accuracy: Int?
        get() = lastAccuracy

    val hasReading: Boolean
        get() = lastValue != null

    private var lastValue: FloatArray? = null
    private var lastAccuracy: Int? = null

    private val sensorManager = context.getSystemService<SensorManager>()
    private val sensorListener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            lastAccuracy = accuracy
            emit(SensorReading(value, this@FlowSensor.accuracy))
        }

        override fun onSensorChanged(event: SensorEvent) {
            lastValue = event.values.clone()
            lastAccuracy = event.accuracy
            emit(SensorReading(value, accuracy))
        }
    }

    override fun start() {
        sensorManager?.getDefaultSensor(sensorType)?.also { sensor ->
            sensorManager.registerListener(
                sensorListener,
                sensor,
                sensorDelay
            )
        }
    }

    override fun stop() {
        sensorManager?.unregisterListener(sensorListener)
    }
}

data class SensorReading(val values: FloatArray?, val accuracy: Int?) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SensorReading

        if (values != null) {
            if (other.values == null) return false
            if (!values.contentEquals(other.values)) return false
        } else if (other.values != null) return false
        return accuracy == other.accuracy
    }

    override fun hashCode(): Int {
        var result = values?.contentHashCode() ?: 0
        result = 31 * result + (accuracy ?: 0)
        return result
    }
}