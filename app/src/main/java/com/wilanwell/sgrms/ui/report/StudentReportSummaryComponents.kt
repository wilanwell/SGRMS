package com.wilanwell.sgrms.ui.report

import com.wilanwell.sgrms.data.model.GradeModel

data class StudentReportSubjectItem(
    val grade: GradeModel,
    val subjectName: String,
    val subjectCode: String
)

