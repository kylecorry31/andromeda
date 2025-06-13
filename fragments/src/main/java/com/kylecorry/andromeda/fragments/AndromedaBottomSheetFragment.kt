package com.kylecorry.andromeda.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.IdRes
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kylecorry.andromeda.core.system.IntentResultRetriever
import com.kylecorry.andromeda.core.ui.ReactiveComponent
import com.kylecorry.andromeda.core.ui.useCallback
import com.kylecorry.andromeda.permissions.PermissionRationale
import com.kylecorry.andromeda.permissions.Permissions
import com.kylecorry.andromeda.permissions.SpecialPermission
import com.kylecorry.luna.hooks.Hooks
import com.kylecorry.luna.hooks.State

open class AndromedaBottomSheetFragment : BottomSheetDialogFragment(), IPermissionRequester,
    IntentResultRetriever,
    ReactiveComponent {

    private var resultLauncher: ActivityResultLauncher<Intent>? = null
    private var permissionLauncher: ActivityResultLauncher<Array<String>>? = null
    private lateinit var specialPermissionLauncher: SpecialPermissionLauncher

    private var resultAction: ((successful: Boolean, data: Intent?) -> Unit)? = null
    private var permissionAction: (() -> Unit)? = null

    protected val hooks = Hooks(stateThrottleMs = INTERVAL_60_FPS) {
        onUpdateWrapper()
    }

    private val states = mutableMapOf<String, State<*>>()
    private val effectCleanups = mutableMapOf<String, () -> Unit>()

    protected val lifecycleHookTrigger = LifecycleHookTrigger()

    protected val resetOnResume
        get() = lifecycleHookTrigger.onResume()

    protected val resetOnStart
        get() = lifecycleHookTrigger.onStart()

    protected val resetOnCreate
        get() = lifecycleHookTrigger.onCreate()

    private var currentHookCount = 0

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
        resultLauncher?.unregister()
        permissionLauncher?.unregister()
    }

    private fun onUpdateWrapper() {
        // TODO: If throttled, schedule an update when it expires
        currentHookCount = 0
        if (canUpdate()) {
            onUpdate()
        }
    }

    open fun canUpdate(): Boolean {
        return context != null
    }

    open fun onUpdate() {
        // Do nothing by default
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

    override fun getResult(intent: Intent, action: (successful: Boolean, data: Intent?) -> Unit) {
        resultAction = action
        resultLauncher?.launch(intent)
    }

    protected fun effect(key: String, vararg values: Any?, action: () -> Unit) {
        hooks.effect(key, *values, action = action)
    }

    protected fun <T> memo(key: String, vararg values: Any?, value: () -> T): T {
        return hooks.memo(key, *values, value = value)
    }

    protected fun <T> state(initialValue: T): State<T> {
        return hooks.state(initialValue)
    }

    override fun useAndroidContext(): Context {
        return requireContext()
    }

    override fun useEffect(vararg values: Any?, action: () -> Unit) {
        effect("effect-$currentHookCount", *values, action = action)
        currentHookCount++
    }

    override fun useEffectWithCleanup(vararg values: Any?, action: () -> () -> Unit) {
        val key = "effect-$currentHookCount"
        effect(key, *values) {
            // Invoke the cleanup of the previous effect
            effectCleanups[key]?.invoke()
            val cleanup = action()
            // Save the cleanup for the next effect
            effectCleanups[key] = cleanup
        }
        currentHookCount++
    }

    override fun <T> useMemo(vararg values: Any?, value: () -> T): T {
        val newValue = memo("memo-$currentHookCount", *values, value = value)
        currentHookCount++
        return newValue
    }

    protected fun resetHooks(
        exceptEffects: List<String> = emptyList(),
        exceptMemos: List<String> = emptyList(),
        cleanupEffects: Boolean = true,
        resetState: Boolean = false
    ) {
        if (resetState) {
            states.clear()
        }
        if (cleanupEffects) {
            cleanupEffects()
        }
        hooks.resetEffects(except = exceptEffects)
        hooks.resetMemos(except = exceptMemos)
    }

    protected fun cleanupEffects() {
        effectCleanups.values.forEach {
            it.invoke()
        }
        effectCleanups.clear()
    }

    private fun <T> getSavedState(key: String, initialValue: T): State<T> {
        return if (states.containsKey(key)) {
            @Suppress("UNCHECKED_CAST")
            states[key] as State<T>
        } else {
            val newState = state(initialValue)
            states[key] = newState
            newState
        }
    }

    override fun <T> useState(initialValue: T): Pair<T, (T) -> Unit> {
        val key = "state-$currentHookCount"
        currentHookCount++
        var savedState by getSavedState(key, initialValue)


        val callback = useCallback { value: T ->
            savedState = value
        }

        return Pair(savedState, callback)
    }

    override fun useRootView(): View {
        return requireView()
    }

    override fun useLifecycleOwner(viewOwner: Boolean): LifecycleOwner {
        return if (viewOwner) {
            viewLifecycleOwner
        } else {
            this
        }
    }

    override fun useArguments(): Bundle {
        return requireArguments()
    }

    override fun useActivity(): Activity {
        return requireActivity()
    }

    override fun <T> useView(@IdRes id: Int): T {
        return useMemo(useRootView(), id) {
            requireView().findViewById(id)!!
        }
    }

    companion object {
        const val INTERVAL_60_FPS = 17L
        const val INTERVAL_30_FPS = 33L
        const val INTERVAL_1_FPS = 1000L
    }
}