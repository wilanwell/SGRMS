package com.example.sgrms.ui.grade

import com.example.sgrms.data.model.GradeModel

data class GradeDisplayItem(
    val grade: GradeModel,
    val studentName: String,
    val className: String,
    val subjectName: String,
    val subjectCode: String
)

