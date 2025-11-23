package com.surya.portfolioadmin

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.File

class AppDownloader(private val context: Context) {

    private val downloadManager = context.getSystemService(DownloadManager::class.java)

    fun downloadFile(url: String, fileName: String): Long {
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle("Portfolio Admin Update")
            .setDescription("Downloading new version...")
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

        return downloadManager.enqueue(request)
    }

    fun installApp(downloadId: Long) {
        val fileUri = downloadManager.getUriForDownloadedFile(downloadId)

        if (fileUri != null) {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(
                    getUriFromFile(context, File(fileUri.path!!)),
                    "application/vnd.android.package-archive"
                )
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(intent)
        }
    }

    private fun getUriFromFile(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
    }
}
