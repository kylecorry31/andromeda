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
    action: (uri: Uri?) -> Unit
) {
    val intent = Intents.pickFile(type, message, useSAF)
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
    action: (uri: Uri?) -> Unit
) {
    val intent = Intents.pickFile(types, message, useSAF)
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
    requirePersistentAccess: Boolean = false,
    requireReadAccess: Boolean = true,
    requireWriteAccess: Boolean = false,
    action: (uri: Uri?) -> Unit
) {
    val intent = Intents.pickDirectory(
        message,
        requirePersistentAccess,
        requireReadAccess,
        requireWriteAccess
    )
    getResult(intent) { successful, data ->
        if (successful) {
            action(data?.data)
        } else {
            action(null)
        }
    }
}