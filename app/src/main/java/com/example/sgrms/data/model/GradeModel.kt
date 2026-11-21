package com.example.sgrms.data.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class GradeModel(
    val gradeId: String = "",
    val studentId: String = "",
    val subjectId: String = "",
    val marks: Double = 0.0,
    val gradeLetter: String = "",
    val status: String = "",
    val remarks: String = "",
    val createdByTeacherId: String = "",
    val createdAt: Long = System.currentTimeMillis()
)