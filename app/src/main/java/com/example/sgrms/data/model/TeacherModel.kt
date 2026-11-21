package com.example.sgrms.data.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class TeacherModel(
    val teacherId: String = "",
    val staffId: String = "",
    val teacherName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val department: String = "",
    val position: String = "Teacher",
    val userId: String = "",

    /** Download URL of the photo in Firebase Storage. */
    val profileImageUrl: String = "",

    /** Legacy: photos saved before the move to Firebase Storage. */
    val profileImageBase64: String = "",

    val officeLocation: String = "",
    val qualification: String = "",
    val specialization: String = "",
    val address: String = "",
    val profileNote: String = "",

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)