package com.surya.portfolioadmin.data

data class AppUpdateInfo(
    val versionCode: Long = 0, // Use Long for compatibility with Firestore numbers
    val versionName: String = "",
    val changelog: String = "",
    val downloadUrl: String = ""
)