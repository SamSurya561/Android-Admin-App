package com.surya.portfolioadmin

import android.net.Uri
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.surya.portfolioadmin.data.AppUpdateInfo
import com.surya.portfolioadmin.data.Category
import com.surya.portfolioadmin.data.Project
import com.surya.portfolioadmin.data.Skill
import com.surya.portfolioadmin.data.StorageItem
import com.surya.portfolioadmin.data.WebsiteSettings
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.UUID

object FirestoreService {

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    // --- Helper Functions ---
    private suspend fun uploadImages(uris: List<Uri>): List<String> = coroutineScope {
        uris.map { uri ->
            async {
                val filename = UUID.randomUUID().toString()
                val ref = storage.reference.child("project-gallery/$filename.jpg")
                ref.putFile(uri).await()
                ref.downloadUrl.await().toString()
            }
        }.awaitAll()
    }

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

    suspend fun addProject(project: Project, coverImageUri: Uri?, galleryUris: List<Uri>): Boolean {
        return try {
            val docRef = db.collection("projects").document()
            var coverImageUrl = ""
            if (coverImageUri != null) {
                coverImageUrl = storage.reference.child("project-images/${docRef.id}.jpg")
                    .putFile(coverImageUri).await().storage.downloadUrl.await().toString()
            }
            val galleryUrls = uploadImages(galleryUris)
            val newProject = project.copy(
                id = docRef.id,
                imageUrl = coverImageUrl,
                images = galleryUrls,
                lastUpdated = Date()
            )
            docRef.set(newProject).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun updateProject(
        project: Project,
        newCoverUri: Uri?,
        newGalleryUris: List<Uri>,
        finalGalleryUrls: List<String>
    ): Boolean {
        return try {
            var coverImageUrl = project.imageUrl
            if (newCoverUri != null) {
                coverImageUrl = storage.reference.child("project-images/${project.id}.jpg")
                    .putFile(newCoverUri).await().storage.downloadUrl.await().toString()
            }
            val newUploadedUrls = uploadImages(newGalleryUris)
            val updatedGalleryList = finalGalleryUrls + newUploadedUrls
            val updatedProject = project.copy(
                imageUrl = coverImageUrl,
                images = updatedGalleryList,
                lastUpdated = Date()
            )
            db.collection("projects").document(project.id).set(updatedProject).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun deleteProject(project: Project): Boolean {
        return try {
            if (project.imageUrl.isNotBlank()) {
                try { storage.getReferenceFromUrl(project.imageUrl).delete().await() } catch (_: Exception) {}
            }
            project.images.forEach { url ->
                try { storage.getReferenceFromUrl(url).delete().await() } catch (_: Exception) {}
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

    suspend fun addCategory(category: Category): Boolean {
        return try {
            val docRef = db.collection("categories").document()
            val newCategory = category.copy(id = docRef.id, imageUrl = "", createdAt = Date())
            docRef.set(newCategory).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateCategory(category: Category): Boolean {
        return try {
            db.collection("categories").document(category.id).set(category).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteCategory(category: Category): Boolean {
        return try {
            if (category.imageUrl.isNotBlank()) {
                try { storage.getReferenceFromUrl(category.imageUrl).delete().await() } catch (_: Exception) {}
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
            e.printStackTrace()
            null
        }
    }


    // NEW: Generic update for single fields (colors, types, etc)
    suspend fun updateSettingsField(field: String, value: Any): Boolean {
        return try {
            val data = mapOf(field to value)
            db.collection("settings").document("website")
                .set(data, SetOptions.merge()).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // ... existing functions ...

    // KEEP THIS for backward compatibility if needed
    suspend fun updateImageUrl(fieldName: String, uri: Uri): String? {
        return try {
            val ref = storage.reference.child("settings/${fieldName}.jpg")
            ref.putFile(uri).await()
            val url = ref.downloadUrl.await().toString()
            val data = mapOf("${fieldName}ImageUrl" to url)
            db.collection("settings").document("website")
                .set(data, SetOptions.merge()).await()
            url
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // --- NEW: Update Image using Compressed Bytes ---
    suspend fun updateImageBytes(fieldName: String, data: ByteArray): String? {
        return try {
            // Use 'hero.jpg', 'about.jpg' etc.
            val ref = storage.reference.child("settings/${fieldName}.jpg")

            // Upload the byte array (compressed image)
            ref.putBytes(data).await()
            val url = ref.downloadUrl.await().toString()

            // Update Firestore with the new URL using merge
            val map = mapOf("${fieldName}ImageUrl" to url)
            db.collection("settings").document("website")
                .set(map, SetOptions.merge()).await()

            Log.d("FirestoreService", "Success updating $fieldName: $url")
            url
        } catch (e: Exception) {
            Log.e("FirestoreService", "Error updating image bytes", e)
            e.printStackTrace()
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

    // --- App Update & Asset Manager Functions (Unchanged) ---
    suspend fun getLatestAppVersion(): AppUpdateInfo? {
        return try {
            db.collection("app_info").document("latest_version").get().await()
                .toObject(AppUpdateInfo::class.java)
        } catch (e: Exception) {
            null
        }
    }

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

    private suspend fun copyFile(source: StorageReference, destination: StorageReference): Boolean {
        return try {
            val stream = source.stream.await().stream
            destination.putStream(stream).await()
            true
        } catch (e: Exception) {
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
}