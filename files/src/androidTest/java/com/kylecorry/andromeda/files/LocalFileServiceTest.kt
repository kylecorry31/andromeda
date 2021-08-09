package com.kylecorry.andromeda.files

import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before

class LocalFileServiceTest {

    private lateinit var service: LocalFileService

    @Before
    fun setup(){
        val ctx = InstrumentationRegistry.getInstrumentation().context
        service = LocalFileService(ctx)
    }

    @Test
    fun createFile() {
        service.createFile("something/other.txt")
        val file = service.getFile("something/other.txt", false)
        assertTrue(file.exists())
        assertFalse(file.isDirectory)
        service.delete("something", true)
    }

    @Test
    fun createDirectory() {
        service.createDirectory("something/other")
        val file = service.getDirectory("something/other", false)
        assertTrue(file.exists())
        assertTrue(file.isDirectory)
        service.delete("something", true)
    }

    @Test
    fun delete() {
        val file = service.getFile("test.txt", true)
        service.delete("test.txt")
        assertFalse(file.exists())
    }

    @Test
    fun deleteRecursive() {
        val file = service.getFile("something/other", true)
        val parent = service.getFile("something", false)
        service.delete("something", true)
        assertFalse(parent.exists())
        assertFalse(file.exists())
    }

    @Test
    fun getDirectoryNoCreate() {
        val file = service.getDirectory("test", false)
        assertEquals("${getBasePath()}/test", file.path)
        assertFalse(file.exists())
    }

    @Test
    fun getDirectoryCreate() {
        val file = service.getDirectory("test", true)
        assertEquals("${getBasePath()}/test", file.path)
        assertTrue(file.isDirectory)
        assertTrue(file.exists())
        service.delete("test")
    }

    @Test
    fun getDirectoryCreatePaths() {
        val file = service.getDirectory("test/other", true)
        assertEquals("${getBasePath()}/test/other", file.path)
        assertTrue(file.isDirectory)
        assertTrue(file.exists())
        service.delete("test", true)
    }

    @Test
    fun getFileNoCreate() {
        val file = service.getFile("test.txt", false)
        assertEquals("${getBasePath()}/test.txt", file.path)
        assertFalse(file.exists())
    }

    @Test
    fun getFileCreate() {
        val file = service.getFile("test.txt", true)
        assertEquals("${getBasePath()}/test.txt", file.path)
        assertTrue(file.exists())

        service.delete("test.txt")
    }

    @Test
    fun getFileCreatePath() {
        val file = service.getFile("exports/test.txt", true)
        assertEquals("${getBasePath()}/exports/test.txt", file.path)
        assertTrue(file.exists())

        service.delete("exports/test.txt", true)
    }

    @Test
    fun readNonExistentFile() {
        val text = service.read("test.txt", false)
        assertEquals("", text)
        assertFalse(service.getFile("test.txt", false).exists())
    }

    @Test
    fun readCreateNonExistentFile() {
        val text = service.read("test.txt", true)
        assertEquals("", text)
        assertTrue(service.getFile("test.txt", false).exists())
        service.delete("test.txt")
    }

    @Test
    fun writeThenReadFile() {
        service.write("test.txt", "testing")
        val text = service.read("test.txt", false)
        assertEquals("testing", text)

        service.delete("test.txt")
    }

    @Test
    fun appendToFile() {
        service.write("test.txt", "testing")
        service.write("test.txt", "123", true)
        val text = service.read("test.txt", false)
        assertEquals("testing123", text)

        service.delete("test.txt")
    }

    @Test
    fun overwriteFile() {
        service.write("test.txt", "testing")
        service.write("test.txt", "123")
        val text = service.read("test.txt", false)
        assertEquals("123", text)

        service.delete("test.txt")
    }

    private fun getBasePath(): String {
        return InstrumentationRegistry.getInstrumentation().context.filesDir.path
    }

}