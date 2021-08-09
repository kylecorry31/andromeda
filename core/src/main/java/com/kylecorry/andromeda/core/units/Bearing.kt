package com.kylecorry.andromeda.core.units

import com.kylecorry.andromeda.core.math.normalizeAngle
import kotlin.math.roundToInt

class Bearing(_value: Float){
    val value: Float = if (_value.isNaN() || !_value.isFinite()) 0f else normalizeAngle(_value)

    val direction: CompassDirection
            get(){
                val directions = CompassDirection.values()
                val a = ((value / 45f).roundToInt() * 45f) % 360
                directions.forEach {
                    if (a == it.azimuth){
                        return it
                    }
                }
                return CompassDirection.North
            }

    val mils: Float = value * 17.7777778f

    fun withDeclination(declination: Float): Bearing {
        return Bearing(value + declination)
    }

    fun inverse(): Bearing {
        return Bearing(value + 180)
    }

    companion object {
        fun from(direction: CompassDirection): Bearing {
            return Bearing(direction.azimuth)
        }

        fun getBearing(degrees: Float): Float {
            return if (degrees.isNaN() || !degrees.isFinite()) 0f else normalizeAngle(degrees)
        }
    }
}