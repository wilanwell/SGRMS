package com.example.sgrms.data.model

data class StudentModel(
    val studentId: String = "",
    val studentCode: String = "",
    val studentName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val classId: String = "",
    val className: String = "",
    val userId: String = "",
    val gender: String = "",
    val status: String = "Active",

    /** Download URL of the photo in Firebase Storage. */
    val profileImageUrl: String = "",

    /** Legacy: photos saved before the move to Firebase Storage. */
    val profileImageBase64: String = "",

    val dateOfBirth: String = "",
    val address: String = "",
    val guardianName: String = "",
    val guardianPhone: String = "",
    val emergencyContact: String = "",
    val profileNote: String = "",

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)