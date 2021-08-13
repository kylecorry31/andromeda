package com.kylecorry.andromeda.files

import android.net.Uri
import androidx.fragment.app.Fragment
import com.kylecorry.andromeda.fragments.getResult

fun Fragment.pickFile(type: String, message: String, action: (uri: Uri?) -> Unit) {
    getResult(ExternalFileService(requireContext()).pickFile(type, message)) { successful, data ->
        val ret = if (successful) {
            data?.data
        } else {
            null
        }
        action(ret)
    }
}

fun Fragment.pickFile(types: List<String>, message: String, action: (uri: Uri?) -> Unit) {
    getResult(ExternalFileService(requireContext()).pickFile(types, message)) { successful, data ->
        val ret = if (successful) {
            data?.data
        } else {
            null
        }
        action(ret)
    }
}

fun Fragment.createFile(filename: String, type: String, action: (uri: Uri?) -> Unit) {
    getResult(
        ExternalFileService(requireContext()).createFile(
            filename,
            type
        )
    ) { successful, data ->
        val ret = if (successful) {
            data?.data
        } else {
            null
        }
        action(ret)
    }
}