package com.kylecorry.andromeda.core.io

import java.io.BufferedReader
import java.io.InputStream
import java.io.OutputStream
import java.nio.Buffer

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
            return builder.toString().dropLast(stopSequence.length)
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