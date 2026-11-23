package com.wilanwell.sgrms.data.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class ReportModel(
    val reportId: String = "",
    val studentId: String = "",
    val userId: String = "",
    val totalMarks: Double = 0.0,
    val averageMarks: Double = 0.0,
    val overallGrade: String = "",
    val remarks: String = "",
    val generatedDate: String = "",
    val createdAt: Long = System.currentTimeMillis()
)