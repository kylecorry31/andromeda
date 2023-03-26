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

inline fun LifecycleOwner.inBackground(
    state: BackgroundMinimumState = BackgroundMinimumState.Resumed,
    cancelWhenBelowState: Boolean = true,
    throwOnDestroy: Boolean = false,
    crossinline block: () -> Unit
) {
    val minimumState = when (state) {
        BackgroundMinimumState.Resumed -> Lifecycle.State.RESUMED
        BackgroundMinimumState.Started -> Lifecycle.State.STARTED
        BackgroundMinimumState.Created -> Lifecycle.State.CREATED
        BackgroundMinimumState.Any -> Lifecycle.State.INITIALIZED
    }

    lifecycleScope.launch {
        waitUntilState(minimumState, true, throwOnDestroy) {
            if (cancelWhenBelowState) {
                block()
            }
        }

        if (!cancelWhenBelowState) {
            block()
        }
    }
}

suspend inline fun <R> LifecycleOwner.waitUntilState(
    state: Lifecycle.State,
    unchecked: Boolean = false,
    throwOnDestroy: Boolean = false,
    crossinline block: () -> R
): R? {
    // The minimum state for withStateAtLeast is CREATED, or else it will throw
    // If we are waiting for a state below CREATED, we should just run it if unchecked
    if (unchecked && state < Lifecycle.State.CREATED) {
        return block()
    }

    return try {
        withStateAtLeast(state, block)
    } catch (e: LifecycleDestroyedException) {
        if (throwOnDestroy) {
            throw e
        }
        null
    }
}