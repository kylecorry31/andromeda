package com.kylecorry.andromeda.files

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

class LocalFileService(private val context: Context) {

    fun getUri(path: String, authority: String, create: Boolean = false): Uri {
        return FileProvider.getUriForFile(context, authority, getFile(path, create))
    }

    private fun create(path: String, isDirectory: Boolean){
        val file = File(context.filesDir, path)
        if (file.exists()){
            return
        }

        if (isDirectory){
            file.mkdirs()
        } else {
            val parent = file.parentFile
            if (parent?.exists() == false){
                parent.mkdirs()
            }
            file.createNewFile()
        }
    }

    fun createFile(path: String){
        create(path, false)
    }

    fun createDirectory(path: String){
        create(path, true)
    }

    fun delete(path: String, recursive: Boolean = false){
        val file = File(context.filesDir, path)
        if (recursive){
            file.deleteRecursively()
        } else {
            file.delete()
        }
    }

    private fun get(path: String, isDirectory: Boolean, create: Boolean = true): File {
        val file = File(context.filesDir, path)
        if (create && !file.exists()){
            create(path, isDirectory)
        }
        return file
    }

    fun getDirectory(path: String, create: Boolean = true): File {
        return get(path, true, create)
    }

    fun getFile(path: String, create: Boolean = true): File {
        return get(path, false, create)
    }

    fun read(path: String, create: Boolean = false): String {
        val file = getFile(path, create)
        return if (file.exists()) {
            file.readText()
        } else {
            ""
        }
    }

    fun write(path: String, text: String, append: Boolean = false) {
        val file = getFile(path, true)
        if (append){
            file.appendText(text)
        } else {
            file.writeText(text)
        }
    }


}