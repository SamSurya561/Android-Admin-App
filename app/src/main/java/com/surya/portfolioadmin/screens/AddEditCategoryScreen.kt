package com.surya.portfolioadmin.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.surya.portfolioadmin.viewmodel.CategoryViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCategoryScreen(
    categoryId: String?,
    onNavigateBack: () -> Unit,
    categoryViewModel: CategoryViewModel = viewModel()
) {
    var name by remember { mutableStateOf("") }
    var gridSize by remember { mutableStateOf("medium") }
    // --- 'order' STATE IS NO LONGER NEEDED HERE ---
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var existingImageUrl by remember { mutableStateOf<String?>(null) }

    // --- THIS VARIABLE IS NEEDED FOR THE UPDATE LOGIC ---
    var originalOrder by remember { mutableStateOf(0) }


    val isEditing = categoryId != null

    LaunchedEffect(categoryId) {
        if (isEditing) {
            val category = categoryViewModel.categories.value.find { it.id == categoryId }
            if (category != null) {
                name = category.name
                gridSize = category.gridSize
                existingImageUrl = category.imageUrl
                // --- Store the original order for editing ---
                originalOrder = (category.order as? Int) ?: 0

            }
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri -> imageUri = uri }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit Category" else "Add New Category") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (isEditing) {
                        val originalCategory = categoryViewModel.categories.value.find { it.id == categoryId }!!
                        // --- When editing, we preserve the original order ---
                        val updatedCategory = originalCategory.copy(name = name, gridSize = gridSize, order = originalOrder)
                        categoryViewModel.updateCategory(updatedCategory, imageUri, onNavigateBack)
                    } else {
                        if (imageUri != null) {
                            // --- CALL THE NEW, SIMPLER addCategory FUNCTION ---
                            categoryViewModel.addCategory(name, gridSize, imageUri!!, onNavigateBack)
                        }
                    }
                },
                containerColor = if (name.isNotBlank() && (imageUri != null || isEditing)) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
            ) {
                Icon(Icons.Default.Save, contentDescription = "Save Category")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Category Name") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // --- THE 'Display Order' TEXT FIELD HAS BEEN REMOVED ---

            item {
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = gridSize.replaceFirstChar { it.titlecase(Locale.getDefault()) },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Grid Size for Bento UI") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        listOf("medium", "large", "tall").forEach { size ->
                            DropdownMenuItem(
                                text = { Text(size.replaceFirstChar { it.titlecase(Locale.getDefault()) }) },
                                onClick = {
                                    gridSize = size
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
            item {
                Text("Cover Image", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                        .clickable { imagePickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    val displayImage = imageUri ?: existingImageUrl
                    if (displayImage != null) {
                        AsyncImage(
                            model = displayImage,
                            contentDescription = "Cover Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CloudUpload, contentDescription = "Upload", modifier = Modifier.size(48.dp))
                            Text("Click to browse or drop image")
                        }
                    }
                }
            }
        }
    }
}