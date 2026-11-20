package com.example.sgrms.data.model

data class SchoolClassModel(
    val classId: String = "",
    val className: String = "",
    val classTeacherId: String = "",
    val classTeacherName: String = "",
    val createdAt: Long = System.currentTimeMillis()
)