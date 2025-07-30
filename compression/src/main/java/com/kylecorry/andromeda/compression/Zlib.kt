package com.kylecorry.andromeda.compression

import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.InflaterInputStream

object Zlib {

    fun decompress(input: InputStream, output: OutputStream, closeOutput: Boolean = false) {
        val inflater = InflaterInputStream(input)
        try {
            inflater.copyTo(output)
        } finally {
            inflater.close()
            if (closeOutput) {
                output.close()
            }
        }
    }

    fun decompress(input: InputStream): ByteArray {
        val output = ByteArrayOutputStream()
        decompress(input, output, true)
        return output.toByteArray()
    }

    fun decompress(bytes: ByteArray): ByteArray {
        return decompress(bytes.inputStream())
    }

}