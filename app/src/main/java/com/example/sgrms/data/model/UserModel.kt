package com.example.sgrms.data.model

data class UserModel(
    val userId: String = "",
    val fullName: String = "",
    val username: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val role: String = "",

    val studentId: String = "",
    val teacherId: String = "",

    val profileImageBase64: String = "",

    val address: String = "",
    val profileNote: String = "",
    val status: String = "Active",

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)