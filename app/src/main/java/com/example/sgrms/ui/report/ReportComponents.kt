package com.example.sgrms.ui.report

import com.example.sgrms.data.model.StudentModel
import com.example.sgrms.utils.ReportPdfData
import com.example.sgrms.utils.ReportPdfGradeItem

data class ReportDisplayItem(
    val report: ReportPdfData,
    val student: StudentModel,
    val gradeItems: List<ReportPdfGradeItem>,
    val studentName: String,
    val studentCode: String,
    val className: String,
    val status: String
)

