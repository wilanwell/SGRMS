package com.example.sgrms.data.model

data class ClassModel(
    val classId: String = "",
    val className: String = "",
    val academicYear: String = "",
    val classTeacherId: String = "",
    val classTeacherName: String = "",
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis()
)