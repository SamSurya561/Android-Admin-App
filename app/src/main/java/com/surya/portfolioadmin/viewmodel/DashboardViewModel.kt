package com.surya.portfolioadmin.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.surya.portfolioadmin.data.Project
import com.surya.portfolioadmin.FirestoreService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DashboardViewModel : ViewModel() {
    private val db = Firebase.firestore

    private val _projects = MutableStateFlow<List<Project>>(emptyList())
    val projects = _projects.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    init {
        fetchProjects()
    }

    private fun fetchProjects() {
        _isLoading.value = true
        db.collection("projects")
            .orderBy("lastUpdated", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("DashboardViewModel", "Listen failed.", e)
                    _isLoading.value = false
                    return@addSnapshotListener
                }

                snapshot?.let {
                    _projects.value = it.documents.mapNotNull { doc ->
                        doc.toObject(Project::class.java)?.copy(id = doc.id)
                    }
                }
                _isLoading.value = false
            }
    }

    fun deleteProject(project: Project) {
        viewModelScope.launch {
            val isSuccess = FirestoreService.deleteProject(project)
            if (!isSuccess) {
                Log.e("DashboardViewModel", "Failed to delete project: ${project.id}")
            }
        }
    }
}
