package com.kylecorry.andromeda.fragments

import android.app.Activity
import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import androidx.core.graphics.drawable.DrawableCompat
import androidx.preference.*
import com.kylecorry.andromeda.core.system.Intents

abstract class AndromedaPreferenceFragment : PreferenceFragmentCompat() {

    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>

    private var resultAction: ((successful: Boolean, data: Intent?) -> Unit)? = null
    private var permissionAction: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        resultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            val successful = it.resultCode == Activity.RESULT_OK
            resultAction?.invoke(successful, it.data)
        }
        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {
            permissionAction?.invoke()
        }
    }

    protected fun requestPermissions(permissions: List<String>, action: () -> Unit) {
        permissionAction = action
        permissionLauncher.launch(permissions.toTypedArray())
    }

    protected fun getResult(intent: Intent, action: (successful: Boolean, data: Intent?) -> Unit) {
        resultAction = action
        resultLauncher.launch(intent)
    }

    protected fun createFile(filename: String, type: String, action: (uri: Uri?) -> Unit) {
        val intent = Intents.createFile(filename, type)
        getResult(intent) { successful, data ->
            if (successful) {
                action(data?.data)
            } else {
                action(null)
            }
        }
    }

    protected fun pickFile(type: String, message: String, action: (uri: Uri?) -> Unit) {
        val intent = Intents.pickFile(type, message)
        getResult(intent) { successful, data ->
            if (successful) {
                action(data?.data)
            } else {
                action(null)
            }
        }
    }

    protected fun pickFile(types: List<String>, message: String, action: (uri: Uri?) -> Unit) {
        val intent = Intents.pickFile(types, message)
        getResult(intent) { successful, data ->
            if (successful) {
                action(data?.data)
            } else {
                action(null)
            }
        }
    }

    protected fun switch(@StringRes id: Int): SwitchPreferenceCompat? {
        return preferenceManager.findPreference(getString(id))
    }

    protected fun list(@StringRes id: Int): ListPreference? {
        return preferenceManager.findPreference(getString(id))
    }

    protected fun seekBar(@StringRes id: Int): SeekBarPreference? {
        return preferenceManager.findPreference(getString(id))
    }

    protected fun editText(@StringRes id: Int): EditTextPreference? {
        return preferenceManager.findPreference(getString(id))
    }

    protected fun preference(@StringRes id: Int): Preference? {
        return preferenceManager.findPreference(getString(id))
    }

    protected fun onClick(pref: Preference?, action: (preference: Preference) -> Unit) {
        pref?.setOnPreferenceClickListener {
            action(it)
            true
        }
    }

    protected fun setIconColor(@ColorInt color: Int?) {
        setIconColor(preferenceScreen, color)
    }

    protected fun setIconColor(preference: Preference, @ColorInt color: Int?) {
        if (preference is PreferenceGroup) {
            for (i in 0 until preference.preferenceCount) {
                setIconColor(preference.getPreference(i), color)
            }
        } else {
            if (preference.icon != null && color != null) {
                preference.icon.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
            } else if (preference.icon != null) {
                DrawableCompat.clearColorFilter(preference.icon)
            }
        }
    }

}