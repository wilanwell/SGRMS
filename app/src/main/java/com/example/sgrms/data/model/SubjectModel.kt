package com.example.sgrms.data.model

data class SubjectModel(
    val subjectId: String = "",
    val subjectCode: String = "",
    val subjectName: String = "",
    val teacherId: String = "",
    val teacherName: String = "",
    val category: String = "",
    val createdAt: Long = System.currentTimeMillis()
)