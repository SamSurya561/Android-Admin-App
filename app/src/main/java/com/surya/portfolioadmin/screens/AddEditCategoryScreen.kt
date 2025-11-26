package com.surya.portfolioadmin.screens

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
    var originalOrder by remember { mutableStateOf(0) }

    val isEditing = categoryId != null

    LaunchedEffect(categoryId) {
        if (isEditing) {
            val category = categoryViewModel.categories.value.find { it.id == categoryId }
            if (category != null) {
                name = category.name
                gridSize = category.gridSize
                originalOrder = (category.order as? Int) ?: 0
            }
        }
    }

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
                    if (name.isNotBlank()) {
                        if (isEditing) {
                            val originalCategory = categoryViewModel.categories.value.find { it.id == categoryId }!!
                            val updatedCategory = originalCategory.copy(name = name, gridSize = gridSize, order = originalOrder)
                            // Using a fire-and-forget approach for simplicity in the view,
                            // assuming ViewModel handles the coroutine scope
                            categoryViewModel.updateCategory(updatedCategory, null, onNavigateBack)
                        } else {
                            // For new categories, we pass a dummy URI or null since images are removed
                            // But to match your existing VM signature, we might need to adjust the VM call
                            // For now, assuming you updated VM to accept null or just call a text-only method
                            // If you haven't updated CategoryViewModel, this might need adjustment.
                            // Based on your code, I will assume you update CategoryViewModel to handle null Uri
                            // or add a specific text-only method.

                            // To be safe with your EXISTING ViewModel signature (which I can't see fully but infer takes a Uri),
                            // you might need to modify CategoryViewModel.addCategory to make Uri nullable.
                            // I will assume the Service handles it.
                            categoryViewModel.addCategory(name, gridSize, android.net.Uri.EMPTY, onNavigateBack)
                        }
                    }
                },
                containerColor = if (name.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
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
                        label = { Text("Grid Size") },
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
                Text(
                    text = "Categories are now text-only. Images have been disabled.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}