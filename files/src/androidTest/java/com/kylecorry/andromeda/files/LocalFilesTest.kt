package com.kylecorry.andromeda.files

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before

class LocalFilesTest {

    private lateinit var ctx: Context

    @Before
    fun setup(){
        ctx = InstrumentationRegistry.getInstrumentation().context
    }

    @Test
    fun createFile() {
        LocalFiles.createFile(ctx, "something/other.txt")
        val file = LocalFiles.getFile(ctx, "something/other.txt", false)
        assertTrue(file.exists())
        assertFalse(file.isDirectory)
        LocalFiles.delete(ctx, "something", true)
    }

    @Test
    fun createDirectory() {
        LocalFiles.createDirectory(ctx, "something/other")
        val file = LocalFiles.getDirectory(ctx, "something/other", false)
        assertTrue(file.exists())
        assertTrue(file.isDirectory)
        LocalFiles.delete(ctx, "something", true)
    }

    @Test
    fun delete() {
        val file = LocalFiles.getFile(ctx, "test.txt", true)
        LocalFiles.delete(ctx, "test.txt")
        assertFalse(file.exists())
    }

    @Test
    fun deleteRecursive() {
        val file = LocalFiles.getFile(ctx, "something/other", true)
        val parent = LocalFiles.getFile(ctx, "something", false)
        LocalFiles.delete(ctx, "something", true)
        assertFalse(parent.exists())
        assertFalse(file.exists())
    }

    @Test
    fun getDirectoryNoCreate() {
        val file = LocalFiles.getDirectory(ctx, "test", false)
        assertEquals("${getBasePath()}/test", file.path)
        assertFalse(file.exists())
    }

    @Test
    fun getDirectoryCreate() {
        val file = LocalFiles.getDirectory(ctx, "test", true)
        assertEquals("${getBasePath()}/test", file.path)
        assertTrue(file.isDirectory)
        assertTrue(file.exists())
        LocalFiles.delete(ctx, "test")
    }

    @Test
    fun getDirectoryCreatePaths() {
        val file = LocalFiles.getDirectory(ctx, "test/other", true)
        assertEquals("${getBasePath()}/test/other", file.path)
        assertTrue(file.isDirectory)
        assertTrue(file.exists())
        LocalFiles.delete(ctx, "test", true)
    }

    @Test
    fun getFileNoCreate() {
        val file = LocalFiles.getFile(ctx, "test.txt", false)
        assertEquals("${getBasePath()}/test.txt", file.path)
        assertFalse(file.exists())
    }

    @Test
    fun getFileCreate() {
        val file = LocalFiles.getFile(ctx, "test.txt", true)
        assertEquals("${getBasePath()}/test.txt", file.path)
        assertTrue(file.exists())

        LocalFiles.delete(ctx, "test.txt")
    }

    @Test
    fun getFileCreatePath() {
        val file = LocalFiles.getFile(ctx, "exports/test.txt", true)
        assertEquals("${getBasePath()}/exports/test.txt", file.path)
        assertTrue(file.exists())

        LocalFiles.delete(ctx, "exports/test.txt", true)
    }

    @Test
    fun readNonExistentFile() {
        val text = LocalFiles.read(ctx, "test.txt", false)
        assertEquals("", text)
        assertFalse(LocalFiles.getFile(ctx, "test.txt", false).exists())
    }

    @Test
    fun readCreateNonExistentFile() {
        val text = LocalFiles.read(ctx, "test.txt", true)
        assertEquals("", text)
        assertTrue(LocalFiles.getFile(ctx, "test.txt", false).exists())
        LocalFiles.delete(ctx, "test.txt")
    }

    @Test
    fun writeThenReadFile() {
        LocalFiles.write(ctx, "test.txt", "testing")
        val text = LocalFiles.read(ctx, "test.txt", false)
        assertEquals("testing", text)

        LocalFiles.delete(ctx, "test.txt")
    }

    @Test
    fun appendToFile() {
        LocalFiles.write(ctx, "test.txt", "testing")
        LocalFiles.write(ctx, "test.txt", "123", true)
        val text = LocalFiles.read(ctx, "test.txt", false)
        assertEquals("testing123", text)

        LocalFiles.delete(ctx, "test.txt")
    }

    @Test
    fun overwriteFile() {
        LocalFiles.write(ctx, "test.txt", "testing")
        LocalFiles.write(ctx, "test.txt", "123")
        val text = LocalFiles.read(ctx, "test.txt", false)
        assertEquals("123", text)

        LocalFiles.delete(ctx, "test.txt")
    }

    private fun getBasePath(): String {
        return InstrumentationRegistry.getInstrumentation().context.filesDir.path
    }

}