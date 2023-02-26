package com.kylecorry.andromeda.preferences

import android.content.Context

data class PreferenceMigration(
    val fromVersion: Int,
    val toVersion: Int,
    val action: (context: Context, prefs: IPreferences) -> Unit
)