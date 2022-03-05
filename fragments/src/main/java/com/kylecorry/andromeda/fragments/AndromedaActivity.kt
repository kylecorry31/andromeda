package com.kylecorry.andromeda.fragments

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.kylecorry.andromeda.core.system.Intents
import com.kylecorry.andromeda.permissions.Permissions
import kotlin.math.absoluteValue
import kotlin.random.Random

open class AndromedaActivity : AppCompatActivity(), IPermissionRequester {

    private var resultAction: ((successful: Boolean, data: Intent?) -> Unit)? = null
    private var permissionAction: (() -> Unit)? = null

    private var resultRequestCode: Int? = null
    private var permissionRequestCode: Int? = null

    override fun requestPermissions(permissions: List<String>, action: () -> Unit) {
        val notGranted = permissions.filterNot { Permissions.hasPermission(this, it) }
        if (notGranted.isEmpty()){
            action()
            return
        }
        val requestCode = Random.nextInt().absoluteValue
        permissionRequestCode = requestCode
        permissionAction = action
        ActivityCompat.requestPermissions(
            this,
            notGranted.toTypedArray(),
            requestCode
        )
    }

    fun getResult(intent: Intent, action: (successful: Boolean, data: Intent?) -> Unit) {
        val requestCode = Random.nextInt().absoluteValue
        resultRequestCode = requestCode
        resultAction = action
        startActivityForResult(intent, requestCode)
    }

    fun createFile(filename: String, type: String, message: String = filename, action: (uri: Uri?) -> Unit) {
        val intent = Intents.createFile(filename, type, message)
        getResult(intent) { successful, data ->
            if (successful) {
                action(data?.data)
            } else {
                action(null)
            }
        }
    }

    fun createFile(filename: String, types: List<String>, message: String = filename, action: (uri: Uri?) -> Unit) {
        val intent = Intents.createFile(filename, types, message)
        getResult(intent) { successful, data ->
            if (successful) {
                action(data?.data)
            } else {
                action(null)
            }
        }
    }

    fun pickFile(type: String, message: String, action: (uri: Uri?) -> Unit) {
        val intent = Intents.pickFile(type, message)
        getResult(intent) { successful, data ->
            if (successful) {
                action(data?.data)
            } else {
                action(null)
            }
        }
    }

    fun pickFile(types: List<String>, message: String, action: (uri: Uri?) -> Unit) {
        val intent = Intents.pickFile(types, message)
        getResult(intent) { successful, data ->
            if (successful) {
                action(data?.data)
            } else {
                action(null)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == resultRequestCode) {
            resultAction?.invoke(resultCode == RESULT_OK, data)
            resultRequestCode = null
            resultAction = null
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == permissionRequestCode) {
            permissionAction?.invoke()
            permissionRequestCode = null
            permissionAction = null
        }
    }

}