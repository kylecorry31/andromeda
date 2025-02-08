package com.kylecorry.andromeda.views.reactivity

import android.content.Context
import android.widget.FrameLayout
import androidx.core.view.children
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.findViewTreeLifecycleOwner
import com.kylecorry.andromeda.core.ui.ReactiveComponent
import com.kylecorry.andromeda.core.ui.useCallback
import com.kylecorry.luna.hooks.Hooks
import com.kylecorry.luna.hooks.State

class ReactiveComponentView<T : ViewAttributes>(
    context: Context,
    private val rerenderFromExternal: Boolean = true,
    private val onUpdateCallback: ReactiveComponentView<T>.(attributes: T) -> VDOMNode<*, *>
) : FrameLayout(context), ReactiveComponent, LifecycleOwner {

    private var currentHookCount = 0
    private var lastAttributes: T? = null
    private val hooks = Hooks(stateThrottleMs = 17L) {
        onUpdate(lastAttributes ?: return@Hooks)
    }

    private val states = mutableMapOf<String, State<*>>()
    private val effectCleanups = mutableMapOf<String, () -> Unit>()

    // When it is added to the view tree, start the hooks
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        hooks.startStateUpdates()
    }

    // When it is removed from the view tree, stop the hooks
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        hooks.stopStateUpdates()
    }

    override fun useAndroidContext(): Context {
        return context
    }

    private fun effect(key: String, vararg values: Any?, action: () -> Unit) {
        hooks.effect(key, *values, action = action)
    }

    private fun <T> memo(key: String, vararg values: Any?, value: () -> T): T {
        return hooks.memo(key, *values, value = value)
    }

    private fun <T> state(initialValue: T): State<T> {
        return hooks.state(initialValue)
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

    fun onUpdate(attributes: T, isExternal: Boolean = false) {
        currentHookCount = 0
        val shouldRerender =
            rerenderFromExternal || !isExternal || lastAttributes == null || lastAttributes != attributes
        lastAttributes = attributes
        if (shouldRerender) {
            VDOM.render(this, onUpdateCallback(attributes), children.firstOrNull())
        }
    }

    override val lifecycle: Lifecycle
        get() = findViewTreeLifecycleOwner()!!.lifecycle
}