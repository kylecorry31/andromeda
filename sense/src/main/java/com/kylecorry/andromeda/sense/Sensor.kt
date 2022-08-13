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

class Sensor(
    context: Context,
    private val sensorType: Int,
    private val sensorDelay: Int
) {

    private val sensorManager = context.getSystemService<SensorManager>()
    private val sensorListener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            this@Sensor.onAccuracyChanged(accuracy)
        }

        override fun onSensorChanged(event: SensorEvent) {
            this@Sensor.onSensorChanged(event)
        }

    }

    private val topic = Topic.lazy<Pair<FloatArray?, Int?>>(
        {
            sensorManager?.getDefaultSensor(sensorType)?.also { sensor ->
                sensorManager.registerListener(
                    sensorListener,
                    sensor,
                    sensorDelay
                )
            }
        },
        {
            tryOrLog {
                sensorManager?.unregisterListener(sensorListener)
            }
        }
    )

    val accuracy: ITopic<Int> = topic.filter { it.second != null }.map { it.second!! }
    val reading: ITopic<FloatArray> = topic.filter { it.first != null }.map { it.first!! }

    private fun onAccuracyChanged(accuracy: Int) {
        val last = topic.value
        val data = if (last.isPresent) last.get().first else null
        topic.publish(data to accuracy)
    }

    private fun onSensorChanged(event: SensorEvent) {
        topic.publish(event.values.clone() to event.accuracy)
    }


}