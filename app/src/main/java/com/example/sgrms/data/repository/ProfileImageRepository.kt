package com.example.sgrms.data.repository

import android.content.Context
import android.net.Uri
import com.example.sgrms.utils.compressImageUri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.StorageMetadata
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Stores profile photos in Firebase Storage at
 * `profile_images/{userId}.jpg` (see storage.rules). Firestore keeps only
 * the download URL, instead of the whole image as Base64.
 */
@Singleton
class ProfileImageRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val storage: FirebaseStorage
) {

    /** Compresses and uploads the photo; returns its download URL. */
    suspend fun uploadProfileImage(
        userId: String,
        imageUri: Uri
    ): String {
        require(userId.isNotBlank()) { "User account is not linked." }

        val bytes = withContext(Dispatchers.IO) {
            compressImageUri(context = context, imageUri = imageUri)
        }

        val reference = storage.reference.child(pathFor(userId))
        val metadata = StorageMetadata.Builder()
            .setContentType("image/jpeg")
            .build()

        reference.putBytes(bytes, metadata).await()

        return reference.downloadUrl.await().toString()
    }

    suspend fun deleteProfileImage(userId: String) {
        if (userId.isBlank()) return

        try {
            storage.reference.child(pathFor(userId)).delete().await()
        } catch (e: StorageException) {
            if (e.errorCode != StorageException.ERROR_OBJECT_NOT_FOUND) throw e
        }
    }

    private fun pathFor(userId: String) = "profile_images/$userId.jpg"
}
