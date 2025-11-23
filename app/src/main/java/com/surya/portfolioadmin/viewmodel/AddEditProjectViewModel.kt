package com.surya.portfolioadmin.viewmodel

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.surya.portfolioadmin.data.Category
import com.surya.portfolioadmin.data.Project
import com.surya.portfolioadmin.FirestoreService
import kotlinx.coroutines.launch

class AddEditProjectViewModel : ViewModel() {
    var project by mutableStateOf<Project?>(null)
    var title by mutableStateOf("")
    var description by mutableStateOf("")
    var selectedCategoryId by mutableStateOf("")
    var layout by mutableStateOf("regular")
    var selectedImageUri by mutableStateOf<Uri?>(null)

    var uiState by mutableStateOf<UiState>(UiState.Idle)
        private set

    // New state to hold the list of categories for the dropdown
    var categories by mutableStateOf<List<Category>>(emptyList())

    init {
        // Fetch categories as soon as the ViewModel is created
        viewModelScope.launch {
            categories = FirestoreService.getCategories()
        }
    }

    fun loadProject(projectId: String?) {
        if (projectId == null) return
        viewModelScope.launch {
            uiState = UiState.Loading
            val loadedProject = FirestoreService.getProject(projectId)
            if (loadedProject != null) {
                project = loadedProject
                title = loadedProject.title
                description = loadedProject.description
                selectedCategoryId = loadedProject.category // This is now an ID
                layout = loadedProject.layout
                uiState = UiState.Success("Project loaded")
            } else {
                uiState = UiState.Error("Failed to load project")
            }
        }
    }

    fun saveProject(onSuccess: () -> Unit) {
        // Prevent saving if no category is selected
        if (selectedCategoryId.isBlank()) {
            uiState = UiState.Error("Please select a category.")
            return
        }

        viewModelScope.launch {
            uiState = UiState.Loading
            val isSuccess = if (project == null) {
                val newProject = Project(title = title, description = description, category = selectedCategoryId, layout = layout)
                FirestoreService.addProject(newProject, selectedImageUri)
            } else {
                val updatedProject = project!!.copy(title = title, description = description, category = selectedCategoryId, layout = layout)
                FirestoreService.updateProjectWithImage(updatedProject, selectedImageUri)
            }

            if (isSuccess) {
                uiState = UiState.Success("Project saved!")
                onSuccess()
            } else {
                uiState = UiState.Error("Failed to save project")
            }
        }
    }
}