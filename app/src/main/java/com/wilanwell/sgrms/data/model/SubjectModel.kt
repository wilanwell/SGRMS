package com.wilanwell.sgrms.data.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class SubjectModel(
    val subjectId: String = "",
    val subjectCode: String = "",
    val subjectName: String = "",
    val teacherId: String = "",
    val teacherName: String = "",
    val category: String = "",
    val createdAt: Long = System.currentTimeMillis()
)