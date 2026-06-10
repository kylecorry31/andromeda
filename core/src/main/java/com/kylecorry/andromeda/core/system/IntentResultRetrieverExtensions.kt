package com.kylecorry.andromeda.core.system

import android.net.Uri

fun IntentResultRetriever.createFile(
    filename: String,
    type: String,
    message: String = filename,
    action: (uri: Uri?) -> Unit
) {
    val intent = Intents.createFile(filename, type, message)
    getResult(intent) { successful, data ->
        if (successful) {
            action(data?.data)
        } else {
            action(null)
        }
    }
}

fun IntentResultRetriever.createFile(
    filename: String,
    types: List<String>,
    message: String = filename,
    action: (uri: Uri?) -> Unit
) {
    val intent = Intents.createFile(filename, types, message)
    getResult(intent) { successful, data ->
        if (successful) {
            action(data?.data)
        } else {
            action(null)
        }
    }
}

fun IntentResultRetriever.pickFile(
    type: String,
    message: String,
    useSAF: Boolean = true,
    access: UriAccess = UriAccess(),
    action: (uri: Uri?) -> Unit
) {
    val intent = Intents.pickFile(
        type,
        message,
        useSAF,
        access
    )
    getResult(intent) { successful, data ->
        if (successful) {
            action(data?.data)
        } else {
            action(null)
        }
    }
}

fun IntentResultRetriever.pickFile(
    types: List<String>,
    message: String,
    useSAF: Boolean = true,
    access: UriAccess = UriAccess(),
    action: (uri: Uri?) -> Unit
) {
    val intent = Intents.pickFile(
        types,
        message,
        useSAF,
        access
    )
    getResult(intent) { successful, data ->
        if (successful) {
            action(data?.data)
        } else {
            action(null)
        }
    }
}

fun IntentResultRetriever.pickDirectory(
    message: String,
    access: UriAccess = UriAccess(),
    action: (uri: Uri?) -> Unit
) {
    val intent = Intents.pickDirectory(
        message,
        access
    )
    getResult(intent) { successful, data ->
        if (successful) {
            action(data?.data)
        } else {
            action(null)
        }
    }
}
