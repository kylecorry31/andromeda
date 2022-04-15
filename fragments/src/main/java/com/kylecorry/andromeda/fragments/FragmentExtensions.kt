package com.kylecorry.andromeda.fragments

import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.commit
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

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

fun BottomSheetDialogFragment.show(fragment: Fragment, tag: String = javaClass.name) {
    show(fragment.requireActivity(), tag)
}

fun BottomSheetDialogFragment.show(activity: FragmentActivity, tag: String = javaClass.name) {
    show(activity.supportFragmentManager, tag)
}

fun Fragment.onBackPressed(
    enabled: Boolean = true,
    onBackPressed: OnBackPressedCallback.() -> Unit
): OnBackPressedCallback {
    return requireActivity().onBackPressedDispatcher.addCallback(this, enabled, onBackPressed)
}