package com.surya.portfolioadmin.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.surya.portfolioadmin.data.StorageItem
import com.surya.portfolioadmin.viewmodel.AssetViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetManagerScreen(
    assetViewModel: AssetViewModel = viewModel()
) {
    val items by assetViewModel.storageItems.collectAsState()
    val isLoading by assetViewModel.isLoading.collectAsState()
    val currentPath by assetViewModel.currentPath.collectAsState()
    val context = LocalContext.current

    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var itemToRename by remember { mutableStateOf<StorageItem?>(null) }
    var itemToMove by remember { mutableStateOf<StorageItem?>(null) }
    var viewingImage by remember { mutableStateOf<StorageItem.File?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val fileName = it.lastPathSegment ?: "new_upload_${System.currentTimeMillis()}"
            assetViewModel.uploadFile(it, fileName)
        }
    }

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(items) {
        isVisible = false
        isVisible = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Asset Manager", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    if (currentPath.isNotEmpty()) {
                        IconButton(onClick = { assetViewModel.navigateUp() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                    }
                    IconButton(onClick = { showCreateFolderDialog = true }) {
                        Icon(Icons.Default.CreateNewFolder, contentDescription = "Create Folder")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { filePickerLauncher.launch("*/*") }) {
                Icon(Icons.Default.UploadFile, contentDescription = "Upload File")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading && items.isEmpty()) {
                CircularProgressIndicator()
            } else if (items.isEmpty()) {
                Text("This folder is empty.", modifier = Modifier.padding(16.dp))
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 120.dp),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    itemsIndexed(
                        items = items,
                        key = { _, item -> item.path }
                    ) { index, item ->
                        AnimatedVisibility(
                            visible = isVisible,
                            enter = fadeIn(animationSpec = tween(delayMillis = index * 50)) +
                                    slideInVertically(initialOffsetY = { it / 2 }, animationSpec = tween(delayMillis = index * 50))
                        ) {
                            AssetCard(
                                item = item,
                                onSingleTap = { tappedItem ->
                                    when (tappedItem) {
                                        is StorageItem.Folder -> assetViewModel.loadItems(tappedItem.path)
                                        is StorageItem.File -> viewingImage = tappedItem
                                    }
                                },
                                onRename = { itemToRename = it },
                                onMove = { itemToMove = it },
                                onDelete = { assetViewModel.deleteItem(it) },
                                onCopyUrl = { file ->
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Download URL", file.downloadUrl)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "URL Copied!", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCreateFolderDialog) {
        InputDialog(
            title = "Create New Folder",
            label = "Folder Name",
            onConfirm = { name ->
                assetViewModel.createFolder(name)
                showCreateFolderDialog = false
            },
            onDismiss = { showCreateFolderDialog = false }
        )
    }

    itemToRename?.let { item ->
        InputDialog(
            title = "Rename Item",
            label = "New Name",
            initialValue = item.ref.name,
            onConfirm = { newName ->
                assetViewModel.renameItem(item, newName)
                itemToRename = null
            },
            onDismiss = { itemToRename = null }
        )
    }

    itemToMove?.let { item ->
        InputDialog(
            title = "Move Item",
            label = "Destination Path",
            onConfirm = { newPath ->
                assetViewModel.moveItem(item, newPath)
                itemToMove = null
            },
            onDismiss = { itemToMove = null }
        )
    }

    if (viewingImage != null) {
        FullScreenImageViewer(file = viewingImage!!, onDismiss = { viewingImage = null })
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AssetCard(
    item: StorageItem,
    onSingleTap: (StorageItem) -> Unit,
    onRename: (StorageItem) -> Unit,
    onMove: (StorageItem) -> Unit,
    onDelete: (StorageItem) -> Unit,
    onCopyUrl: (StorageItem.File) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .combinedClickable(
                onClick = { onSingleTap(item) },
                onLongClick = { showMenu = true }
            ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            when (item) {
                is StorageItem.Folder -> {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Folder, "Folder", modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(8.dp))
                        Text(item.ref.name, style = MaterialTheme.typography.bodyMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                }
                is StorageItem.File -> {
                    AsyncImage(model = item.downloadUrl, contentDescription = item.ref.name, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)))
                    Text(
                        text = item.ref.name,
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.align(Alignment.BottomStart).padding(8.dp)
                    )
                }
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(text = { Text("Rename") }, onClick = { onRename(item); showMenu = false })
                DropdownMenuItem(text = { Text("Move") }, onClick = { onMove(item); showMenu = false })
                if (item is StorageItem.File) {
                    DropdownMenuItem(text = { Text("Copy URL") }, onClick = { onCopyUrl(item); showMenu = false })
                }
                Divider()
                DropdownMenuItem(
                    text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                    onClick = { onDelete(item); showMenu = false }
                )
            }
        }
    }
}

@Composable
fun InputDialog(
    title: String,
    label: String,
    initialValue: String = "",
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf(initialValue) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text(label) },
                singleLine = true
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(text) },
                enabled = text.isNotBlank()
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun FullScreenImageViewer(file: StorageItem.File, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = file.downloadUrl,
                contentDescription = "Full screen image",
                modifier = Modifier.fillMaxSize()
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(Icons.Default.Close, "Close", tint = Color.White)
            }
        }
    }
}
