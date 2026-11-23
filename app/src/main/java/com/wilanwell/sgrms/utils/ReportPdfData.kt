package com.wilanwell.sgrms.utils

import com.wilanwell.sgrms.data.model.GradeModel

// The data a PDF report is drawn from.

data class ReportPdfData(
    val reportId: String = "",
    val studentId: String = "",
    val totalMarks: Double = 0.0,
    val averageMarks: Double = 0.0,
    val overallGrade: String = "",
    val remarks: String = "",
    val generatedDate: String = "",
    val classTeacherName: String = ""
)

data class ReportPdfGradeItem(
    val grade: GradeModel,
    val subjectName: String,
    val subjectCode: String
)
