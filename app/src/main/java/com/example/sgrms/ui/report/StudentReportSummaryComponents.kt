package com.example.sgrms.ui.report

import com.example.sgrms.data.model.GradeModel

data class StudentReportSubjectItem(
    val grade: GradeModel,
    val subjectName: String,
    val subjectCode: String
)

