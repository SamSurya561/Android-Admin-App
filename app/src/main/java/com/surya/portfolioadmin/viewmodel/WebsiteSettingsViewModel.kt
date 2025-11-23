package com.surya.portfolioadmin.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.surya.portfolioadmin.data.Skill
import com.surya.portfolioadmin.data.WebsiteSettings
import com.surya.portfolioadmin.FirestoreService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface UiState {
    object Idle : UiState
    object Loading : UiState
    data class Success(val message: String) : UiState
    data class Error(val message: String) : UiState
}

class WebsiteSettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val _settings = MutableStateFlow<WebsiteSettings?>(null)
    val settings = _settings.asStateFlow()

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState = _uiState.asStateFlow()

    init {
        fetchSettings()
    }

    private fun fetchSettings() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            _settings.value = FirestoreService.getSettings()
            _uiState.value = UiState.Success("Settings loaded")
        }
    }

    fun updateImage(fieldName: String, uri: Uri) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val newUrl = FirestoreService.updateImageUrl(fieldName, uri)
            if (newUrl != null) {
                fetchSettings()
                _uiState.value = UiState.Success("$fieldName image updated!")
            } else {
                _uiState.value = UiState.Error("Image update failed.")
            }
        }
    }

    fun updateCvUrl(newUrl: String) {
        viewModelScope.launch {
            if (FirestoreService.updateCvUrl(newUrl)) {
                fetchSettings()
                _uiState.value = UiState.Success("CV URL updated!")
            } else {
                _uiState.value = UiState.Error("CV URL update failed.")
            }
        }
    }

    fun uploadCvFromDevice(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val newUrl = FirestoreService.uploadCvFile(uri)
            if (newUrl != null) {
                if (FirestoreService.updateCvUrl(newUrl)) {
                    fetchSettings()
                    _uiState.value = UiState.Success("CV uploaded and updated!")
                } else {
                    _uiState.value = UiState.Error("CV URL update failed.")
                }
            } else {
                _uiState.value = UiState.Error("CV file upload failed.")
            }
        }
    }

    fun addSkill(name: String, percentage: Int, category: String) {
        val currentSkills = _settings.value?.skills.orEmpty()
        val newSkill = Skill(name, percentage, category)
        updateSkillsList(currentSkills + newSkill)
    }

    fun deleteSkill(skill: Skill) {
        val currentSkills = _settings.value?.skills.orEmpty()
        updateSkillsList(currentSkills - skill)
    }

    fun updateSkill(originalSkill: Skill, newName: String, newPercentage: Int, newCategory: String) {
        val currentSkills = _settings.value?.skills.orEmpty().toMutableList()
        val index = currentSkills.indexOf(originalSkill)
        if (index != -1) {
            val updatedSkill = originalSkill.copy(
                name = newName,
                percentage = newPercentage,
                category = newCategory
            )
            currentSkills[index] = updatedSkill
            updateSkillsList(currentSkills)
        }
    }

    private fun updateSkillsList(updatedList: List<Skill>) {
        viewModelScope.launch {
            if (FirestoreService.updateSkills(updatedList)) {
                fetchSettings()
                _uiState.value = UiState.Success("Skills updated!")
            } else {
                _uiState.value = UiState.Error("Skill update failed.")
            }
        }
    }
}
