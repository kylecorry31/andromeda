package com.kylecorry.andromeda.preferences

import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class StringEnumPreferenceTest {

    private lateinit var preferences: Preferences
    private val prefName = "string_enum_preference_test"

    private val mapping = mapOf(
        "1" to TestEnum.One,
        "2" to TestEnum.Two,
        "3" to TestEnum.Three
    )

    @Before
    fun setup() {
        val ctx = InstrumentationRegistry.getInstrumentation().context
        preferences = Preferences(ctx)
        preferences.remove(prefName)
    }

    @After
    fun teardown() {
        preferences.remove(prefName)
    }

    @Test
    fun canGetDefaultValue() {
        val onePref by StringEnumPreference(preferences, prefName, mapping, TestEnum.One)
        val twoPref by StringEnumPreference(preferences, prefName, mapping, TestEnum.Two)

        assertEquals(TestEnum.One, onePref)
        assertEquals(TestEnum.Two, twoPref)
    }

    @Test
    fun canGetDefaultValueAndSave() {
        val pref by StringEnumPreference(preferences, prefName, mapping, TestEnum.One, saveDefault = true)

        assertEquals(TestEnum.One, pref)
        assertEquals("1", preferences.getString(prefName))
    }

    @Test
    fun canGetValue() {
        preferences.putString(prefName, "2")
        val pref by StringEnumPreference(preferences, prefName, mapping, TestEnum.One)
        assertEquals(TestEnum.Two, pref)

        preferences.putString(prefName, "3")
        assertEquals(TestEnum.Three, pref)
    }

    @Test
    fun canGetDefaultValueWhenNoMappingExists() {
        preferences.putString(prefName, "4")
        val pref by StringEnumPreference(preferences, prefName, mapping, TestEnum.One)

        assertEquals(TestEnum.One, pref)
    }

    @Test
    fun canSetValue() {
        assertEquals(null, preferences.getString(prefName))

        var pref by StringEnumPreference(preferences, prefName, mapping, TestEnum.One)
        pref = TestEnum.Two

        assertEquals(TestEnum.Two, pref)
        assertEquals("2", preferences.getString(prefName))

        pref = TestEnum.Three
        assertEquals(TestEnum.Three, pref)
        assertEquals("3", preferences.getString(prefName))
    }

    @Test
    fun doesNotSetValueWhenNoMappingExists() {
        preferences.putString(prefName, "2")
        var pref by StringEnumPreference(preferences, prefName, mapping, TestEnum.One)
        pref = TestEnum.Four

        assertEquals(TestEnum.Two, pref)
        assertEquals("2", preferences.getString(prefName))
    }

    private enum class TestEnum {
        One,
        Two,
        Three,
        Four
    }

}