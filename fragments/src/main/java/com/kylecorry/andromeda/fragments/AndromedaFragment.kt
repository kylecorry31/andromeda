package com.kylecorry.andromeda.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.kylecorry.andromeda.core.system.Intents
import com.kylecorry.andromeda.core.time.Throttle
import com.kylecorry.andromeda.core.time.Timer
import com.kylecorry.andromeda.permissions.PermissionRationale
import com.kylecorry.andromeda.permissions.Permissions
import com.kylecorry.andromeda.permissions.SpecialPermission
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.time.Duration
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

open class AndromedaFragment : Fragment(), IPermissionRequester {

    protected var hasUpdates: Boolean = true

    private var resultLauncher: ActivityResultLauncher<Intent>? = null
    private var permissionLauncher: ActivityResultLauncher<Array<String>>? = null
    private lateinit var specialPermissionLauncher: SpecialPermissionLauncher

    private var resultAction: ((successful: Boolean, data: Intent?) -> Unit)? = null
    private var permissionAction: (() -> Unit)? = null

    private val updateTimer = Timer {
        onUpdateWrapper()
    }

    private var updateInterval: Long? = null
    private var throttle: Throttle? = null

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
        specialPermissionLauncher = SpecialPermissionLauncher(requireContext(), this)
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
    }

    protected fun scheduleUpdates(interval: Duration) {
        scheduleUpdates(interval.toMillis())
    }

    protected fun scheduleUpdates(interval: Long) {
        updateInterval = interval
        updateTimer.interval(interval)
    }

    protected fun throttleUpdates(maxUpdateInterval: Duration) {
        throttleUpdates(maxUpdateInterval.toMillis())
    }

    protected fun throttleUpdates(maxUpdateInterval: Long) {
        throttle = Throttle(maxUpdateInterval)
    }

    protected fun unthrottleUpdates() {
        throttle = null
    }

    protected fun cancelUpdates() {
        updateInterval = null
        updateTimer.stop()
    }

    private fun onUpdateWrapper() {
        // TODO: If throttled, schedule an update when it expires
        if (canUpdate()) {
            onUpdate()
        }
    }

    open fun canUpdate(): Boolean {
        return context != null && hasUpdates && throttle?.isThrottled() != true
    }

    open fun onUpdate() {
        // Do nothing by default
    }

    protected fun runInBackground(
        context: CoroutineContext = EmptyCoroutineContext,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> Unit
    ): Job {
        return lifecycleScope.launch(context, start, block)
    }

    override fun requestPermissions(permissions: List<String>, action: () -> Unit) {
        val notGranted = permissions.filterNot { Permissions.hasPermission(requireContext(), it) }
        if (notGranted.isEmpty()) {
            action()
            return
        }
        permissionAction = action
        permissionLauncher?.launch(notGranted.toTypedArray())
    }

    override fun requestPermission(
        permission: SpecialPermission,
        rationale: PermissionRationale,
        action: () -> Unit
    ) {
        specialPermissionLauncher.requestPermission(permission, rationale, action)
    }

    fun getResult(intent: Intent, action: (successful: Boolean, data: Intent?) -> Unit) {
        resultAction = action
        resultLauncher?.launch(intent)
    }

    fun createFile(filename: String, type: String, message: String = filename, action: (uri: Uri?) -> Unit) {
        val intent = Intents.createFile(filename, type, message)
        getResult(intent) { successful, data ->
            if (successful) {
                action(data?.data)
            } else {
                action(null)
            }
        }
    }

    fun createFile(filename: String, types: List<String>, message: String = filename, action: (uri: Uri?) -> Unit) {
        val intent = Intents.createFile(filename, types, message)
        getResult(intent) { successful, data ->
            if (successful) {
                action(data?.data)
            } else {
                action(null)
            }
        }
    }

    fun pickFile(type: String, message: String, useSAF: Boolean = true, action: (uri: Uri?) -> Unit) {
        val intent = Intents.pickFile(type, message, useSAF)
        getResult(intent) { successful, data ->
            if (successful) {
                action(data?.data)
            } else {
                action(null)
            }
        }
    }

    fun pickFile(types: List<String>, message: String, useSAF: Boolean = true, action: (uri: Uri?) -> Unit) {
        val intent = Intents.pickFile(types, message, useSAF)
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