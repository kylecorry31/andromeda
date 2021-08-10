package com.kylecorry.andromeda.fragments

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kylecorry.andromeda.permissions.PermissionService

open class AndromedaBottomSheetDialogFragment : BottomSheetDialogFragment() {

    private val permissions by lazy { PermissionService(requireContext()) }
    private val permissionActions = mutableMapOf<Int, () -> Unit>()

    fun show(fragment: Fragment, tag: String = javaClass.name) {
        show(fragment.requireActivity(), tag)
    }

    fun show(activity: FragmentActivity, tag: String = javaClass.name) {
        show(activity.supportFragmentManager, tag)
    }

    protected fun requestPermissions(
        requestCode: Int,
        permissions: List<String>,
        action: () -> Unit
    ) {
        if (permissions.all { this.permissions.hasPermission(it) }) {
            action.invoke()
            return
        }

        permissionActions[requestCode] = action
        this.permissions.requestPermissions(
            this,
            permissions,
            requestCode
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        val action = permissionActions[requestCode]
        permissionActions.remove(requestCode)
        action?.invoke()
    }
}