package com.kylecorry.andromeda.preferences

import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

internal class PreferencesTest {

    private lateinit var preferences: Preferences

    @Before
    fun setup(){
        val ctx = InstrumentationRegistry.getInstrumentation().context
        preferences = Preferences(ctx)
    }

    @Test
    fun cachesDouble(){
        val value = 3.141592654
        preferences.putDouble("test_double", value)

        assertEquals(preferences.getDouble("test_double"), value)
        assertTrue(preferences.contains("test_double"))

        preferences.remove("test_double")

        assertNull(preferences.getDouble("test_double"))
        assertFalse(preferences.contains("test_double"))
    }

    @Test
    fun cachesInt(){
        val value = 1
        val key = "test_int"
        preferences.putInt(key, value)

        assertEquals(preferences.getInt(key), value)
        assertTrue(preferences.contains(key))

        preferences.remove(key)

        assertNull(preferences.getInt(key))
        assertFalse(preferences.contains(key))
    }

    @Test
    fun cachesString(){
        val value = "test"
        val key = "test_string"
        preferences.putString(key, value)

        assertEquals(preferences.getString(key), value)
        assertTrue(preferences.contains(key))

        preferences.remove(key)

        assertNull(preferences.getString(key))
        assertFalse(preferences.contains(key))
    }

    @Test
    fun cachesFloat(){
        val value = 1.2f
        val key = "test_float"
        preferences.putFloat(key, value)

        assertEquals(preferences.getFloat(key), value)
        assertTrue(preferences.contains(key))

        preferences.remove(key)

        assertNull(preferences.getFloat(key))
        assertFalse(preferences.contains(key))
    }

    @Test
    fun cachesBoolean(){
        val value = true
        val key = "test_bool"
        preferences.putBoolean(key, value)

        assertEquals(preferences.getBoolean(key), value)
        assertTrue(preferences.contains(key))

        preferences.remove(key)

        assertNull(preferences.getBoolean(key))
        assertFalse(preferences.contains(key))
    }

    @Test
    fun cachesLong(){
        val value = 2L
        val key = "test_long"
        preferences.putLong(key, value)

        assertEquals(preferences.getLong(key), value)
        assertTrue(preferences.contains(key))

        preferences.remove(key)

        assertNull(preferences.getLong(key))
        assertFalse(preferences.contains(key))
    }

    @Test
    fun updatesCacheValue(){
        val key = "test_int"
        preferences.putInt(key, 1)

        assertEquals(preferences.getInt(key), 1)

        preferences.putInt(key, 2)

        assertEquals(preferences.getInt(key), 2)

        preferences.remove(key)
    }

}