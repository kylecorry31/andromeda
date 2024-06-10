package com.kylecorry.andromeda.sound

import android.util.Range
import com.kylecorry.sol.math.SolMath
import org.jetbrains.annotations.ApiStatus.Experimental
import kotlin.math.log10
import kotlin.math.pow

object AudioUtils {

    /**
     * Converts a PCM 16-bit buffer to a float array
     *
     * @param buffer the PCM 16-bit buffer. Must have an even number of bytes.
     * @param out the output array, must be exactly half the size of the input buffer
     * @return the float array (same as the out parameter). Range [-1, 1]
     */
    fun pcm16ToFloat(
        buffer: ByteArray,
        out: FloatArray = FloatArray(buffer.size / 2)
    ): FloatArray {
        if (out.size != buffer.size / 2) {
            throw IllegalArgumentException("Output array must be half the size of the input array")
        }

        if (buffer.size % 2 != 0) {
            throw IllegalArgumentException("Input buffer must have an even number of bytes")
        }

        for (i in 0 until buffer.size / 2) {
            out[i] =
                (buffer[i * 2].toInt() and 0xFF or (buffer[i * 2 + 1].toInt() shl 8)).toFloat() / Short.MAX_VALUE
        }
        return out
    }

    /**
     * Converts an amplitude [-1, 1] as read from the microphone to a dB value
     * @param amplitude the amplitude
     * @param decibelRange the range of decibels the microphone can read
     * @param sensitivity the sensitivity of the microphone
     * @param referenceSoundPressure the reference sound pressure in Pa (default is 0.00002f or 20 uPa / 0 dB SPL)
     * @return the dB value
     */
    @Experimental
    fun amplitudeToDecibels(
        amplitude: Float,
        decibelRange: Range<Float>,
        sensitivity: Float = 0f,
        referenceSoundPressure: Float = 0.00002f
    ): Float {
        // TODO: Add sensitivity before or after the calculation?
        val maxDb = (decibelRange.upper + sensitivity).coerceAtLeast(0f)
        val minDb = (decibelRange.lower + sensitivity).coerceAtLeast(0f)

        // Convert dB to sound pressure
        val maxSoundPressurePa = referenceSoundPressure * 10f.pow(maxDb / 20)
        val minSoundPressurePa = referenceSoundPressure * 10f.pow(minDb / 20)

        // Interpolate between the min and max sound pressure
        val readingSoundPressurePa = SolMath.lerp(amplitude, minSoundPressurePa, maxSoundPressurePa)

        // Convert sound pressure to dB
        return 20 * log10(readingSoundPressurePa / referenceSoundPressure)
    }

}