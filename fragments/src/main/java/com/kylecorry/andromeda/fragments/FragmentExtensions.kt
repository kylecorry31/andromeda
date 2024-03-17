package com.kylecorry.andromeda.fragments

import android.app.Activity
import android.app.Application
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.annotation.IdRes
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.commit
import androidx.lifecycle.*
import com.google.android.material.color.DynamicColors
import com.kylecorry.andromeda.core.coroutines.BackgroundMinimumState
import com.kylecorry.luna.cache.Hooks
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

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
){
    val minimumState = when (state) {
        BackgroundMinimumState.Resumed -> Lifecycle.State.RESUMED
        BackgroundMinimumState.Started -> Lifecycle.State.STARTED
        BackgroundMinimumState.Created -> Lifecycle.State.CREATED
        BackgroundMinimumState.Any -> Lifecycle.State.CREATED
    }

    lifecycleScope.launch {
        repeatOnLifecycle(minimumState){
            block()
        }
    }
}

fun LifecycleOwner.inBackground(
    state: BackgroundMinimumState = BackgroundMinimumState.Resumed,
    cancelWhenBelowState: Boolean = true,
    block: suspend CoroutineScope.() -> Unit
) {
    val minimumState = when (state) {
        BackgroundMinimumState.Resumed -> Lifecycle.State.RESUMED
        BackgroundMinimumState.Started -> Lifecycle.State.STARTED
        BackgroundMinimumState.Created -> Lifecycle.State.CREATED
        BackgroundMinimumState.Any -> null
    }

    lifecycleScope.launch {

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
                        isComplete = true
                        this.cancel()
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