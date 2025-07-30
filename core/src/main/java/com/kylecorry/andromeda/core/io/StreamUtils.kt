package com.kylecorry.andromeda.core.io

import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream

fun InputStream.readUntil(predicate: (char: Char) -> Boolean): String {
    val builder = StringBuilder()
    var b: Int
    while ((read().also { b = it }) > -1) {
        val char = b.toChar()
        if (predicate(char)) break
        builder.append(char)
    }

    return builder.toString()
}

fun InputStream.readUntil(stop: Char): String {
    return readUntil { it == stop }
}

fun InputStream.readBytesUntil(
    stopSequence: ByteArray,
    saveContent: Boolean = true,
    trimLines: Boolean = true,
    trimStartAndEndOnly: Boolean = false
): ByteArray {
    val builder = ByteArrayOutputStream()
    val endBuffer = ByteArray(stopSequence.size)

    var endIndex = 0
    while (true) {
        val byte = read()
        if (byte == -1) break

        if (!trimStartAndEndOnly || endIndex == 0) {
            if (trimLines && byte == '\n'.code) continue
            if (trimLines && byte == '\r'.code) continue
        }

        if (saveContent) {
            builder.write(byte)
        }

        if (endIndex == stopSequence.size) {
            // Shift the buffer to the left
            System.arraycopy(endBuffer, 1, endBuffer, 0, stopSequence.size - 1)
            endIndex = stopSequence.size - 1
        }

        endBuffer[endIndex] = byte.toByte()
        endIndex++

        if (endBuffer.contentEquals(stopSequence)) {
            return if (saveContent) {
                val bytes = builder.toByteArray()
                var stopIndex = bytes.size - stopSequence.size
                if (trimStartAndEndOnly) {
                    // Count the number of newlines at the end
                    while (stopIndex > 0 && (bytes[stopIndex - 1] == '\n'.code.toByte() || bytes[stopIndex - 1] == '\r'.code.toByte())) {
                        stopIndex--
                    }
                }
                bytes.copyOfRange(0, stopIndex)
            } else {
                ByteArray(0)
            }
        }
    }
    return if (saveContent) {
        val bytes = builder.toByteArray()
        var stopIndex = bytes.size - stopSequence.size
        if (trimStartAndEndOnly) {
            // Count the number of newlines at the end
            while (stopIndex > 0 && (bytes[stopIndex - 1] == '\n'.code.toByte() || bytes[stopIndex - 1] == '\r'.code.toByte())) {
                stopIndex--
            }
        }
        bytes.copyOfRange(0, stopIndex)
    } else {
        ByteArray(0)
    }
}

fun BufferedReader.readLinesUntil(
    stopSequence: String,
    saveContent: Boolean = true,
    trimLines: Boolean = true
): String {
    val builder = StringBuilder()

    while (true) {
        var line = readLine() ?: break
        if (trimLines) {
            line = line.trim()
        }

        if (saveContent) {
            builder.append(line)
        }

        if (line.trim().endsWith(stopSequence)) {
            return if (saveContent) {
                val idx = builder.indexOf(stopSequence)
                builder.substring(0, idx)
            } else {
                ""
            }
        }
    }

    return builder.toString()
}

fun BufferedReader.readUntil(
    stopSequence: String,
    saveContent: Boolean = true,
    trimLines: Boolean = true
): String {
    val builder = StringBuilder()

    forEachChar { char ->
        if (!trimLines || char != '\n') {
            builder.append(char)
        }

        if (!saveContent && builder.length > stopSequence.length) {
            builder.deleteAt(0)
        }

        if (builder.endsWith(stopSequence)) {
            return if (saveContent) builder.toString().dropLast(stopSequence.length) else ""
        }
    }
    return builder.toString()
}

inline fun BufferedReader.forEachChar(block: (char: Char) -> Unit) {
    var b: Int
    while (read().also { b = it } != -1) {
        block(b.toChar())
    }
}

inline fun InputStream.forEachByte(block: (byte: Int) -> Unit) {
    var b: Int
    while (read().also { b = it } != -1) {
        block(b)
    }
}

fun InputStream.readLine(): String {
    return readUntil { it == '\n' }
}

fun OutputStream.write(str: String) {
    writeAll(str.toByteArray())
}

fun OutputStream.writeAll(bytes: ByteArray) {
    write(bytes)
    flush()
}

fun OutputStream.write(byte: Byte) {
    write(byte.toInt())
    flush()
}