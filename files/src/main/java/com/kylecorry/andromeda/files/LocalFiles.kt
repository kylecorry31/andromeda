package com.kylecorry.andromeda.files

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

object LocalFiles {

    fun getUri(context: Context, path: String, authority: String, create: Boolean = false): Uri {
        return FileProvider.getUriForFile(context, authority, getFile(context, path, create))
    }

    private fun create(context: Context, path: String, isDirectory: Boolean) {
        val file = File(context.filesDir, path)
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

    fun createFile(context: Context, path: String) {
        create(context, path, false)
    }

    fun createDirectory(context: Context, path: String) {
        create(context, path, true)
    }

    fun delete(context: Context, path: String, recursive: Boolean = false) {
        val file = File(context.filesDir, path)
        if (recursive) {
            file.deleteRecursively()
        } else {
            file.delete()
        }
    }

    private fun get(
        context: Context,
        path: String,
        isDirectory: Boolean,
        create: Boolean = true
    ): File {
        val file = File(context.filesDir, path)
        if (create && !file.exists()) {
            create(context, path, isDirectory)
        }
        return file
    }

    fun getDirectory(context: Context, path: String, create: Boolean = true): File {
        return get(context, path, true, create)
    }

    fun getFile(context: Context, path: String, create: Boolean = true): File {
        return get(context, path, false, create)
    }

    fun read(context: Context, path: String, create: Boolean = false): String {
        val file = getFile(context, path, create)
        return if (file.exists()) {
            file.readText()
        } else {
            ""
        }
    }

    fun write(context: Context, path: String, text: String, append: Boolean = false) {
        val file = getFile(context, path, true)
        if (append) {
            file.appendText(text)
        } else {
            file.writeText(text)
        }
    }


}