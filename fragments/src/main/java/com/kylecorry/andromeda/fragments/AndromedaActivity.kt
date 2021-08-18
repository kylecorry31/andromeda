package com.kylecorry.andromeda.fragments

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.kylecorry.andromeda.core.system.Intents
import kotlin.random.Random

class AndromedaActivity : AppCompatActivity() {

    private var resultAction: ((successful: Boolean, data: Intent?) -> Unit)? = null
    private var permissionAction: (() -> Unit)? = null

    private var resultRequestCode: Int? = null
    private var permissionRequestCode: Int? = null

    protected fun requestPermissions(permissions: List<String>, action: () -> Unit) {
        val requestCode = Random.nextInt()
        permissionRequestCode = requestCode
        permissionAction = action
        ActivityCompat.requestPermissions(
            this,
            permissions.toTypedArray(),
            requestCode
        )
    }

    protected fun getResult(intent: Intent, action: (successful: Boolean, data: Intent?) -> Unit) {
        val requestCode = Random.nextInt()
        resultRequestCode = requestCode
        resultAction = action
        startActivityForResult(intent, requestCode)
    }

    protected fun createFile(filename: String, type: String, action: (uri: Uri?) -> Unit) {
        val intent = Intents.createFile(filename, type)
        getResult(intent) { successful, data ->
            if (successful) {
                action(data?.data)
            } else {
                action(null)
            }
        }
    }

    protected fun pickFile(type: String, message: String, action: (uri: Uri?) -> Unit) {
        val intent = Intents.pickFile(type, message)
        getResult(intent) { successful, data ->
            if (successful) {
                action(data?.data)
            } else {
                action(null)
            }
        }
    }

    protected fun pickFile(types: List<String>, message: String, action: (uri: Uri?) -> Unit) {
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