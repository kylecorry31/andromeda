package com.kylecorry.andromeda.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.kylecorry.andromeda.core.system.Intents
import com.kylecorry.andromeda.permissions.PermissionRationale
import com.kylecorry.andromeda.permissions.Permissions
import com.kylecorry.andromeda.permissions.SpecialPermission
import com.kylecorry.luna.cache.Hooks
import kotlin.math.absoluteValue
import kotlin.random.Random

open class AndromedaActivity : AppCompatActivity(), IPermissionRequester {

    private var resultAction: ((successful: Boolean, data: Intent?) -> Unit)? = null
    private var permissionAction: (() -> Unit)? = null
    private lateinit var specialPermissionLauncher: SpecialPermissionLauncher

    private var resultRequestCode: Int? = null
    private var permissionRequestCode: Int? = null

    private var volumeAction: ((button: VolumeButton, isPressed: Boolean) -> Boolean) =
        { _, _ -> false }

    protected val hooks = Hooks()
    protected val lifecycleHookTrigger = LifecycleHookTrigger()

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        lifecycleHookTrigger.bind(this)
        specialPermissionLauncher = SpecialPermissionLauncher(this, this)
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleHookTrigger.unbind(this)
    }

    override fun requestPermissions(permissions: List<String>, action: () -> Unit) {
        val notGranted = permissions.filterNot { Permissions.hasPermission(this, it) }
        if (notGranted.isEmpty()) {
            action()
            return
        }
        val requestCode = Random.nextInt().absoluteValue
        permissionRequestCode = requestCode
        permissionAction = action
        ActivityCompat.requestPermissions(
            this,
            notGranted.toTypedArray(),
            requestCode
        )
    }

    override fun requestPermission(
        permission: SpecialPermission,
        rationale: PermissionRationale,
        action: () -> Unit
    ) {
        specialPermissionLauncher.requestPermission(permission, rationale, action)
    }

    fun getResult(intent: Intent, action: (successful: Boolean, data: Intent?) -> Unit) {
        val requestCode = Random.nextInt().absoluteValue
        resultRequestCode = requestCode
        resultAction = action
        startActivityForResult(intent, requestCode)
    }

    fun createFile(
        filename: String,
        type: String,
        message: String = filename,
        action: (uri: Uri?) -> Unit
    ) {
        val intent = Intents.createFile(filename, type, message)
        getResult(intent) { successful, data ->
            if (successful) {
                action(data?.data)
            } else {
                action(null)
            }
        }
    }

    fun createFile(
        filename: String,
        types: List<String>,
        message: String = filename,
        action: (uri: Uri?) -> Unit
    ) {
        val intent = Intents.createFile(filename, types, message)
        getResult(intent) { successful, data ->
            if (successful) {
                action(data?.data)
            } else {
                action(null)
            }
        }
    }

    fun pickFile(
        type: String,
        message: String,
        useSAF: Boolean = true,
        action: (uri: Uri?) -> Unit
    ) {
        val intent = Intents.pickFile(type, message, useSAF)
        getResult(intent) { successful, data ->
            if (successful) {
                action(data?.data)
            } else {
                action(null)
            }
        }
    }

    fun pickFile(
        types: List<String>,
        message: String,
        useSAF: Boolean = true,
        action: (uri: Uri?) -> Unit
    ) {
        val intent = Intents.pickFile(types, message, useSAF)
        getResult(intent) { successful, data ->
            if (successful) {
                action(data?.data)
            } else {
                action(null)
            }
        }
    }

    fun onVolumeButtonChange(action: ((button: VolumeButton, isPressed: Boolean) -> Boolean)?) {
        volumeAction = action ?: { _, _ -> false }
    }

    fun getFragment(): Fragment? {
        return supportFragmentManager.fragments.firstOrNull()?.childFragmentManager?.fragments?.firstOrNull()
    }

    fun setColorTheme(theme: ColorTheme, useDynamicColors: Boolean) {
        val mode = when (theme) {
            ColorTheme.Light -> AppCompatDelegate.MODE_NIGHT_NO
            ColorTheme.Dark -> AppCompatDelegate.MODE_NIGHT_YES
            ColorTheme.System -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(mode)
        if (useDynamicColors) {
            useDynamicColors()
        }
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == resultRequestCode) {
            resultAction?.invoke(resultCode == RESULT_OK, data)
            resultRequestCode = null
            resultAction = null
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == permissionRequestCode) {
            permissionAction?.invoke()
            permissionRequestCode = null
            permissionAction = null
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            return volumeAction(VolumeButton.Down, true)
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            return volumeAction(VolumeButton.Up, true)
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            return volumeAction(VolumeButton.Down, false)
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            return volumeAction(VolumeButton.Up, false)
        }
        return super.onKeyUp(keyCode, event)
    }

    protected fun effect(key: String, vararg values: Any?, action: () -> Unit) {
        hooks.effect(key, *values, action = action)
    }

    protected fun <T> memo(key: String, vararg values: Any?, value: () -> T): T {
        return hooks.memo(key, *values, value = value)
    }

}