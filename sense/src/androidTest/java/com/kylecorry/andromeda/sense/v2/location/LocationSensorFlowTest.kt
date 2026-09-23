package com.kylecorry.andromeda.sense.v2.location

import android.Manifest
import android.location.LocationManager
import androidx.test.platform.app.InstrumentationRegistry
import com.kylecorry.andromeda.sense.location.LocationRequestConfig
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class LocationSensorFlowTest {

    @Before
    fun setup() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.context
        instrumentation.uiAutomation.grantRuntimePermission(
            context.packageName,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        instrumentation.uiAutomation.grantRuntimePermission(
            context.packageName,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        instrumentation.uiAutomation.grantRuntimePermission(
            context.packageName,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        )
    }

    @Test
    fun isActiveWhileCollecting() = runBlocking {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.context
        val sensor = LocationSensorFlow(context, LocationManager.GPS_PROVIDER)
        val collector = launch { sensor.flow.collect() }

        try {
            withTimeout(5_000.milliseconds) { sensor.isActive.first { it } }
            Assert.assertTrue(sensor.isActive.value)
        } finally {
            collector.cancelAndJoin()
        }

        withTimeout(5_000.milliseconds) { sensor.isActive.first { !it } }
        Assert.assertFalse(sensor.isActive.value)
    }

    @Test
    fun canCollectLocations() = runBlocking {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.context

        val sensor = LocationSensorFlow(
            context,
            LocationManager.GPS_PROVIDER,
            LocationRequestConfig(frequency = Duration.ofSeconds(1))
        )
        val location = withTimeout(10_000.milliseconds) { sensor.flow.first() }
        Assert.assertNotNull(location)
    }

    @Test
    fun prepopulatesLastKnownLocation() = runBlocking {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.context

        val sensor = LocationSensorFlow(context, LocationManager.GPS_PROVIDER)
        Assert.assertNotNull(sensor.lastKnown.value)
    }
}
