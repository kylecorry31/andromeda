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
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceGroup
import androidx.preference.SeekBarPreference
import androidx.preference.SwitchPreferenceCompat
import com.kylecorry.andromeda.core.system.IntentResultRetriever
import com.kylecorry.andromeda.core.system.Intents
import com.kylecorry.andromeda.permissions.PermissionRationale
import com.kylecorry.andromeda.permissions.SpecialPermission
import com.kylecorry.luna.hooks.Hooks

abstract class AndromedaPreferenceFragment : PreferenceFragmentCompat(), IPermissionRequester,
    IntentResultRetriever {

    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var specialPermissionLauncher: SpecialPermissionLauncher

    private var resultAction: ((successful: Boolean, data: Intent?) -> Unit)? = null
    private var permissionAction: (() -> Unit)? = null
    protected val hooks = Hooks()
    protected val lifecycleHookTrigger = LifecycleHookTrigger()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleHookTrigger.bind(this)
        scheduleStateUpdates(hooks)
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
        specialPermissionLauncher = SpecialPermissionLauncher(requireContext(), this)
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleHookTrigger.unbind(this)
    }

    override fun requestPermission(
        permission: SpecialPermission,
        rationale: PermissionRationale,
        action: () -> Unit
    ) {
        specialPermissionLauncher.requestPermission(permission, rationale, action)
    }

    override fun requestPermissions(permissions: List<String>, action: () -> Unit) {
        permissionAction = action
        permissionLauncher.launch(permissions.toTypedArray())
    }

    override fun getResult(intent: Intent, action: (successful: Boolean, data: Intent?) -> Unit) {
        resultAction = action
        resultLauncher.launch(intent)
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

    protected fun onChange(pref: Preference?, action: (value: Any) -> Unit) {
        pref?.setOnPreferenceChangeListener { _, value ->
            action(value)
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
            val icon = preference.icon
            if (icon != null && color != null) {
                icon.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
            } else if (icon != null) {
                DrawableCompat.clearColorFilter(icon)
            }
        }
    }

    protected fun effect(key: String, vararg values: Any?, action: () -> Unit) {
        hooks.effect(key, *values, action = action)
    }

    protected fun <T> memo(key: String, vararg values: Any?, value: () -> T): T {
        return hooks.memo(key, *values, value = value)
    }

    protected fun resetHooks(
        exceptEffects: List<String> = emptyList(),
        exceptMemos: List<String> = emptyList()
    ) {
        hooks.resetEffects(except = exceptEffects)
        hooks.resetMemos(except = exceptMemos)
    }

}