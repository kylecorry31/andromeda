package com.kylecorry.andromeda.fragments

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.IdRes
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

inline fun FragmentManager.doTransaction(func: FragmentTransaction.() -> FragmentTransaction) {
    beginTransaction().func().commitAllowingStateLoss()
}

fun Fragment.switchToFragment(
    fragment: Fragment,
    @IdRes holderId: Int,
    addToBackStack: Boolean = false
) {
    parentFragmentManager.doTransaction {
        if (addToBackStack) {
            this.addToBackStack(null)
        }
        this.replace(
            holderId,
            fragment
        )
    }
}

fun Fragment.createActivityResult(
    intent: Intent,
    callback: (successful: Boolean, data: Intent?) -> Unit
): () -> Unit {
    val launcher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        val successful = it.resultCode == Activity.RESULT_OK
        callback(successful, it.data)
    }
    return {
        launcher.launch(intent)
    }
}

fun BottomSheetDialogFragment.show(fragment: Fragment, tag: String = javaClass.name) {
    show(fragment.requireActivity(), tag)
}

fun BottomSheetDialogFragment.show(activity: FragmentActivity, tag: String = javaClass.name) {
    show(activity.supportFragmentManager, tag)
}