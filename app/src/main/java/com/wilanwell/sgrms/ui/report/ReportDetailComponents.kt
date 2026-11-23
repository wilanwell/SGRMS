package com.wilanwell.sgrms.ui.report

import com.wilanwell.sgrms.data.model.GradeModel

data class ReportGradeDisplayItem(
    val grade: GradeModel,
    val subjectName: String,
    val subjectCode: String
)

