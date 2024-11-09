package com.kylecorry.andromeda.files

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import java.time.Instant
import kotlin.collections.toList

class ContentFileSystem(context: Context, treeUri: Uri) {

    private val tree = DocumentFile.fromTreeUri(context, treeUri)

    fun createFile(displayName: String, mimeType: String): ContentDocument? {
        return tree?.createFile(mimeType, displayName)?.toContentDocument()
    }

    fun getFile(displayName: String): ContentDocument? {
        return findFile(displayName)?.toContentDocument()
    }

    fun deleteFile(displayName: String): Boolean {
        return findFile(displayName)?.delete() == true
    }

    fun listFiles(): List<DocumentFile> {
        return tree?.listFiles()?.toList() ?: emptyList()
    }

    fun canWrite(displayName: String? = null): Boolean {
        return if (displayName == null) {
            tree?.canWrite() == true
        } else {
            findFile(displayName)?.canWrite() == true
        }
    }

    fun canRead(displayName: String? = null): Boolean {
        return if (displayName == null) {
            tree?.canRead() == true
        } else {
            findFile(displayName)?.canRead() == true
        }
    }

    private fun findFile(displayName: String): DocumentFile? {
        return tree?.findFile(displayName)
    }

    companion object {
        fun getDocumentFromUri(context: Context, uri: Uri): ContentDocument? {
            return DocumentFile.fromSingleUri(context, uri)?.toContentDocument()
        }

        private fun DocumentFile.toContentDocument(): ContentDocument {
            return ContentDocument(
                uri,
                name,
                type,
                canRead(),
                canWrite(),
                Instant.ofEpochMilli(lastModified()),
                length()
            )
        }
    }
}