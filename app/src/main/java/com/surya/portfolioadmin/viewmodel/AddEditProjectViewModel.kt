package com.surya.portfolioadmin.viewmodel

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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

    // --- NEW FIELDS FOR IMAGES ---
    var selectedCoverImageUri by mutableStateOf<Uri?>(null)
    var existingCoverImageUrl by mutableStateOf("")

    // Gallery images
    var selectedGalleryUris = mutableStateListOf<Uri>()
    var existingGalleryUrls = mutableStateListOf<String>()

    var uiState by mutableStateOf<UiState>(UiState.Idle)
        private set

    var categories by mutableStateOf<List<Category>>(emptyList())

    init {
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
                selectedCategoryId = loadedProject.category
                layout = loadedProject.layout

                existingCoverImageUrl = loadedProject.imageUrl

                // Load existing gallery URLs
                existingGalleryUrls.clear()
                existingGalleryUrls.addAll(loadedProject.images)

                uiState = UiState.Success("Project loaded")
            } else {
                uiState = UiState.Error("Failed to load project")
            }
        }
    }

    fun addGalleryImages(uris: List<Uri>) {
        selectedGalleryUris.addAll(uris)
    }

    fun removeNewGalleryImage(uri: Uri) {
        selectedGalleryUris.remove(uri)
    }

    fun removeExistingGalleryImage(url: String) {
        existingGalleryUrls.remove(url)
    }

    fun saveProject(onSuccess: () -> Unit) {
        if (selectedCategoryId.isBlank()) {
            uiState = UiState.Error("Please select a category.")
            return
        }
        // Require at least a cover image (either new or existing)
        if (selectedCoverImageUri == null && existingCoverImageUrl.isBlank()) {
            uiState = UiState.Error("Please select a cover image.")
            return
        }

        viewModelScope.launch {
            uiState = UiState.Loading
            val isSuccess = if (project == null) {
                // Add new
                val newProject = Project(
                    title = title,
                    description = description,
                    category = selectedCategoryId,
                    layout = layout
                )
                FirestoreService.addProject(newProject, selectedCoverImageUri, selectedGalleryUris)
            } else {
                // Update existing
                val updatedProject = project!!.copy(
                    title = title,
                    description = description,
                    category = selectedCategoryId,
                    layout = layout
                )
                FirestoreService.updateProject(
                    project = updatedProject,
                    newCoverUri = selectedCoverImageUri,
                    newGalleryUris = selectedGalleryUris,
                    finalGalleryUrls = existingGalleryUrls
                )
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