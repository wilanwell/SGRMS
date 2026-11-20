package com.example.sgrms.data.repository

/** Personal fields a student may edit on their own profile (see firestore.rules). */
data class StudentProfileUpdate(
    val phoneNumber: String,
    val dateOfBirth: String,
    val address: String,
    val guardianName: String,
    val guardianPhone: String,
    val emergencyContact: String,
    val profileNote: String
)

/** Fields a teacher may edit on their own profile (see firestore.rules). */
data class TeacherProfileUpdate(
    val phoneNumber: String,
    val department: String,
    val position: String,
    val officeLocation: String,
    val qualification: String,
    val specialization: String,
    val address: String,
    val profileNote: String
)
