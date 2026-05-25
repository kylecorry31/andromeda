package com.kylecorry.andromeda.battery

import androidx.test.platform.app.InstrumentationRegistry
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Test

class BatteryTest {

    @Test
    fun canReadBatteryValues() = runBlocking {
        val ctx = InstrumentationRegistry.getInstrumentation().context

        val battery = Battery(ctx)

        withTimeout(2000) {
            battery.read()
        }

        assertTrue(battery.hasValidReading)
        assertTrue(battery.percent >= 0f)
        assertTrue(battery.capacity >= 0f)
        assertEquals(BatteryHealth.Good, battery.health)
        assertTrue(battery.chargingStatus != BatteryChargingStatus.Unknown)
        assertTrue(battery.chargingMethod != BatteryChargingMethod.Unknown)
        assertTrue(battery.temperature.isFinite() && battery.temperature >= 0f)
    }

}
