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
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.DynamicColorsOptions
import com.kylecorry.andromeda.core.coroutines.BackgroundMinimumState
import kotlinx.coroutines.CoroutineScope
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

fun LifecycleOwner.inBackground(
    state: BackgroundMinimumState = BackgroundMinimumState.Resumed,
    block: suspend CoroutineScope.() -> Unit
) {
    when (state) {
        BackgroundMinimumState.Resumed -> lifecycleScope.launchWhenResumed(block)
        BackgroundMinimumState.Started -> lifecycleScope.launchWhenStarted(block)
        BackgroundMinimumState.Created -> lifecycleScope.launchWhenCreated(block)
        BackgroundMinimumState.Any -> lifecycleScope.launch { block() }
    }
}