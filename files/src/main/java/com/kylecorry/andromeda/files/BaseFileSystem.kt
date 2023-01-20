package com.kylecorry.andromeda.files

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import java.io.*

open class BaseFileSystem(private val context: Context, private val basePath: String = "") :
    IFileSystem {

    override fun getUri(path: String, authority: String, create: Boolean): Uri {
        return FileProvider.getUriForFile(context, authority, getFile(path, create))
    }

    override fun getUri(path: String, create: Boolean): Uri {
        return getFile(path, create).toUri()
    }

    private fun create(path: String, isDirectory: Boolean) {
        val file = File(basePath, path)
        if (file.exists()) {
            return
        }

        if (isDirectory) {
            file.mkdirs()
        } else {
            val parent = file.parentFile
            if (parent?.exists() == false) {
                parent.mkdirs()
            }
            file.createNewFile()
        }
    }

    override fun createFile(path: String) {
        create(path, false)
    }

    override fun createDirectory(path: String) {
        create(path, true)
    }

    override fun delete(path: String, recursive: Boolean) {
        val file = File(basePath, path)
        if (recursive) {
            file.deleteRecursively()
        } else {
            file.delete()
        }
    }

    private fun get(
        path: String,
        isDirectory: Boolean,
        create: Boolean = true
    ): File {
        val file = File(basePath, path)
        if (create && !file.exists()) {
            create(path, isDirectory)
        }
        return file
    }

    override fun getDirectory(path: String, create: Boolean): File {
        return get(path, true, create)
    }

    override fun getFile(path: String, create: Boolean): File {
        return get(path, false, create)
    }

    override fun read(path: String, create: Boolean): String {
        val file = getFile(path, create)
        return if (file.exists()) {
            file.readText()
        } else {
            ""
        }
    }

    override fun write(path: String, text: String, append: Boolean) {
        val file = getFile(path, true)
        if (append) {
            file.appendText(text)
        } else {
            file.writeText(text)
        }
    }

    override fun getRelativePath(file: File): String {
        return file.path.substringAfter("$basePath/")
    }

    override fun inputStream(path: String, create: Boolean): InputStream {
        return FileInputStream(getFile(path, create))
    }

    override fun outputStream(path: String): OutputStream {
        return FileOutputStream(getFile(path, true))
    }

    override fun getMimeType(path: String): String? {
        val file = getFile(path, false)
        return MimeType.toMimeType(file.extension)
    }
}