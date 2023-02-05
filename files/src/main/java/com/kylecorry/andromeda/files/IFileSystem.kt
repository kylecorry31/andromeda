package com.kylecorry.andromeda.files

import android.net.Uri
import java.io.File
import java.io.InputStream
import java.io.OutputStream

interface IFileSystem {
    fun getUri(path: String, authority: String, create: Boolean = false): Uri
    fun getUri(path: String, create: Boolean = false): Uri
    fun createFile(path: String)
    fun createDirectory(path: String)
    fun delete(path: String, recursive: Boolean = false)
    fun getDirectory(path: String, create: Boolean = true): File
    fun getFile(path: String, create: Boolean = true): File
    fun read(path: String, create: Boolean = false): String
    fun write(path: String, text: String, append: Boolean = false)
    fun getRelativePath(file: File): String
    fun inputStream(path: String, create: Boolean = true): InputStream
    fun outputStream(path: String): OutputStream
    fun getMimeType(path: String): String?
    fun list(path: String): List<File>
}