package com.kylecorry.andromeda.core.io

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