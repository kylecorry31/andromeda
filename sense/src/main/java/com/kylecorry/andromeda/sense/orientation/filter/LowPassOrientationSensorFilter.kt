package com.kylecorry.andromeda.sense.orientation.filter

import com.kylecorry.sol.math.Quaternion
import com.kylecorry.sol.math.QuaternionMath

class LowPassOrientationSensorFilter(
    var alpha: Float,
    var useSlerp: Boolean = true
) : OrientationSensorFilter {

    val value: FloatArray
        get() = synchronized(lock) {
            _current.copyOf()
        }

    private val lock = Any()
    private val _current = Quaternion.zero.toFloatArray()

    override fun filter(quaternion: FloatArray, out: FloatArray) {
        synchronized(lock) {
            if (useSlerp) {
                QuaternionMath.slerp(_current, quaternion, alpha, _current, true)
            } else {
                QuaternionMath.lerp(_current, quaternion, alpha, _current, true)
            }
            QuaternionMath.normalize(_current, _current)
            _current.copyInto(out)
        }
    }

    override fun reset(value: FloatArray) {
        synchronized(lock) {
            value.copyInto(_current)
        }
    }
}