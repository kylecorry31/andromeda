package com.kylecorry.andromeda.fragments

import android.app.Activity
import android.app.Application
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.annotation.IdRes
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.commit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.withStateAtLeast
import com.google.android.material.color.DynamicColors
import com.kylecorry.andromeda.core.coroutines.BackgroundMinimumState
import com.kylecorry.andromeda.core.topics.ITopic
import com.kylecorry.andromeda.core.topics.asLiveData
import com.kylecorry.andromeda.core.topics.generic.asLiveData
import com.kylecorry.andromeda.core.ui.ReactiveComponent
import com.kylecorry.luna.coroutines.CoroutineQueueRunner
import com.kylecorry.luna.hooks.Hooks
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

fun Fragment.switchToFragment(
    fragment: Fragment,
    @IdRes holderId: Int,
    addToBackStack: Boolean = false
) {
    parentFragmentManager.commit(true) {
        if (addToBackStack) {
            this.addToBackStack(null)
        }
        this.replace(
            holderId,
            fragment
        )
    }
}

fun DialogFragment.show(fragment: Fragment, tag: String = javaClass.name) {
    show(fragment.requireActivity(), tag)
}

fun DialogFragment.show(activity: FragmentActivity, tag: String = javaClass.name) {
    show(activity.supportFragmentManager, tag)
}

fun Fragment.onBackPressed(
    enabled: Boolean = true,
    onBackPressed: OnBackPressedCallback.() -> Unit
): OnBackPressedCallback {
    return requireActivity().onBackPressedDispatcher.addCallback(this, enabled, onBackPressed)
}

fun Activity.useDynamicColors() {
    DynamicColors.applyToActivityIfAvailable(this)
}

fun Application.useDynamicColors() {
    DynamicColors.applyToActivitiesIfAvailable(this)
}

inline fun LifecycleOwner.repeatInBackground(
    state: BackgroundMinimumState = BackgroundMinimumState.Resumed,
    crossinline block: suspend CoroutineScope.() -> Unit
) {
    val minimumState = when (state) {
        BackgroundMinimumState.Resumed -> Lifecycle.State.RESUMED
        BackgroundMinimumState.Started -> Lifecycle.State.STARTED
        BackgroundMinimumState.Created -> Lifecycle.State.CREATED
        BackgroundMinimumState.Any -> Lifecycle.State.CREATED
    }

    lifecycleScope.launch {
        repeatOnLifecycle(minimumState) {
            block()
        }
    }
}

fun LifecycleOwner.inBackground(
    state: BackgroundMinimumState = BackgroundMinimumState.Resumed,
    cancelWhenBelowState: Boolean = true,
    repeat: Boolean = false,
    block: suspend CoroutineScope.() -> Unit
): Job {
    val minimumState = when (state) {
        BackgroundMinimumState.Resumed -> Lifecycle.State.RESUMED
        BackgroundMinimumState.Started -> Lifecycle.State.STARTED
        BackgroundMinimumState.Created -> Lifecycle.State.CREATED
        BackgroundMinimumState.Any -> null
    }

    return lifecycleScope.launch {

        // If there is no minimum state, just run the block
        if (minimumState == null) {
            block()
            return@launch
        }


        if (!cancelWhenBelowState) {
            // The block will just run until completion once the state is reached
            waitUntilState(minimumState, throwOnCancel = false)
            block()
        } else {
            // The block will be cancelled if the state falls below the minimum state
            // Need to use repeat on lifecycle because there's no better way to do this anymore
            var isComplete = false
            repeatOnLifecycle(minimumState) {
                if (!isComplete) {
                    try {
                        block()
                    } finally {
                        if (!repeat) {
                            isComplete = true
                            this.cancel()
                        }
                    }
                }
            }
        }
    }
}

