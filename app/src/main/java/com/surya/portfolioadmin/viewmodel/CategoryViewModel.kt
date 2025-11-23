package com.surya.portfolioadmin.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.surya.portfolioadmin.data.Category
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date

class CategoryViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        fetchCategories()
    }

    fun fetchCategories() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val snapshot = firestore.collection("categories").get().await()
                val categoryList = snapshot.documents.mapNotNull { doc ->
                    try {
                        val orderValue = doc.get("order")
                        val orderInt = when (orderValue) {
                            is Number -> orderValue.toInt()
                            is String -> orderValue.toIntOrNull() ?: 0
                            else -> 0
                        }

                        Category(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            gridSize = doc.getString("gridSize") ?: "medium",
                            imageUrl = doc.getString("imageUrl") ?: "",
                            order = orderInt,
                            createdAt = doc.getDate("createdAt") ?: Date()
                        )
                    } catch (e: Exception) {
                        Log.e("CategoryVM", "Error parsing category", e)
                        null
                    }
                }.sortedBy { it.getOrderAsInt() }

                _categories.value = categoryList
            } catch (e: Exception) {
                Log.e("CategoryVM", "Error fetching categories", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addCategory(name: String, gridSize: String, imageUri: Uri, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Get current max order
                val snapshot = firestore.collection("categories").get().await()
                val currentMaxOrder = snapshot.documents
                    .mapNotNull { it.get("order") }
                    .map {
                        when (it) {
                            is Number -> it.toInt()
                            is String -> it.toIntOrNull() ?: 0
                            else -> 0
                        }
                    }
                    .maxOrNull() ?: 0
                val newOrder = currentMaxOrder + 1

                // Upload image
                val imageRef = storage.reference.child("categories/${System.currentTimeMillis()}.jpg")
                imageRef.putFile(imageUri).await()
                val imageUrl = imageRef.downloadUrl.await().toString()

                // Create category
                val data = hashMapOf(
                    "name" to name,
                    "gridSize" to gridSize,
                    "imageUrl" to imageUrl,
                    "order" to newOrder,
                    "createdAt" to Date()
                )

                firestore.collection("categories").add(data).await()
                fetchCategories()
                onSuccess()
            } catch (e: Exception) {
                Log.e("CategoryVM", "Error adding category", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateCategory(category: Category, newImageUri: Uri?, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val categoryRef = firestore.collection("categories").document(category.id)

                val updatedData = hashMapOf<String, Any>(
                    "name" to category.name,
                    "gridSize" to category.gridSize,
                    "order" to category.getOrderAsInt()
                )

                if (newImageUri != null) {
                    val imageRef = storage.reference.child("categories/${System.currentTimeMillis()}.jpg")
                    imageRef.putFile(newImageUri).await()
                    updatedData["imageUrl"] = imageRef.downloadUrl.await().toString()
                }

                categoryRef.update(updatedData).await()
                fetchCategories()
                onSuccess()
            } catch (e: Exception) {
                Log.e("CategoryVM", "Error updating category", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteCategory(category: Category, onSuccess: (() -> Unit)? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                firestore.collection("categories").document(category.id).delete().await()
                fetchCategories()
                onSuccess?.invoke()
            } catch (e: Exception) {
                Log.e("CategoryVM", "Error deleting category", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
