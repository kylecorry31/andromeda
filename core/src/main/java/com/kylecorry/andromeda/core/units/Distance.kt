package com.kylecorry.andromeda.core.units

data class Distance(val distance: Float, val units: DistanceUnits) {

    fun convertTo(newUnits: DistanceUnits): Distance {
        val m = distance * units.meters
        val newDistance = m / newUnits.meters
        return Distance(newDistance, newUnits)
    }

    fun meters(): Distance {
        return convertTo(DistanceUnits.Meters)
    }

    operator fun times(value: Float): Distance {
        return Distance(distance * value, units)
    }

    companion object {

        fun meters(distance: Float): Distance {
            return Distance(distance, DistanceUnits.Meters)
        }

        fun feet(distance: Float): Distance {
            return Distance(distance, DistanceUnits.Feet)
        }

        fun centimeters(distance: Float): Distance {
            return Distance(distance, DistanceUnits.Centimeters)
        }

        fun kilometers(distance: Float): Distance {
            return Distance(distance, DistanceUnits.Kilometers)
        }

        fun miles(distance: Float): Distance {
            return Distance(distance, DistanceUnits.Miles)
        }

        fun nauticalMiles(distance: Float): Distance {
            return Distance(distance, DistanceUnits.NauticalMiles)
        }

        fun yards(distance: Float): Distance {
            return Distance(distance, DistanceUnits.Yards)
        }

    }

}