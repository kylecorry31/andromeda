package com.kylecorry.andromeda.preferences

import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class BooleanPreferenceTest {

    private lateinit var preferences: DefaultSharedPreferences
    private val prefName = "boolean_preference_test"

    @Before
    fun setup() {
        val ctx = InstrumentationRegistry.getInstrumentation().context
        preferences = DefaultSharedPreferences(ctx)
        preferences.remove(prefName)
    }

    @After
    fun teardown() {
        preferences.remove(prefName)
    }

    @Test
    fun canGetDefaultValue() {
        val falsePref by BooleanPreference(preferences, prefName, false)
        val truePref by BooleanPreference(preferences, prefName, true)

        assertFalse(falsePref)
        assertTrue(truePref)
    }

    @Test
    fun canGetDefaultValueAndSave() {
        val pref by BooleanPreference(preferences, prefName, false, saveDefault = true)

        assertFalse(pref)
        assertEquals(false, preferences.getBoolean(prefName))
    }

    @Test
    fun canGetValue() {
        preferences.putBoolean(prefName, true)
        val pref by BooleanPreference(preferences, prefName, false)
        assertTrue(pref)

        preferences.putBoolean(prefName, false)
        assertFalse(pref)
    }

    @Test
    fun canSetValue() {
        assertEquals(null, preferences.getBoolean(prefName))

        var pref by BooleanPreference(preferences, prefName, false)
        pref = true

        assertTrue(pref)
        assertEquals(true, preferences.getBoolean(prefName))

        pref = false
        assertFalse(pref)
        assertEquals(false, preferences.getBoolean(prefName))
    }

}