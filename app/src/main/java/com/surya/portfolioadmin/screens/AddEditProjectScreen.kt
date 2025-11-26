package com.surya.portfolioadmin.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.surya.portfolioadmin.viewmodel.AddEditProjectViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProjectScreen(
    projectId: String?,
    onNavigateBack: () -> Unit,
    viewModel: AddEditProjectViewModel = viewModel()
) {
    LaunchedEffect(projectId) {
        viewModel.loadProject(projectId)
    }

    // Launcher for single Cover Image
    val coverImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            viewModel.selectedCoverImageUri = uri
        }
    )

    // Launcher for multiple Gallery Images
    val galleryImagesLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
        onResult = { uris: List<Uri> ->
            viewModel.addGalleryImages(uris)
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (projectId == null) "Add Project" else "Edit Project") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.saveProject(onSuccess = onNavigateBack) }) {
                Icon(Icons.Default.Save, contentDescription = "Save Project")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // --- 1. DETAILS SECTION ---
            item {
                Text("Details", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = viewModel.title,
                    onValueChange = { viewModel.title = it },
                    label = { Text("Project Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = viewModel.description,
                    onValueChange = { viewModel.description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4
                )
            }

            // --- 2. CATEGORY SELECTION ---
            item {
                // Dropdown logic remains the same
                var expanded by remember { mutableStateOf(false) }
                val categories = viewModel.categories
                val selectedCategoryName = categories.find { it.id == viewModel.selectedCategoryId }?.name ?: "Select Category"

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedCategoryName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = {
                                    viewModel.selectedCategoryId = category.id
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            // --- 3. COVER IMAGE SECTION ---
            item {
                Text("Cover Image (Website Visible)", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(8.dp))

                val coverDisplay = viewModel.selectedCoverImageUri ?: viewModel.existingCoverImageUrl.takeIf { it.isNotEmpty() }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .clickable { coverImageLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (coverDisplay != null) {
                        AsyncImage(
                            model = coverDisplay,
                            contentDescription = "Cover",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        // Overlay label
                        Box(Modifier.fillMaxSize().background(Color.Black.copy(0.3f)))
                        Text("Tap to Change", color = Color.White, style = MaterialTheme.typography.labelLarge)
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CloudUpload, "Upload", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Select Cover Image", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // --- 4. GALLERY SECTION ---
            item {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Project Gallery", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    TextButton(onClick = { galleryImagesLauncher.launch("image/*") }) {
                        Icon(Icons.Default.Add, "Add")
                        Spacer(Modifier.width(4.dp))
                        Text("Add Images")
                    }
                }

                if (viewModel.existingGalleryUrls.isEmpty() && viewModel.selectedGalleryUris.isEmpty()) {
                    Text("No images added to gallery yet.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        // Existing Images (Strings)
                        items(viewModel.existingGalleryUrls) { url ->
                            GalleryThumbnail(
                                model = url,
                                onDelete = { viewModel.removeExistingGalleryImage(url) }
                            )
                        }
                        // New Images (Uris)
                        items(viewModel.selectedGalleryUris) { uri ->
                            GalleryThumbnail(
                                model = uri,
                                onDelete = { viewModel.removeNewGalleryImage(uri) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GalleryThumbnail(model: Any, onDelete: () -> Unit) {
    Box(
        modifier = Modifier
            .size(120.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
    ) {
        AsyncImage(
            model = model,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        IconButton(
            onClick = onDelete,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .size(24.dp)
                .background(Color.Black.copy(0.6f), CircleShape)
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Remove",
                tint = Color.White,
                modifier = Modifier.padding(4.dp)
            )
        }
    }
}