package com.surya.portfolioadmin.data

import com.google.firebase.storage.StorageReference

// Represents an item in Firebase Storage, can be a file or a folder
sealed class StorageItem {
    abstract val ref: StorageReference
    abstract val path: String

    data class File(
        override val ref: StorageReference,
        override val path: String,
        val downloadUrl: String = ""
    ) : StorageItem()

    data class Folder(
        override val ref: StorageReference,
        override val path: String
    ) : StorageItem()
}