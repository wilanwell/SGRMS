package com.wilanwell.sgrms.data.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class ClassModel(
    val classId: String = "",
    val className: String = "",
    val academicYear: String = "",
    val classTeacherId: String = "",
    val classTeacherName: String = "",
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis()
)