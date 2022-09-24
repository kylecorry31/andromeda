package com.kylecorry.andromeda.files

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before

class ZipUtilsTest {

    private lateinit var ctx: Context
    private lateinit var fileSystem: CacheFileSystem

    @Before
    fun setup() {
        ctx = InstrumentationRegistry.getInstrumentation().context
        fileSystem = CacheFileSystem(ctx)
    }

    @Test
    fun canZip() {
        val zip = fileSystem.getFile("test.zip", true)
        fileSystem.write("test.txt", "testing")
        fileSystem.write("other.text", "other")

        assertTrue(zip.length() == 0L)

        ZipUtils.zip(zip, fileSystem.getFile("test.txt"), fileSystem.getFile("other.text"))

        assertTrue(zip.length() > 0L)

        fileSystem.delete("test.txt")
        fileSystem.delete("other.txt")
        fileSystem.delete("test.zip")
    }

    @Test
    fun canUnzip() {
        val zip = fileSystem.getFile("test.zip", true)
        fileSystem.write("test.txt", "testing")
        fileSystem.write("other.txt", "other")
        ZipUtils.zip(zip, fileSystem.getFile("test.txt"), fileSystem.getFile("other.txt"))

        ZipUtils.unzip(zip, fileSystem.getDirectory("test", true))

        assertEquals("testing", fileSystem.read("test/test.txt"))
        assertEquals("other", fileSystem.read("test/other.txt"))
        assertEquals(listOf("test.txt", "other.txt"), fileSystem.getDirectory("test").list()!!.toList())

        fileSystem.delete("test", true)
        fileSystem.delete("test.txt")
        fileSystem.delete("other.txt")
        fileSystem.delete("test.zip")
    }

    @Test
    fun canUnzipWithMaxCount() {
        val zip = fileSystem.getFile("test.zip", true)
        fileSystem.write("test.txt", "testing")
        fileSystem.write("other.txt", "other")
        ZipUtils.zip(zip, fileSystem.getFile("test.txt"), fileSystem.getFile("other.txt"))

        ZipUtils.unzip(zip, fileSystem.getDirectory("test", true), 1)

        assertEquals("testing", fileSystem.read("test/test.txt"))
        assertEquals(listOf("test.txt"), fileSystem.getDirectory("test").list()!!.toList())

        fileSystem.delete("test", true)
        fileSystem.delete("test.txt")
        fileSystem.delete("other.txt")
        fileSystem.delete("test.zip")
    }

    @Test
    fun canList() {
        val zip = fileSystem.getFile("test.zip", true)
        fileSystem.write("test.txt", "testing")
        fileSystem.write("other.txt", "other")
        ZipUtils.zip(zip, fileSystem.getFile("test.txt"), fileSystem.getFile("other.txt"))

        val files = ZipUtils.list(zip)

        assertEquals(listOf("test.txt", "other.txt"), files.map { it.file.path })
        assertEquals(listOf(false, false), files.map { it.isDirectory })

        fileSystem.delete("test", true)
        fileSystem.delete("test.txt")
        fileSystem.delete("other.txt")
        fileSystem.delete("test.zip")
    }

    @Test
    fun canListMaxLength() {
        val zip = fileSystem.getFile("test.zip", true)
        fileSystem.write("test.txt", "testing")
        fileSystem.write("other.txt", "other")
        ZipUtils.zip(zip, fileSystem.getFile("test.txt"), fileSystem.getFile("other.txt"))

        val files = ZipUtils.list(zip, 1)

        assertEquals(listOf("test.txt"), files.map { it.file.path })
        assertEquals(listOf(false), files.map { it.isDirectory })

        fileSystem.delete("test", true)
        fileSystem.delete("test.txt")
        fileSystem.delete("other.txt")
        fileSystem.delete("test.zip")
    }

}