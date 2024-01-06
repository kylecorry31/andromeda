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
    private val _last = Quaternion.zero.toFloatArray()


    override fun filter(quaternion: FloatArray, out: FloatArray) {
        // Don't bother filtering if the quaternion is invalid
        if (quaternion.any { it.isNaN() || it.isInfinite() }) return

        synchronized(lock) {
            // Make a copy of the last value in case we need to roll back
            _current.copyInto(_last)

            // Low pass filter
            if (useSlerp) {
                QuaternionMath.slerp(_current, quaternion, alpha, _current, true)
            } else {
                QuaternionMath.lerp(_current, quaternion, alpha, _current, true)
            }
            QuaternionMath.normalize(_current, _current)


            // If the new value is invalid, roll back
            if (_current.any { it.isNaN() || it.isInfinite() }) {
                _last.copyInto(_current)
            }

            // Write it to the output
            _current.copyInto(out)
        }
    }

    override fun reset(value: FloatArray) {
        synchronized(lock) {
            value.copyInto(_current)
        }
    }
}