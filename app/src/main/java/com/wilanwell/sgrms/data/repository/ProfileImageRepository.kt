package com.wilanwell.sgrms.data.repository

import android.content.Context
import android.net.Uri
import android.util.Base64
import android.util.LruCache
import com.wilanwell.sgrms.utils.compressImageUri
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Profile photos on the free Spark plan (no Cloud Storage).
 *
 * The photo is shrunk to a small JPEG (~15-30 KB) and kept in its own
 * document, profileImages/{userId}, so the student / teacher / user
 * documents that lists load stay small. The profile document stores only a
 * reference, `firestore://profileImages/{userId}?v=<time>`; the version
 * changes on every upload so cached copies are refreshed.
 */
@Singleton
class ProfileImageRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    firestore: FirebaseFirestore
) {
    private val images = firestore.collection(COLLECTION)
    private val cache = LruCache<String, ByteArray>(CACHE_ENTRIES)

    /** Compresses and saves the photo; returns the reference to store on the profile. */
    suspend fun uploadProfileImage(
        userId: String,
        imageUri: Uri
    ): String {
        require(userId.isNotBlank()) { "User account is not linked." }

        val bytes = withContext(Dispatchers.IO) {
            compressImageUri(context = context, imageUri = imageUri, maxSize = MAX_SIZE_PX, quality = JPEG_QUALITY)
        }
        val version = System.currentTimeMillis()

        images.document(userId)
            .set(
                mapOf(
                    "userId" to userId,
                    "data" to Base64.encodeToString(bytes, Base64.NO_WRAP),
                    "updatedAt" to version
                )
            )
            .await()

        val reference = referenceFor(userId, version)
        cache.put(reference, bytes)
        return reference
    }

    suspend fun deleteProfileImage(userId: String) {
        if (userId.isBlank()) return
        images.document(userId).delete().await()
    }

    /** JPEG bytes for a reference made by [uploadProfileImage], or null. */
    suspend fun loadImage(reference: String): ByteArray? {
        cache.get(reference)?.let { return it }

        val userId = userIdOf(reference) ?: return null
        val data = images.document(userId).get().await().getString("data") ?: return null
        val bytes = withContext(Dispatchers.Default) { Base64.decode(data, Base64.NO_WRAP) }

        cache.put(reference, bytes)
        return bytes
    }

    companion object {
        const val COLLECTION = "profileImages"
        private const val SCHEME = "firestore://$COLLECTION/"
        private const val MAX_SIZE_PX = 320
        private const val JPEG_QUALITY = 72
        private const val CACHE_ENTRIES = 30

        fun isFirestoreReference(reference: String) = reference.startsWith(SCHEME)

        fun referenceFor(userId: String, version: Long) = "$SCHEME$userId?v=$version"

        fun userIdOf(reference: String): String? =
            reference.takeIf(::isFirestoreReference)
                ?.removePrefix(SCHEME)
                ?.substringBefore('?')
                ?.takeIf { it.isNotBlank() }
    }
}