suspend inline fun LifecycleOwner.waitUntilState(
    state: Lifecycle.State,
    throwOnCancel: Boolean = false,
    crossinline block: () -> Unit = {}
) {
    try {
        withStateAtLeast(state) {
            block()
        }
    } catch (e: CancellationException) {
        if (throwOnCancel) {
            throw e
        }
    }
}

fun LifecycleOwner.scheduleStateUpdates(hooks: Hooks) {
    val observer = LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_RESUME) {
            hooks.startStateUpdates()
        } else if (event == Lifecycle.Event.ON_PAUSE) {
            hooks.stopStateUpdates()
        }
    }
    lifecycle.addObserver(observer)
}

fun ReactiveComponent.useBackgroundEffect(
    vararg values: Any?,
    state: BackgroundMinimumState = BackgroundMinimumState.Resumed,
    cancelWhenBelowState: Boolean = true,
    cancelWhenRerun: Boolean = false,
    repeat: Boolean = false,
    block: suspend CoroutineScope.() -> Unit
) {
    val owner = useLifecycleOwner(false)
    useEffectWithCleanup(*values) {
        val job = owner.inBackground(state, cancelWhenBelowState, repeat, block)
        return@useEffectWithCleanup {
            if (cancelWhenRerun) {
                job.cancel()
            }
        }
    }
}

fun ReactiveComponent.useBackgroundCallback(
    vararg values: Any?,
    state: BackgroundMinimumState = BackgroundMinimumState.Resumed,
    cancelWhenBelowState: Boolean = true,
    callback: suspend CoroutineScope.() -> Unit
): () -> Job {
    val owner = useLifecycleOwner(false)
    return useMemo(*values) { { owner.inBackground(state, cancelWhenBelowState, false, callback) } }
}

fun <R> ReactiveComponent.useBackgroundCallback(
    vararg values: Any?,
    state: BackgroundMinimumState = BackgroundMinimumState.Resumed,
    cancelWhenBelowState: Boolean = true,
    callback: suspend CoroutineScope.(R) -> Unit
): (R) -> Job {
    val owner = useLifecycleOwner(false)
    return useMemo(*values) {
        { p1 ->
            owner.inBackground(state, cancelWhenBelowState) {
                callback(p1)
            }
        }
    }
}

fun <R, S> ReactiveComponent.useBackgroundCallback(
    vararg values: Any?,
    state: BackgroundMinimumState = BackgroundMinimumState.Resumed,
    cancelWhenBelowState: Boolean = true,
    callback: suspend CoroutineScope.(R, S) -> Unit
): (R, S) -> Job {
    val owner = useLifecycleOwner(false)
    return useMemo(*values) {
        { p1, p2 ->
            owner.inBackground(state, cancelWhenBelowState) {
                callback(p1, p2)
            }
        }
    }
}

fun <R, S, U> ReactiveComponent.useBackgroundCallback(
    vararg values: Any?,
    state: BackgroundMinimumState = BackgroundMinimumState.Resumed,
    cancelWhenBelowState: Boolean = true,
    callback: suspend CoroutineScope.(R, S, U) -> Unit
): (R, S, U) -> Job {
    val owner = useLifecycleOwner(false)

    return useMemo(*values) {
        { p1, p2, p3 ->
            owner.inBackground(state, cancelWhenBelowState) {
                callback(p1, p2, p3)
            }
        }
    }
}

fun <R, S, U, V> ReactiveComponent.useBackgroundCallback(
    vararg values: Any?,
    state: BackgroundMinimumState = BackgroundMinimumState.Resumed,
    cancelWhenBelowState: Boolean = true,
    callback: suspend CoroutineScope.(R, S, U, V) -> Unit
): (R, S, U, V) -> Job {
    val owner = useLifecycleOwner(false)

    return useMemo(*values) {
        { p1, p2, p3, p4 ->
            owner.inBackground(
                state,
                cancelWhenBelowState,
            ) {
                callback(p1, p2, p3, p4)
            }
        }
    }
}

