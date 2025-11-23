package com.surya.portfolioadmin.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
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

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            viewModel.selectedImageUri = uri
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                OutlinedTextField(
                    value = viewModel.title,
                    onValueChange = { viewModel.title = it },
                    label = { Text("Project Title") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                OutlinedTextField(
                    value = viewModel.description,
                    onValueChange = { viewModel.description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4
                )
            }
            item {
                // --- CATEGORY SELECTOR DROPDOWN ---
                var expanded by remember { mutableStateOf(false) }
                val categories = viewModel.categories
                val selectedCategoryName = categories.find { it.id == viewModel.selectedCategoryId }?.name ?: "Select a Category"

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
            item {
                Button(onClick = { imagePickerLauncher.launch("image/*") }) {
                    Text(if (viewModel.selectedImageUri != null || viewModel.project != null) "Change Image" else "Select Image")
                }
                if (viewModel.selectedImageUri != null) {
                    Text("New image selected.")
                }
            }
        }
    }
}
