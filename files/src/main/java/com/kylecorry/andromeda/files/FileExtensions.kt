package com.kylecorry.andromeda.files

import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import com.kylecorry.andromeda.fragments.registerIntentCallback

fun Fragment.registerFilePicker(action: (uri: Uri?) -> Unit): ActivityResultLauncher<Intent> {
    return registerIntentCallback { successful, data ->
        val ret = if (successful) {
            data?.data
        } else {
            null
        }
        action(ret)
    }
}

fun Fragment.registerFileCreator(action: (uri: Uri?) -> Unit): ActivityResultLauncher<Intent> {
    return registerIntentCallback { successful, data ->
        val ret = if (successful) {
            data?.data
        } else {
            null
        }
        action(ret)
    }
}

fun Fragment.createFile(filename: String, type: String, launcher: ActivityResultLauncher<Intent>) {
    val intent = ExternalFileService(requireContext()).createFile(filename, type)
    launcher.launch(intent)
}

fun Fragment.pickFile(type: String, message: String, launcher: ActivityResultLauncher<Intent>) {
    val intent = ExternalFileService(requireContext()).pickFile(type, message)
    launcher.launch(intent)
}

fun Fragment.pickFile(
    types: List<String>,
    message: String,
    launcher: ActivityResultLauncher<Intent>
) {
    val intent = ExternalFileService(requireContext()).pickFile(types, message)
    launcher.launch(intent)
}