fun <R, S, U, V, W> ReactiveComponent.useBackgroundCallback(
    vararg values: Any?,
    state: BackgroundMinimumState = BackgroundMinimumState.Resumed,
    cancelWhenBelowState: Boolean = true,
    callback: suspend CoroutineScope.(R, S, U, V, W) -> Unit
): (R, S, U, V, W) -> Job {
    val owner = useLifecycleOwner(false)

    return useMemo(*values) {
        { p1, p2, p3, p4, p5 ->
            owner.inBackground(
                state,
                cancelWhenBelowState,
            ) {
                callback(p1, p2, p3, p4, p5)
            }
        }
    }
}

fun <T> ReactiveComponent.useFlow(
    flow: Flow<T>,
    state: BackgroundMinimumState = BackgroundMinimumState.Created,
    collectOn: CoroutineContext = Dispatchers.Default,
    observeOn: CoroutineContext = Dispatchers.Main,
): T? {
    val (value, setValue) = useState<T?>(null)
    useBackgroundEffect(
        state = state,
        repeat = true,
        cancelWhenRerun = true,
        cancelWhenBelowState = true
    ) {
        withContext(collectOn) {
            flow.collect {
                withContext(observeOn) {
                    setValue(it)
                }
            }
        }
    }
    return value
}

fun <T : ITopic, V> ReactiveComponent.useTopic(topic: T, default: V, mapper: (T) -> V): V {
    val (state, setState) = useState(default)
    val owner = useLifecycleOwner()

    // Note: This does not change when the mapper changes
    useEffect(topic, owner) {
        topic.asLiveData().observe(owner) {
            setState(mapper(topic))
        }
    }

    return state
}

fun <T : ITopic, V> ReactiveComponent.useTopic(topic: T, mapper: (T) -> V?): V? {
    return useTopic(topic, null, mapper)
}

fun <T : Any, V> ReactiveComponent.useTopic(
    topic: com.kylecorry.andromeda.core.topics.generic.ITopic<T>,
    default: V,
    mapper: (T) -> V
): V {
    val (state, setState) = useState(default)
    val owner = useLifecycleOwner()

    // Note: This does not change when the mapper changes
    useEffect(topic, owner) {
        topic.asLiveData().observe(owner) {
            setState(mapper(it))
        }
    }

    return state
}

fun <T : Any, V> ReactiveComponent.useTopic(
    topic: com.kylecorry.andromeda.core.topics.generic.ITopic<T>,
    mapper: (T) -> V?
): V? {
    return useTopic(topic, null, mapper)
}

fun <T> ReactiveComponent.useBackgroundMemo(
    vararg values: Any?,
    state: BackgroundMinimumState = BackgroundMinimumState.Resumed,
    cancelWhenBelowState: Boolean = true,
    cancelWhenRerun: Boolean = false,
    block: suspend CoroutineScope.() -> T
): T? {
    val (currentState, setCurrentState) = useState<T?>(null)

    useBackgroundEffect(values, state, cancelWhenBelowState, cancelWhenRerun) {
        setCurrentState(block())
    }

    return currentState
}

fun ReactiveComponent.useClickCallback(view: View, vararg values: Any?, callback: () -> Unit) {
    useEffect(view, *values) {
        view.setOnClickListener {
            callback()
        }
    }
}

fun <T> ReactiveComponent.useArgument(key: String): T? {
    val arguments = useArguments()
    return useMemo(arguments, key) {
        @Suppress("DEPRECATION", "UNCHECKED_CAST")
        arguments.get(key) as? T?
    }
}

fun ReactiveComponent.useCoroutineQueue(
    queueSize: Int = 1,
    ignoreExceptions: Boolean = false
): CoroutineQueueRunner {
    // This will not create a new runner if the arguments change
    return useMemo {
        CoroutineQueueRunner(queueSize, ignoreExceptions = ignoreExceptions)
    }
}