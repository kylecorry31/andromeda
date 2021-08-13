package com.kylecorry.andromeda.files

import android.net.Uri
import androidx.fragment.app.Fragment
import com.kylecorry.andromeda.fragments.createActivityResult

fun Fragment.createFilePicker(
    type: String,
    message: String,
    action: (uri: Uri?) -> Unit
): () -> Unit {
    val intent = ExternalFileService(requireContext()).pickFile(type, message)
    return createActivityResult(intent) { successful, data ->
        val ret = if (successful) {
            data?.data
        } else {
            null
        }
        action(ret)
    }
}

fun Fragment.createFilePicker(
    types: List<String>,
    message: String,
    action: (uri: Uri?) -> Unit
): () -> Unit {
    val intent = ExternalFileService(requireContext()).pickFile(types, message)
    return createActivityResult(intent) { successful, data ->
        val ret = if (successful) {
            data?.data
        } else {
            null
        }
        action(ret)
    }
}

fun Fragment.createFileSaver(
    filename: String,
    type: String,
    action: (uri: Uri?) -> Unit
): () -> Unit {
    val intent = ExternalFileService(requireContext()).createFile(filename, type)
    return createActivityResult(intent) { successful, data ->
        val ret = if (successful) {
            data?.data
        } else {
            null
        }
        action(ret)
    }
}