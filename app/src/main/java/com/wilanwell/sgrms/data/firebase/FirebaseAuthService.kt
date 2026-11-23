package com.wilanwell.sgrms.data.firebase

import com.wilanwell.sgrms.utils.UserMessageException
import com.wilanwell.sgrms.R
import com.wilanwell.sgrms.data.model.UserModel
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthService @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) {
    private val usersCollection = firestore.collection("users")

    suspend fun loginUser(
        email: String,
        password: String
    ): UserModel {
        val authResult = firebaseAuth
            .signInWithEmailAndPassword(email, password)
            .await()

        val firebaseUser = authResult.user
            ?: throw UserMessageException(R.string.login_failed_user_not_found)

        val userDocument = usersCollection
            .document(firebaseUser.uid)
            .get()
            .await()

        val userModel = userDocument.toObject(UserModel::class.java)
            ?: throw UserMessageException(R.string.user_profile_not_found)

        return userModel.copy(
            userId = firebaseUser.uid
        )
    }

    suspend fun getCurrentUserProfile(): UserModel? {
        val firebaseUser = firebaseAuth.currentUser ?: return null

        val userDocument = usersCollection
            .document(firebaseUser.uid)
            .get()
            .await()

        return userDocument.toObject(UserModel::class.java)?.copy(
            userId = firebaseUser.uid
        )
    }

    fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }

    fun isUserLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    fun logoutUser() {
        firebaseAuth.signOut()
    }

    /** Asks Firebase Auth to e-mail a password reset link. */
    suspend fun sendPasswordResetEmail(email: String) {
        firebaseAuth.sendPasswordResetEmail(email).await()
    }

    /** Re-authenticates with the current password, then sets a new one. */
    suspend fun changePassword(
        currentPassword: String,
        newPassword: String
    ) {
        val firebaseUser = firebaseAuth.currentUser
        val email = firebaseUser?.email

        if (firebaseUser == null || email.isNullOrBlank()) {
            throw UserMessageException(R.string.user_session_not_found_please_log)
        }

        firebaseUser
            .reauthenticate(EmailAuthProvider.getCredential(email, currentPassword))
            .await()

        firebaseUser
            .updatePassword(newPassword)
            .await()
    }
}