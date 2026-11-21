package com.example.sgrms.data.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class UserModel(
    val userId: String = "",
    val fullName: String = "",
    val username: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val role: String = "",

    val studentId: String = "",
    val teacherId: String = "",

    /** Download URL of the photo in Firebase Storage. */
    val profileImageUrl: String = "",

    /** Legacy: photos saved before the move to Firebase Storage. */
    val profileImageBase64: String = "",

    val address: String = "",
    val profileNote: String = "",
    val status: String = "Active",

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)