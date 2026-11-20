package com.example.sgrms.data.repository

import com.example.sgrms.data.firebase.AccountFunctionsService
import com.example.sgrms.data.model.UserModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/** Personal fields a user may edit on their own users/{uid} profile. */
data class UserProfileUpdate(
    val fullName: String,
    val username: String,
    val phoneNumber: String,
    val address: String,
    val profileNote: String
)

@Singleton
class UserFirebaseRepository @Inject constructor(
    firestore: FirebaseFirestore,
    private val accountFunctions: AccountFunctionsService
) {
    private val usersCollection = firestore.collection("users")

    suspend fun getAllUsers(): List<UserModel> {
        return usersCollection
            .get()
            .await()
            .documents
            .mapNotNull { document ->
                document.toObject(UserModel::class.java)?.copy(userId = document.id)
            }
            .sortedBy { it.fullName.lowercase() }
    }

    /** Deletes the login account, profile and related records (Cloud Function). */
    suspend fun deleteAccount(userId: String) {
        accountFunctions.deleteAccount(userId = userId)
    }

    suspend fun getUser(userId: String): UserModel? {
        if (userId.isBlank()) return null

        val document = usersCollection.document(userId).get().await()
        return document.toObject(UserModel::class.java)?.copy(userId = document.id)
    }

    suspend fun updateOwnProfile(
        userId: String,
        update: UserProfileUpdate
    ) {
        usersCollection
            .document(userId)
            .update(
                mapOf(
                    "fullName" to update.fullName,
                    "username" to update.username,
                    "phoneNumber" to update.phoneNumber,
                    "address" to update.address,
                    "profileNote" to update.profileNote,
                    "updatedAt" to System.currentTimeMillis()
                )
            )
            .await()
    }

    /** Pass an empty [imageUrl] to remove the photo. */
    suspend fun updateProfileImage(
        userId: String,
        imageUrl: String
    ) {
        usersCollection
            .document(userId)
            .update(profileImageFields(imageUrl))
            .await()
    }
}

/** Fields written when a photo changes; also clears the legacy Base64 copy. */
internal fun profileImageFields(imageUrl: String): Map<String, Any> = mapOf(
    "profileImageUrl" to imageUrl,
    "profileImageBase64" to "",
    "updatedAt" to System.currentTimeMillis()
)
