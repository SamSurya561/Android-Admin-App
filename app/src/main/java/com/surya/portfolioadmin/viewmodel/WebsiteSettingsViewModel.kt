package com.surya.portfolioadmin.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.surya.portfolioadmin.data.Skill
import com.surya.portfolioadmin.data.WebsiteSettings
import com.surya.portfolioadmin.FirestoreService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

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

    // --- Image Upload Logic ---
    fun updateImage(fieldName: String, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = UiState.Loading
            val compressedBytes = compressImage(getApplication<Application>().applicationContext, uri)
            if (compressedBytes != null) {
                val newUrl = FirestoreService.updateImageBytes(fieldName, compressedBytes)
                if (newUrl != null) {
                    fetchSettings()
                    _uiState.value = UiState.Success("$fieldName image updated!")
                } else {
                    _uiState.value = UiState.Error("Upload failed. Check internet.")
                }
            } else {
                _uiState.value = UiState.Error("Could not process image.")
            }
        }
    }

    private fun compressImage(context: Context, uri: Uri): ByteArray? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val original = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            if (original == null) return null
            val maxDim = 1024
            val ratio = original.width.toFloat() / original.height.toFloat()
            val width = if (ratio > 1) maxDim else (maxDim * ratio).toInt()
            val height = if (ratio > 1) (maxDim / ratio).toInt() else maxDim
            val scaled = Bitmap.createScaledBitmap(original, width, height, true)
            val out = ByteArrayOutputStream()
            scaled.compress(Bitmap.CompressFormat.JPEG, 80, out)
            out.toByteArray()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // --- NEW: Light/Dark Mode Updates ---

    // General Type (applies to both usually, or just stores the preference)
    fun updateBackgroundType(type: String) {
        updateSettingField("backgroundType", type)
    }

    // Light Mode
    fun updateLightSolidColor(color: String) {
        updateSettingField("lightBackgroundColor", color)
    }

    fun updateLightGradient(start: String, end: String) {
        viewModelScope.launch {
            FirestoreService.updateSettingsField("lightGradientStart", start)
            FirestoreService.updateSettingsField("lightGradientEnd", end)
            fetchSettings()
        }
    }

    // Dark Mode
    fun updateDarkSolidColor(color: String) {
        updateSettingField("darkBackgroundColor", color)
    }

    fun updateDarkGradient(start: String, end: String) {
        viewModelScope.launch {
            FirestoreService.updateSettingsField("darkGradientStart", start)
            FirestoreService.updateSettingsField("darkGradientEnd", end)
            fetchSettings()
        }
    }

    fun updateAccentColor(color: String) {
        updateSettingField("accentColor", color)
    }

    private fun updateSettingField(field: String, value: Any) {
        viewModelScope.launch {
            if (FirestoreService.updateSettingsField(field, value)) {
                fetchSettings()
            } else {
                _uiState.value = UiState.Error("Failed to update $field")
            }
        }
    }

    // --- Existing Methods (CV, Skills) ---
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
                    _uiState.value = UiState.Success("CV uploaded!")
                } else {
                    _uiState.value = UiState.Error("CV update failed.")
                }
            } else {
                _uiState.value = UiState.Error("CV upload failed.")
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

    fun updateSkill(original: Skill, name: String, pct: Int, cat: String) {
        val list = _settings.value?.skills.orEmpty().toMutableList()
        val index = list.indexOf(original)
        if (index != -1) {
            list[index] = original.copy(name = name, percentage = pct, category = cat)
            updateSkillsList(list)
        }
    }

    private fun updateSkillsList(list: List<Skill>) {
        viewModelScope.launch {
            if (FirestoreService.updateSkills(list)) {
                fetchSettings()
                _uiState.value = UiState.Success("Skills updated!")
            } else {
                _uiState.value = UiState.Error("Failed to update skills.")
            }
        }
    }
}