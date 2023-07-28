package com.kylecorry.andromeda.files

import com.kylecorry.andromeda.core.tryOrNothing
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object ZipUtils {

    /**
     * Zip files to a stream - does not support zipping directories yet
     */
    fun zip(toFile: File, vararg files: File) {
        zip(FileOutputStream(toFile), files = files)
    }

    /**
     * Zip files to a stream
     */
    fun zip(toStream: OutputStream, vararg files: File) {
        val saver = FileSaver(false)
        val zip = ZipOutputStream(toStream)
        zip.use {
            files.forEach { file ->
                addFileToZip(zip, file, saver)
            }
        }
    }

    private fun addFileToZip(
        zip: ZipOutputStream,
        file: File,
        saver: FileSaver,
        rootPath: String = ""
    ) {
        if (file.isDirectory) {
            file.listFiles()?.forEach {
                addFileToZip(zip, it, saver, rootPath + file.name + "/")
            }
        } else {
            val entry = ZipEntry(rootPath + file.name)
            zip.putNextEntry(entry)
            saver.save(file, zip)
        }
    }

    fun unzip(fromFile: File, toDirectory: File, maxCount: Int = Int.MAX_VALUE) {
        unzip(FileInputStream(fromFile), toDirectory, maxCount)
    }

    fun unzip(fromStream: InputStream, toDirectory: File, maxCount: Int = Int.MAX_VALUE) {
        val zip = ZipInputStream(fromStream)
        var count = 0
        val saver = FileSaver(false)
        zip.forEach {
            val dest = File(toDirectory, it.name)
            if (it.isDirectory) {
                if (!dest.exists()) {
                    dest.mkdirs()
                }
            } else {
                val parent = dest.parentFile
                if (parent?.exists() == false) {
                    parent.mkdirs()
                }
                if (!dest.exists()) {
                    dest.createNewFile()
                }

                saver.save(zip, dest)
            }
            count++
            count < maxCount
        }
    }

    fun list(zipFile: File, maxCount: Int = Int.MAX_VALUE): List<ZipFile> {
        return list(FileInputStream(zipFile), maxCount)
    }

    fun list(zipStream: InputStream, maxCount: Int = Int.MAX_VALUE): List<ZipFile> {
        val zip = ZipInputStream(zipStream)
        val files = mutableListOf<ZipFile>()
        zip.forEach {
            files.add(ZipFile(File(it.name), it.isDirectory))
            files.size < maxCount
        }
        return files
    }

    private fun ZipInputStream.forEach(fn: (entry: ZipEntry) -> Boolean) {
        use {
            var entry = nextEntry
            var shouldContinue = true
            while (entry != null && shouldContinue) {
                shouldContinue = fn(entry)
                entry = nextEntry
            }
            tryOrNothing {
                closeEntry()
            }
        }
    }

}