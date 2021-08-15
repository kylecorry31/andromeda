package com.kylecorry.andromeda.core.units

data class Pressure(val pressure: Float, val units: PressureUnits) : Comparable<Pressure> {

    fun convertTo(toUnits: PressureUnits): Pressure {
        if (units == toUnits) {
            return Pressure(pressure, units)
        }

        val hpa = when (units) {
            PressureUnits.Hpa -> pressure
            PressureUnits.Mbar -> pressure
            PressureUnits.Inhg -> pressure / 0.02953f
            PressureUnits.Psi -> pressure / 0.0145037738f
        }

        val newPressure = when (toUnits) {
            PressureUnits.Hpa -> hpa
            PressureUnits.Inhg -> 0.02953f * hpa
            PressureUnits.Mbar -> hpa
            PressureUnits.Psi -> 0.0145037738f * hpa
        }

        return Pressure(newPressure, toUnits)
    }

    fun hpa(): Pressure {
        return convertTo(PressureUnits.Hpa)
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Pressure) {
            return false
        }

        return compareTo(other) == 0
    }

    override fun compareTo(other: Pressure): Int {
        val hpa = convertTo(PressureUnits.Hpa).pressure
        val otherHpa = other.convertTo(PressureUnits.Hpa).pressure

        return when {
            hpa > otherHpa -> {
                1
            }
            otherHpa > hpa -> {
                -1
            }
            else -> {
                0
            }
        }
    }

    override fun hashCode(): Int {
        var result = pressure.hashCode()
        result = 31 * result + units.hashCode()
        return result
    }


}