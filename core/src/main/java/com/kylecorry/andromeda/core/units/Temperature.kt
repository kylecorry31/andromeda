package com.kylecorry.andromeda.core.units

data class Temperature(val temperature: Float, val units: TemperatureUnits) {

    fun convertTo(toUnits: TemperatureUnits): Temperature {
       val c = when (units) {
            TemperatureUnits.C -> temperature
            TemperatureUnits.F -> (temperature - 32) * 5 / 9
        }

        val newTemp = when (toUnits) {
            TemperatureUnits.C -> c
            TemperatureUnits.F -> (c * 9 / 5) + 32
        }

        return Temperature(newTemp, toUnits)
    }

    fun celsius(): Temperature {
        return convertTo(TemperatureUnits.C)
    }
}