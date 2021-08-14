package com.kylecorry.andromeda.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.kylecorry.andromeda.core.system.IntentUtils
import com.kylecorry.andromeda.core.time.Timer
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import java.time.Duration

open class AndromedaFragment : Fragment() {

    protected var cancelLifecycleScopeOnPause: Boolean = false
    protected var hasUpdates: Boolean = true

    private var resultLauncher: ActivityResultLauncher<Intent>? = null
    private var permissionLauncher: ActivityResultLauncher<Array<String>>? = null

    private var resultAction: ((successful: Boolean, data: Intent?) -> Unit)? = null
    private var permissionAction: (() -> Unit)? = null

    private val updateTimer = Timer {
        onUpdateWrapper()
    }

    private var updateInterval: Long? = null

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

    override fun onDestroy() {
        super.onDestroy()
        resultLauncher?.unregister()
        permissionLauncher?.unregister()
    }

    override fun onResume() {
        super.onResume()
        val updateInterval = this.updateInterval
        if (updateInterval != null) {
            scheduleUpdates(updateInterval)
        }
    }

    override fun onPause() {
        super.onPause()
        updateTimer.stop()
        if (cancelLifecycleScopeOnPause && lifecycleScope.isActive) {
            lifecycleScope.cancel()
        }
    }

    protected fun scheduleUpdates(interval: Duration) {
        scheduleUpdates(interval.toMillis())
    }

    protected fun scheduleUpdates(interval: Long) {
        updateInterval = interval
        updateTimer.interval(interval)
    }

    protected fun cancelUpdates() {
        updateInterval = null
        updateTimer.stop()
    }

    private fun onUpdateWrapper() {
        if (canUpdate()) {
            onUpdate()
        }
    }

    open fun canUpdate(): Boolean {
        return context != null && hasUpdates
    }

    open fun onUpdate() {
        // Do nothing by default
    }

    protected fun requestPermissions(permissions: List<String>, action: () -> Unit) {
        permissionAction = action
        permissionLauncher?.launch(permissions.toTypedArray())
    }

    protected fun getResult(intent: Intent, action: (successful: Boolean, data: Intent?) -> Unit) {
        resultAction = action
        resultLauncher?.launch(intent)
    }

    protected fun createFile(filename: String, type: String, action: (uri: Uri?) -> Unit) {
        val intent = IntentUtils.createFile(filename, type)
        getResult(intent) { successful, data ->
            if (successful) {
                action(data?.data)
            } else {
                action(null)
            }
        }
    }

    protected fun pickFile(type: String, message: String, action: (uri: Uri?) -> Unit) {
        val intent = IntentUtils.pickFile(type, message)
        getResult(intent) { successful, data ->
            if (successful) {
                action(data?.data)
            } else {
                action(null)
            }
        }
    }

    protected fun pickFile(types: List<String>, message: String, action: (uri: Uri?) -> Unit) {
        val intent = IntentUtils.pickFile(types, message)
        getResult(intent) { successful, data ->
            if (successful) {
                action(data?.data)
            } else {
                action(null)
            }
        }
    }

    companion object {
        const val INTERVAL_60_FPS = 17L
        const val INTERVAL_30_FPS = 33L
        const val INTERVAL_1_FPS = 1000L
    }

}