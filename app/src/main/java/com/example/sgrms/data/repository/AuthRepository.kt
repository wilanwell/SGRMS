package com.example.sgrms.data.repository

import com.example.sgrms.data.firebase.FirebaseAuthService
import com.example.sgrms.data.model.UserModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val firebaseAuthService: FirebaseAuthService
) {
    suspend fun loginUser(
        email: String,
        password: String
    ): UserModel {
        return firebaseAuthService.loginUser(
            email = email,
            password = password
        )
    }

    suspend fun getCurrentUserProfile(): UserModel? {
        return firebaseAuthService.getCurrentUserProfile()
    }

    fun getCurrentUserId(): String? {
        return firebaseAuthService.getCurrentUserId()
    }

    fun isUserLoggedIn(): Boolean {
        return firebaseAuthService.isUserLoggedIn()
    }

    fun logoutUser() {
        firebaseAuthService.logoutUser()
    }

    suspend fun changePassword(
        currentPassword: String,
        newPassword: String
    ) {
        firebaseAuthService.changePassword(
            currentPassword = currentPassword,
            newPassword = newPassword
        )
    }
}
