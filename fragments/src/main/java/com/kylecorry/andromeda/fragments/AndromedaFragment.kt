package com.kylecorry.andromeda.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.kylecorry.andromeda.core.system.IntentResultRetriever
import com.kylecorry.andromeda.permissions.PermissionRationale
import com.kylecorry.andromeda.permissions.Permissions
import com.kylecorry.andromeda.permissions.SpecialPermission

open class AndromedaFragment : Fragment(), IPermissionRequester, IntentResultRetriever {
    private var resultLauncher: ActivityResultLauncher<Intent>? = null
    private var permissionLauncher: ActivityResultLauncher<Array<String>>? = null
    private lateinit var specialPermissionLauncher: SpecialPermissionLauncher

    private var resultAction: ((successful: Boolean, data: Intent?) -> Unit)? = null
    private var permissionAction: (() -> Unit)? = null


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
}
