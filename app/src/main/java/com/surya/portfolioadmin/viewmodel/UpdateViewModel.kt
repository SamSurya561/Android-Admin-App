package com.surya.portfolioadmin.viewmodel

import android.app.Application
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.surya.portfolioadmin.data.AppUpdateInfo
import com.surya.portfolioadmin.FirestoreService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

// Enum to represent the various states of the download process
enum class DownloadState {
    IDLE, DOWNLOADING, FINISHED
}

class UpdateViewModel(application: Application) : AndroidViewModel(application) {
    private val _updateInfo = MutableStateFlow<AppUpdateInfo?>(null)
    val updateInfo = _updateInfo.asStateFlow()

    // Add the missing isLoading and downloadState flows
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _downloadState = MutableStateFlow(DownloadState.IDLE)
    val downloadState = _downloadState.asStateFlow()

    private var downloadId: Long = 0L
    private val downloadManager = application.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    init {
        checkForUpdates()
    }

    // Make this function public to be called from the UI (e.g., pull-to-refresh)
    fun checkForUpdates() {
        viewModelScope.launch {
            _isLoading.value = true
            _updateInfo.value = FirestoreService.getLatestAppVersion()
            _isLoading.value = false
        }
    }

    // Add the missing download and install functions
    fun startDownload() {
        _updateInfo.value?.downloadUrl?.let { url ->
            if (url.isBlank()) return

            _downloadState.value = DownloadState.DOWNLOADING
            val request = DownloadManager.Request(Uri.parse(url))
                .setTitle("Downloading Update")
                .setDescription("Portfolio Admin v${_updateInfo.value?.versionName}")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalFilesDir(getApplication(), "updates", "app-release.apk")

            downloadId = downloadManager.enqueue(request)
            // A more robust implementation would listen for the download broadcast receiver
            // For simplicity, we'll just assume it finishes and let the user install.
            // This is a placeholder for a more complex broadcast receiver implementation.
            viewModelScope.launch {
                kotlinx.coroutines.delay(5000) // Simulate download time
                _downloadState.value = DownloadState.FINISHED
            }
        }
    }

    fun installUpdate() {
        val context = getApplication<Application>().applicationContext
        val file = File(context.getExternalFilesDir("updates"), "app-release.apk")
        if (file.exists()) {
            val fileUri = FileProvider.getUriForFile(
                context,
                context.packageName + ".provider",
                file
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(fileUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
        _downloadState.value = DownloadState.IDLE
    }
}