package com.wilanwell.sgrms.ui.report

import com.wilanwell.sgrms.data.model.StudentModel
import com.wilanwell.sgrms.utils.ReportPdfData
import com.wilanwell.sgrms.utils.ReportPdfGradeItem

data class ReportDisplayItem(
    val report: ReportPdfData,
    val student: StudentModel,
    val gradeItems: List<ReportPdfGradeItem>,
    val studentName: String,
    val studentCode: String,
    val className: String,
    val status: String
)

