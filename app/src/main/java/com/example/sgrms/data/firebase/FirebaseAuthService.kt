package com.example.sgrms.data.firebase

import com.example.sgrms.data.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseAuthService {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val usersCollection = firestore.collection("users")

    suspend fun loginUser(
        email: String,
        password: String
    ): UserModel {
        val authResult = firebaseAuth
            .signInWithEmailAndPassword(email, password)
            .await()

        val firebaseUser = authResult.user
            ?: throw Exception("Login failed. User not found.")

        val userDocument = usersCollection
            .document(firebaseUser.uid)
            .get()
            .await()

        val userModel = userDocument.toObject(UserModel::class.java)
            ?: throw Exception("User profile not found.")

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
}