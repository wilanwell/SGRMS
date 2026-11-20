package com.example.sgrms.data.repository

import com.example.sgrms.data.firebase.FirebaseAuthService
import com.example.sgrms.data.model.UserModel

class AuthRepository(
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
}