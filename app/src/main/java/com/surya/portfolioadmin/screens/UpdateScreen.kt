package com.surya.portfolioadmin.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.surya.portfolioadmin.BuildConfig
import com.surya.portfolioadmin.viewmodel.DownloadState
import com.surya.portfolioadmin.viewmodel.UpdateViewModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun UpdateScreen(
    updateViewModel: UpdateViewModel = viewModel() // Correctly get the ViewModel instance
) {
    val updateInfo by updateViewModel.updateInfo.collectAsState()
    val isLoading by updateViewModel.isLoading.collectAsState()
    val downloadState by updateViewModel.downloadState.collectAsState()
    val context = LocalContext.current
    val currentVersionCode = BuildConfig.VERSION_CODE
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isLoading,
        onRefresh = { updateViewModel.checkForUpdates() } // This will now work
    )

    var isChangelogExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
    ) {
        if (isLoading && updateInfo == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(16.dp))
                    Text("Checking for updates...")
                }
            }
        } else if (updateInfo == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Could not check for updates. Swipe down to try again.")
            }
        } else {
            val latestVersionCode = updateInfo!!.versionCode
            val isUpdateAvailable = latestVersionCode > currentVersionCode

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Your Version: ${BuildConfig.VERSION_NAME} ($currentVersionCode)",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    "Latest Version: ${updateInfo!!.versionName} ($latestVersionCode)",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(Modifier.height(24.dp))

                if (isUpdateAvailable) {
                    Box(modifier = Modifier.animateContentSize()) {
                        when (downloadState) {
                            DownloadState.IDLE -> {
                                Button(onClick = { updateViewModel.startDownload() }) {
                                    Text("Download Update")
                                }
                            }

                            DownloadState.DOWNLOADING -> {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator()
                                    Spacer(Modifier.height(8.dp))
                                    Text("Downloading...", style = MaterialTheme.typography.bodySmall)
                                }
                            }

                            DownloadState.FINISHED -> {
                                Button(onClick = { updateViewModel.installUpdate() }) {
                                    Text("Install Now")
                                }
                            }
                        }
                    }
                } else {
                    Text("You are on the latest version.")
                }

                Divider(modifier = Modifier.padding(vertical = 24.dp))

                Text("Changelog", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(8.dp))

                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isChangelogExpanded = !isChangelogExpanded }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = updateInfo!!.changelog.replace("\\n", "\n"),
                            textAlign = TextAlign.Start,
                            maxLines = if (isChangelogExpanded) Int.MAX_VALUE else 5,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (!isChangelogExpanded) {
                            Text(
                                "... tap to see more",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .align(Alignment.End)
                                    .padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        PullRefreshIndicator(
            refreshing = isLoading,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}