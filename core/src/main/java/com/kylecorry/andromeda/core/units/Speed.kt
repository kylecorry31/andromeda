package com.kylecorry.andromeda.core.units

data class Speed(val speed: Float, val distanceUnits: DistanceUnits, val timeUnits: TimeUnits){

    fun convertTo(newDistanceUnits: DistanceUnits, newTimeUnits: TimeUnits): Speed {
        val distance = Distance(speed, distanceUnits).convertTo(newDistanceUnits).distance
        val newSpeed = (distance / timeUnits.seconds) * newTimeUnits.seconds
        return Speed(newSpeed, newDistanceUnits, newTimeUnits)
    }

}
