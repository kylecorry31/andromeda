package com.kylecorry.andromeda.preferences

import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class IntEnumPreferenceTest {

    private lateinit var preferences: SharedPreferences
    private val prefName = "int_enum_preference_test"

    private val mapping = mapOf(
        1 to TestEnum.One,
        2 to TestEnum.Two,
        3 to TestEnum.Three
    )

    @Before
    fun setup() {
        val ctx = InstrumentationRegistry.getInstrumentation().context
        preferences = SharedPreferences(ctx)
        preferences.remove(prefName)
    }

    @After
    fun teardown() {
        preferences.remove(prefName)
        preferences.close()
    }

    @Test
    fun canGetDefaultValue() {
        val onePref by IntEnumPreference(preferences, prefName, mapping, TestEnum.One)
        val twoPref by IntEnumPreference(preferences, prefName, mapping, TestEnum.Two)

        assertEquals(TestEnum.One, onePref)
        assertEquals(TestEnum.Two, twoPref)
    }

    @Test
    fun canGetDefaultValueAndSave() {
        val pref by IntEnumPreference(preferences, prefName, mapping, TestEnum.One, saveDefault = true)

        assertEquals(TestEnum.One, pref)
        assertEquals(1, preferences.getInt(prefName))
    }

    @Test
    fun canGetValue() {
        preferences.putInt(prefName, 2)
        val pref by IntEnumPreference(preferences, prefName, mapping, TestEnum.One)
        assertEquals(TestEnum.Two, pref)

        preferences.putInt(prefName, 3)
        assertEquals(TestEnum.Three, pref)
    }

    @Test
    fun canGetDefaultValueWhenNoMappingExists() {
        preferences.putInt(prefName, 4)
        val pref by IntEnumPreference(preferences, prefName, mapping, TestEnum.One)

        assertEquals(TestEnum.One, pref)
    }

    @Test
    fun canSetValue() {
        assertEquals(null, preferences.getString(prefName))

        var pref by IntEnumPreference(preferences, prefName, mapping, TestEnum.One)
        pref = TestEnum.Two

        assertEquals(TestEnum.Two, pref)
        assertEquals(2, preferences.getInt(prefName))

        pref = TestEnum.Three
        assertEquals(TestEnum.Three, pref)
        assertEquals(3, preferences.getInt(prefName))
    }

    @Test
    fun doesNotSetValueWhenNoMappingExists() {
        preferences.putInt(prefName, 2)
        var pref by IntEnumPreference(preferences, prefName, mapping, TestEnum.One)
        pref = TestEnum.Four

        assertEquals(TestEnum.Two, pref)
        assertEquals(2, preferences.getInt(prefName))
    }

    private enum class TestEnum {
        One,
        Two,
        Three,
        Four
    }

}