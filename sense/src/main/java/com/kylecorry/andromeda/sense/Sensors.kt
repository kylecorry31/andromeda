package com.kylecorry.andromeda.sense

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.core.content.getSystemService

object Sensors {

    private fun getSensorManager(context: Context): SensorManager? {
        return context.getSystemService()
    }

    fun hasBarometer(context: Context): Boolean {
        return hasSensor(context, Sensor.TYPE_PRESSURE)
    }

    fun hasGyroscope(context: Context): Boolean {
        return hasSensor(context, Sensor.TYPE_GYROSCOPE)
    }

    fun hasGravity(context: Context): Boolean {
        return hasSensor(context, Sensor.TYPE_GRAVITY)
    }

    @Suppress("DEPRECATION")
    fun hasCompass(context: Context): Boolean {
        return (hasSensor(context, Sensor.TYPE_MAGNETIC_FIELD) && (hasSensor(
            context,
            Sensor.TYPE_ACCELEROMETER
        ) || hasGravity(context))) || hasSensor(
            context,
            Sensor.TYPE_ORIENTATION
        )
    }

    fun hasThermometer(@Suppress("UNUSED_PARAMETER") context: Context): Boolean {
        // True because of battery sensor (maybe check to see if the battery has the thermometer)
        return true
    }

    fun hasHygrometer(context: Context): Boolean {
        return hasSensor(context, Sensor.TYPE_RELATIVE_HUMIDITY)
    }

    fun hasSensor(context: Context, sensorCode: Int): Boolean {
        val sensors = getSensorManager(context)?.getSensorList(sensorCode)
        return sensors?.isNotEmpty() ?: false
    }

    fun hasSensorLike(context: Context, name: String): Boolean {
        val sensors = getSensorManager(context)?.getSensorList(Sensor.TYPE_ALL)
        return sensors?.any { it.name.contains(name, true) } ?: false
    }

}