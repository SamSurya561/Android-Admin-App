package com.surya.portfolioadmin

import android.net.Uri
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.surya.portfolioadmin.data.AppUpdateInfo
import com.surya.portfolioadmin.data.Category
import com.surya.portfolioadmin.data.Project
import com.surya.portfolioadmin.data.Skill
import com.surya.portfolioadmin.data.StorageItem
import com.surya.portfolioadmin.data.WebsiteSettings
import kotlinx.coroutines.tasks.await
import java.util.Date

object FirestoreService {

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    // --- Project Management ---
    suspend fun getProjects(): List<Project> {
        return try {
            val snapshot = db.collection("projects")
                .orderBy("lastUpdated", Query.Direction.DESCENDING)
                .get().await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Project::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getProject(projectId: String): Project? {
        return try {
            val doc = db.collection("projects").document(projectId).get().await()
            doc.toObject(Project::class.java)?.copy(id = doc.id)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun addProject(project: Project, imageUri: Uri?): Boolean {
        return try {
            if (imageUri == null) return false
            val docRef = db.collection("projects").document()
            val imageUrl = storage.reference.child("project-images/${docRef.id}.jpg")
                .putFile(imageUri).await().storage.downloadUrl.await().toString()
            val newProject = project.copy(id = docRef.id, imageUrl = imageUrl, lastUpdated = Date())
            docRef.set(newProject).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateProjectWithImage(project: Project, newImageUri: Uri?): Boolean {
        return try {
            var imageUrl = project.imageUrl
            if (newImageUri != null) {
                imageUrl = storage.reference.child("project-images/${project.id}.jpg")
                    .putFile(newImageUri).await().storage.downloadUrl.await().toString()
            }
            val updatedProject = project.copy(imageUrl = imageUrl, lastUpdated = Date())
            db.collection("projects").document(project.id).set(updatedProject).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteProject(project: Project): Boolean {
        return try {
            if (project.imageUrl.isNotBlank()) {
                storage.getReferenceFromUrl(project.imageUrl).delete().await()
            }
            db.collection("projects").document(project.id).delete().await()
            true
        } catch (e: Exception) {
            try {
                db.collection("projects").document(project.id).delete().await()
                true
            } catch (_: Exception) {
                false
            }
        }
    }

    // --- Category Management ---
    suspend fun getCategories(): List<Category> {
        return try {
            val snapshot = db.collection("categories")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get().await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Category::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addCategory(category: Category, imageUri: Uri): Boolean {
        return try {
            val docRef = db.collection("categories").document()
            val imageUrl = storage.reference.child("category-images/${docRef.id}.jpg")
                .putFile(imageUri).await().storage.downloadUrl.await().toString()
            val newCategory = category.copy(id = docRef.id, imageUrl = imageUrl, createdAt = Date())
            docRef.set(newCategory).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateCategory(category: Category, newImageUri: Uri?): Boolean {
        return try {
            var imageUrl = category.imageUrl
            if (newImageUri != null) {
                imageUrl = storage.reference.child("category-images/${category.id}.jpg")
                    .putFile(newImageUri).await().storage.downloadUrl.await().toString()
            }
            val updatedCategory = category.copy(imageUrl = imageUrl)
            db.collection("categories").document(category.id).set(updatedCategory).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteCategory(category: Category): Boolean {
        return try {
            if (category.imageUrl.isNotBlank()) {
                storage.getReferenceFromUrl(category.imageUrl).delete().await()
            }
            db.collection("categories").document(category.id).delete().await()
            true
        } catch (e: Exception) {
            try {
                db.collection("categories").document(category.id).delete().await()
                true
            } catch (_: Exception) {
                false
            }
        }
    }

    // --- Website Settings ---
    suspend fun getSettings(): WebsiteSettings? {
        return try {
            db.collection("settings").document("website").get().await()
                .toObject(WebsiteSettings::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateImageUrl(fieldName: String, uri: Uri): String? {
        return try {
            val ref = storage.reference.child("settings/${fieldName}.jpg")
            ref.putFile(uri).await()
            val url = ref.downloadUrl.await().toString()
            db.collection("settings").document("website")
                .update("${fieldName}ImageUrl", url).await()
            url
        } catch (e: Exception) {
            null
        }
    }

    suspend fun uploadCvFile(uri: Uri): String? {
        return try {
            val ref = storage.reference.child("settings/resume.pdf")
            ref.putFile(uri).await()
            ref.downloadUrl.await().toString()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateCvUrl(newUrl: String): Boolean {
        return try {
            db.collection("settings").document("website")
                .update("cvUrl", newUrl).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateSkills(skills: List<Skill>): Boolean {
        return try {
            db.collection("settings").document("website")
                .update("skills", skills).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // --- App Update Function ---
    suspend fun getLatestAppVersion(): AppUpdateInfo? {
        return try {
            db.collection("app_info").document("latest_version").get().await()
                .toObject(AppUpdateInfo::class.java)
        } catch (e: Exception) {
            null
        }
    }

    // --- Asset Manager Functions ---
    suspend fun listStorageItems(path: String): List<StorageItem> {
        return try {
            val storageRef = if (path.isEmpty()) storage.reference else storage.reference.child(path)
            val listResult = storageRef.listAll().await()
            val folders = listResult.prefixes.map { StorageItem.Folder(it, it.path) }
            val files = listResult.items.map { ref ->
                val downloadUrl = try { ref.downloadUrl.await().toString() } catch (e: Exception) { "" }
                StorageItem.File(ref, ref.path, downloadUrl)
            }
            (folders + files).sortedBy { it.path }
        } catch (e: Exception) {
            Log.e("FirestoreService", "Error listing storage items at path: '$path'", e)
            emptyList()
        }
    }

    suspend fun uploadAssetFile(path: String, uri: Uri, fileName: String): Boolean {
        return try {
            storage.reference.child(path).child(fileName).putFile(uri).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun createFolder(path: String, folderName: String): Boolean {
        return try {
            val placeholderFile = storage.reference.child(path).child(folderName).child(".placeholder")
            placeholderFile.putBytes(byteArrayOf()).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteStorageItem(item: StorageItem): Boolean {
        return try {
            if (item is StorageItem.Folder) {
                val itemsInFolder = listStorageItems(item.path)
                itemsInFolder.forEach { deleteStorageItem(it) }
            }
            item.ref.delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // --- THIS IS THE FIX ---
    // This function now uses streams, which is much more memory-efficient
    // and prevents crashes with large files.
    private suspend fun copyFile(source: StorageReference, destination: StorageReference): Boolean {
        return try {
            val stream = source.stream.await().stream
            destination.putStream(stream).await()
            true
        } catch (e: Exception) {
            Log.e("FirestoreService", "Error copying file from ${source.path} to ${destination.path}", e)
            false
        }
    }

    suspend fun renameStorageItem(item: StorageItem, newName: String): Boolean {
        val parentPath = item.ref.parent?.path ?: ""
        val newPath = if (parentPath.isEmpty()) newName else "$parentPath/$newName"
        val newRef = storage.reference.child(newPath)

        return try {
            if (item is StorageItem.File) {
                if (copyFile(item.ref, newRef)) {
                    item.ref.delete().await()
                    true
                } else {
                    false
                }
            } else {
                val itemsToMove = listStorageItems(item.path)
                for (subItem in itemsToMove) {
                    val subItemNewPath = subItem.path.replaceFirst(item.path, newPath)
                    moveStorageItem(subItem, subItemNewPath)
                }
                try { item.ref.child(".placeholder").delete().await() } catch (_: Exception) {}
                true
            }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun moveStorageItem(item: StorageItem, newPath: String): Boolean {
        val newRef = storage.reference.child(newPath)
        return try {
            if (item is StorageItem.File) {
                if (copyFile(item.ref, newRef)) {
                    item.ref.delete().await()
                    true
                } else {
                    false
                }
            } else { // It's a folder
                val itemsToMove = listStorageItems(item.path)
                for (subItem in itemsToMove) {
                    val subItemNewPath = subItem.path.replaceFirst(item.path, newPath)
                    moveStorageItem(subItem, subItemNewPath)
                }
                try { item.ref.child(".placeholder").delete().await() } catch (_: Exception) {}
                true
            }
        } catch (e: Exception) {
            false
        }
    }
}
