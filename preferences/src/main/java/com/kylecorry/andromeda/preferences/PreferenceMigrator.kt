package com.kylecorry.andromeda.preferences

import android.content.Context

class PreferenceMigrator(
    private val context: Context,
    private val versionKey: String,
    private val preferences: IPreferences = SharedPreferences(context)
) {

    private val lock = Object()

    fun migrate(toVersion: Int, migrations: List<PreferenceMigration>) {
        synchronized(lock) {
            var currentVersion = preferences.getInt(versionKey) ?: 0

            while (currentVersion < toVersion) {
                val current = currentVersion
                val next = currentVersion + 1
                val migration =
                    migrations.find { it.fromVersion == current && it.toVersion == next }
                migration?.action?.invoke(context, preferences)
                currentVersion++
                preferences.putInt(versionKey, currentVersion)
            }
        }
    }
}