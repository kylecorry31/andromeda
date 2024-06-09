package com.kylecorry.andromeda.sound

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

}