package com.surya.portfolioadmin.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.surya.portfolioadmin.data.StorageItem
import com.surya.portfolioadmin.FirestoreService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AssetViewModel : ViewModel() {

    private val _storageItems = MutableStateFlow<List<StorageItem>>(emptyList())
    val storageItems = _storageItems.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _currentPath = MutableStateFlow("")
    val currentPath = _currentPath.asStateFlow()

    init {
        loadItems()
    }

    private fun refreshCurrentFolder() {
        loadItems(_currentPath.value)
    }

    fun loadItems(path: String = "") {
        viewModelScope.launch {
            _isLoading.value = true
            _currentPath.value = path
            _storageItems.value = FirestoreService.listStorageItems(path)
            _isLoading.value = false
        }
    }

    fun uploadFile(uri: Uri, fileName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            FirestoreService.uploadAssetFile(_currentPath.value, uri, fileName)
            refreshCurrentFolder()
        }
    }

    fun createFolder(folderName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            FirestoreService.createFolder(_currentPath.value, folderName)
            refreshCurrentFolder()
        }
    }

    fun deleteItem(item: StorageItem) {
        viewModelScope.launch {
            _isLoading.value = true
            FirestoreService.deleteStorageItem(item)
            refreshCurrentFolder()
        }
    }

    fun renameItem(item: StorageItem, newName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            FirestoreService.renameStorageItem(item, newName)
            refreshCurrentFolder()
        }
    }

    fun moveItem(item: StorageItem, newPath: String) {
        viewModelScope.launch {
            _isLoading.value = true
            // For simplicity, we assume newPath is a valid folder path.
            // A real app might have a folder picker UI.
            val finalPath = "$newPath/${item.ref.name}"
            FirestoreService.moveStorageItem(item, finalPath)
            refreshCurrentFolder()
        }
    }

    fun navigateUp() {
        if (_currentPath.value.isNotEmpty()) {
            val parentPath = _currentPath.value.trimEnd('/').substringBeforeLast('/', "")
            loadItems(parentPath)
        }
    }
}
