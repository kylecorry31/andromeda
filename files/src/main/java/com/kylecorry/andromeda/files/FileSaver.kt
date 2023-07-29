package com.kylecorry.andromeda.files

import java.io.*

class FileSaver(private val autoClose: Boolean = true) {

    fun save(input: InputStream, output: OutputStream) {
        try {
            input.copyTo(output, 1024)
        } finally {
            if (autoClose) {
                try {
                    input.close()
                } catch (e: Exception) {
                    // Do nothing
                }
                try {
                    output.close()
                } catch (e: Exception) {
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

    fun save(input: File, output: OutputStream) {
        FileInputStream(input).use { inputStream ->
            save(inputStream, output)
        }
    }

}