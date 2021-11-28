package com.kylecorry.andromeda.files

import java.io.*

class FileSaver(private val autoClose: Boolean = true) {

    fun save(input: InputStream, output: OutputStream) {
        try {
            val buf = ByteArray(1024)
            var len: Int
            while (input.read(buf).also { len = it } > 0) {
                output.write(buf, 0, len)
            }
        } finally {
            if (autoClose) {
                try {
                    input.close()
                } catch (e: Exception){
                    // Do nothing
                }
                try {
                    output.close()
                } catch (e: Exception){
                    // Do nothing
                }
            }
        }
    }

    fun save(input: InputStream, output: File) {
        FileOutputStream(output).use {
            save(input, it)
        }
    }

    fun save(input: File, output: File) {
        FileInputStream(input).use { inputStream ->
            FileOutputStream(output).use { outputStream ->
                save(inputStream, outputStream)
            }
        }
    }

}