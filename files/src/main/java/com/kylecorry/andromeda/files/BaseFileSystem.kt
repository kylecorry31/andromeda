package com.kylecorry.andromeda.files

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import java.io.*

open class BaseFileSystem(private val context: Context, private val basePath: String = "") :
    IFileSystem {

    private val baseDirectory = File(basePath).canonicalFile

    override fun getUri(path: String, authority: String, create: Boolean): Uri {
        return FileProvider.getUriForFile(context, authority, getFile(path, create))
    }

    override fun getUri(path: String, create: Boolean): Uri {
        return getFile(path, create).toUri()
    }

    private fun create(path: String, isDirectory: Boolean) {
        create(resolve(path), isDirectory)
    }

    private fun create(file: File, isDirectory: Boolean) {
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
        val file = resolve(path)
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
        val file = resolve(path)
        if (create && !file.exists()) {
            create(file, isDirectory)
        }
        return file
    }

    private fun resolve(path: String): File {
        val file = File(baseDirectory, path).canonicalFile
        require(file == baseDirectory || file.path.startsWith("${baseDirectory.path}${File.separator}")) {
            "Path must be within ${baseDirectory.path}: $path"
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
        return resolve(file.path).relativeTo(baseDirectory).path
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

    override fun list(path: String): List<File> {
        return getDirectory(path, false).listFiles()?.toList() ?: emptyList()
    }
}
