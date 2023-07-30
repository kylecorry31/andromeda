package com.kylecorry.andromeda.preferences

import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

internal class SynchronousDataStorePreferencesTest {

    private lateinit var preferences: SynchronousDataStorePreferences

    @Before
    fun setup() {
        val ctx = InstrumentationRegistry.getInstrumentation().context
        preferences = SynchronousDataStorePreferences(ctx, "settings")
        preferences.clear()
    }

    @After
    fun teardown() {
        preferences.clear()
        preferences.close()
    }

    @Test
    fun cachesDouble() {
        val value = 3.141592654
        preferences.putDouble("test_double", value)

        assertEquals(preferences.getDouble("test_double"), value)
        assertTrue(preferences.contains("test_double"))

        preferences.remove("test_double")

        assertNull(preferences.getDouble("test_double"))
        assertFalse(preferences.contains("test_double"))
    }

    @Test
    fun cachesInt() {
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
    fun cachesString() {
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
    fun cachesFloat() {
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
    fun cachesBoolean() {
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
    fun cachesLong() {
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
    fun cachesInstant() {
        val value = Instant.ofEpochMilli(1000)
        val key = "test_instant"
        preferences.putInstant(key, value)

        assertEquals(preferences.getInstant(key), value)
        assertTrue(preferences.contains(key))

        preferences.remove(key)

        assertNull(preferences.getInstant(key))
        assertFalse(preferences.contains(key))
    }

    @Test
    fun cachesLocalDate() {
        val value = LocalDate.of(2021, 1, 2)
        val key = "test_local_date"
        preferences.putLocalDate(key, value)

        assertEquals(preferences.getLocalDate(key), value)
        assertTrue(preferences.contains(key))

        preferences.remove(key)

        assertNull(preferences.getLocalDate(key))
        assertFalse(preferences.contains(key))
    }

    @Test
    fun updatesCacheValue() {
        val key = "test_int"
        preferences.putInt(key, 1)

        assertEquals(preferences.getInt(key), 1)

        preferences.putInt(key, 2)

        assertEquals(preferences.getInt(key), 2)

        preferences.remove(key)
    }

    @Test
    fun canMigrateDefaultSharedPreferencesAllKeys() {
        val ctx = InstrumentationRegistry.getInstrumentation().context
        val sharedPrefs = SharedPreferences(ctx)
        sharedPrefs.putInt("test_int", 1)
        sharedPrefs.putFloat("test_float", 1.2f)

        val dataStore = SynchronousDataStorePreferences(
            ctx,
            "settings_canMigrateDefaultSharedPreferencesAllKeys",
            migrations = listOf(
                SynchronousDataStorePreferences.getDefaultSharedPreferencesMigration(ctx)
            )
        )

        assertEquals(dataStore.getInt("test_int"), 1)
        assertEquals(dataStore.getFloat("test_float"), 1.2f)

        // Verify shared prefs are empty
        assertEquals(sharedPrefs.getInt("test_int"), null)
        assertEquals(sharedPrefs.getFloat("test_float"), null)

        // Cleanup
        dataStore.remove("test_int")
        dataStore.remove("test_float")
        dataStore.close()
        sharedPrefs.close()
        SynchronousDataStorePreferences.deleteDataStore(
            ctx,
            "settings_canMigrateDefaultSharedPreferencesAllKeys"
        )
    }

    @Test
    fun canMigrateDefaultSharedPreferencesPartialKeys() {
        val ctx = InstrumentationRegistry.getInstrumentation().context
        val sharedPrefs = SharedPreferences(ctx)
        sharedPrefs.putInt("test_int", 1)
        sharedPrefs.putFloat("test_float", 1.2f)

        val dataStore = SynchronousDataStorePreferences(
            ctx,
            "settings_canMigrateDefaultSharedPreferencesPartialKeys",
            migrations = listOf(
                SynchronousDataStorePreferences.getDefaultSharedPreferencesMigration(
                    ctx,
                    listOf("test_int")
                )
            )
        )

        assertEquals(dataStore.getInt("test_int"), 1)
        assertEquals(dataStore.getFloat("test_float"), null)

        // Verify shared prefs are empty
        assertEquals(sharedPrefs.getInt("test_int"), null)
        assertEquals(sharedPrefs.getFloat("test_float"), 1.2f)

        // Cleanup
        dataStore.remove("test_int")
        dataStore.remove("test_float")
        sharedPrefs.remove("test_float")
        dataStore.close()
        sharedPrefs.close()
        SynchronousDataStorePreferences.deleteDataStore(
            ctx,
            "settings_canMigrateDefaultSharedPreferencesPartialKeys"
        )
    }

    @Test
    fun canGetAll(){
        preferences.putInt("test_int", 1)
        preferences.putBoolean("test_bool", true)
        preferences.putString("test_string", "test")
        preferences.putFloat("test_float", 1.2f)
        preferences.putLong("test_long", 2L)

        val all = preferences.getAll()

        assertEquals(5, all.size)
        assertEquals(listOf(
            Preference("test_bool", PreferenceType.Boolean, true),
            Preference("test_float", PreferenceType.Float, 1.2f),
            Preference("test_int", PreferenceType.Int, 1),
            Preference("test_long", PreferenceType.Long, 2L),
            Preference("test_string", PreferenceType.String, "test")
        ), all.sortedBy { it.key })
    }

    @Test
    fun canPutAllWithClear(){
        preferences.putInt("test_int", 1)
        preferences.putString("test_string", "test")
        preferences.putLong("test_long", 2L)
        preferences.putLong("test_long2", 3L)

        preferences.putAll(listOf(
            Preference("test_bool", PreferenceType.Boolean, false),
            Preference("test_float", PreferenceType.Float, 1.3f),
            Preference("test_int", PreferenceType.Int, 2),
            Preference("test_long", PreferenceType.Long, 3L),
            Preference("test_string", PreferenceType.String, "test2")
        ), true)


        val all = preferences.getAll()

        assertEquals(5, all.size)
        assertEquals(listOf(
            Preference("test_bool", PreferenceType.Boolean, false),
            Preference("test_float", PreferenceType.Float, 1.3f),
            Preference("test_int", PreferenceType.Int, 2),
            Preference("test_long", PreferenceType.Long, 3L),
            Preference("test_string", PreferenceType.String, "test2")
        ), all.sortedBy { it.key })

        // Remove all the prefs
        preferences.clear()
    }

    @Test
    fun canPutAllWithoutClear(){
        preferences.putInt("test_int", 1)
        preferences.putString("test_string", "test")
        preferences.putLong("test_long", 2L)
        preferences.putLong("test_long2", 3L)

        preferences.putAll(listOf(
            Preference("test_bool", PreferenceType.Boolean, false),
            Preference("test_float", PreferenceType.Float, 1.3f),
            Preference("test_int", PreferenceType.Int, 2),
            Preference("test_long", PreferenceType.Long, 3L),
            Preference("test_string", PreferenceType.String, "test2")
        ), false)


        val all = preferences.getAll()

        assertEquals(6, all.size)
        assertEquals(listOf(
            Preference("test_bool", PreferenceType.Boolean, false),
            Preference("test_float", PreferenceType.Float, 1.3f),
            Preference("test_int", PreferenceType.Int, 2),
            Preference("test_long", PreferenceType.Long, 3L),
            Preference("test_long2", PreferenceType.Long, 3L),
            Preference("test_string", PreferenceType.String, "test2")
        ), all.sortedBy { it.key })
    }

}