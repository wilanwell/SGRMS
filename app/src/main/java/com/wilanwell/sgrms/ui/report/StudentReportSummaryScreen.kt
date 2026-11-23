package com.wilanwell.sgrms.ui.report

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.wilanwell.sgrms.utils.ReportPdfGenerator
import com.wilanwell.sgrms.utils.ReportPdfGradeItem
import com.wilanwell.sgrms.utils.ReportPdfData
import com.wilanwell.sgrms.viewmodel.GradeFirebaseViewModel
import com.wilanwell.sgrms.viewmodel.ReportRemarksViewModel
import com.wilanwell.sgrms.viewmodel.StudentFirebaseViewModel
import com.wilanwell.sgrms.viewmodel.SubjectFirebaseViewModel
import kotlinx.coroutines.launch
import com.wilanwell.sgrms.utils.GradeCalculator
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.wilanwell.sgrms.utils.ReportCalculator
import com.wilanwell.sgrms.ui.components.SgrmsAvatar
import com.wilanwell.sgrms.ui.components.SgrmsStatusChip
import com.wilanwell.sgrms.ui.components.ChipTone
import com.wilanwell.sgrms.ui.components.toneForGrade
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Grade
import com.wilanwell.sgrms.ui.components.SgrmsDetailScreen
import com.wilanwell.sgrms.ui.components.SgrmsDetailHero
import com.wilanwell.sgrms.ui.components.SgrmsInfoSection
import com.wilanwell.sgrms.ui.components.SgrmsRowDivider
import com.wilanwell.sgrms.ui.components.SgrmsMarkRow
import com.wilanwell.sgrms.ui.components.SgrmsStatCard
import com.wilanwell.sgrms.ui.components.SgrmsPrimaryButton

@Composable
fun StudentReportSummaryScreen(
    studentId: String = "",
    onBackClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val studentViewModel: StudentFirebaseViewModel = hiltViewModel()

    val gradeViewModel: GradeFirebaseViewModel = hiltViewModel()

    val subjectViewModel: SubjectFirebaseViewModel = hiltViewModel()
    val remarksViewModel: ReportRemarksViewModel = hiltViewModel()

    val studentUiState by studentViewModel.uiState.collectAsState()
    val gradeUiState by gradeViewModel.uiState.collectAsState()
    val subjectUiState by subjectViewModel.uiState.collectAsState()

    LaunchedEffect(studentId) {
        if (studentId.isNotBlank()) {
            studentViewModel.loadStudentById(studentId)
            gradeViewModel.loadGradesByStudentId(studentId)
            subjectViewModel.loadSubjects()
        }
    }

    val student = studentUiState.selectedStudent
    val grades = gradeUiState.grades
    val subjects = subjectUiState.subjects

    val subjectItems = grades.map { grade ->
        val subject = subjects.find { subject ->
            subject.subjectId == grade.subjectId
        }

        StudentReportSubjectItem(
            grade = grade,
            subjectName = subject?.subjectName ?: "Unknown Subject",
            subjectCode = subject?.subjectCode ?: "-"
        )
    }.sortedByDescending { item ->
        item.grade.marks
    }

    val completedSubjectItems = subjectItems.filter { item ->
        item.grade.status.equals("Complete", ignoreCase = true)
    }

    val totalSubjects = completedSubjectItems.size

    val totalMarks = completedSubjectItems.sumOf { item ->
        item.grade.marks
    }

    val averageMarks = if (completedSubjectItems.isNotEmpty()) {
        totalMarks / completedSubjectItems.size
    } else {
        0.0
    }

    val overallGrade = if (completedSubjectItems.isNotEmpty()) {
        GradeCalculator.gradeLetter(averageMarks)
    } else {
        "-"
    }

    val performanceStatus = GradeCalculator.performanceStatus(
        averageMarks = averageMarks,
        hasGrades = completedSubjectItems.isNotEmpty()
    )

    val reportRemarks = ReportCalculator.buildRemarks(subjectItems.map { it.grade }) { subjectId ->
        subjectItems.firstOrNull { it.grade.subjectId == subjectId }?.subjectName
    }

    val reportDate = ReportCalculator.todayText()

    fun downloadPdf() {
        if (completedSubjectItems.isEmpty()) {
            return
        }

        coroutineScope.launch {
            val reportForPdf = ReportPdfData(
                reportId = studentId,
                studentId = studentId,
                totalMarks = totalMarks,
                averageMarks = averageMarks,
                overallGrade = overallGrade,
                remarks = reportRemarks,
                generatedDate = reportDate,
                classTeacherName = remarksViewModel.getClassTeacherName(student)
            )

            ReportPdfGenerator.saveReportAsPdf(
                context = context,
                report = reportForPdf,
                student = student,
                gradeItems = completedSubjectItems.map { item ->
                    ReportPdfGradeItem(
                        grade = item.grade,
                        subjectName = item.subjectName,
                        subjectCode = item.subjectCode
                    )
                }
            )
        }
    }

    val isLoading = studentUiState.isLoading || gradeUiState.isLoading

    SgrmsDetailScreen(
        title = "My report",
        onBack = null,
        isLoading = isLoading && student == null,
        errorMessage = gradeUiState.errorMessage.ifBlank { studentUiState.errorMessage },
        bottomBar = {
            SgrmsPrimaryButton(
                text = "Download PDF",
                icon = Icons.Default.PictureAsPdf,
                enabled = completedSubjectItems.isNotEmpty(),
                onClick = { downloadPdf() },
                modifier = Modifier.weight(1f)
            )
        }
    ) {
        SgrmsDetailHero(
            title = student?.studentName ?: "Student",
            subtitle = listOfNotNull(student?.className, "Report date $reportDate").filter { it.isNotBlank() }.joinToString(" · "),
            leading = {
                SgrmsAvatar(
                    name = student?.studentName ?: "Student",
                    size = 72.dp,
                    imageUrl = student?.profileImageUrl.orEmpty(),
                    legacyImageBase64 = student?.profileImageBase64.orEmpty()
                )
            }
        ) {
            SgrmsStatusChip(performanceStatus, if (completedSubjectItems.isEmpty()) ChipTone.Neutral else toneForGrade(overallGrade))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SgrmsStatCard(
                value = overallGrade,
                label = "Overall grade",
                icon = Icons.Default.Grade,
                tint = MaterialTheme.colorScheme.primary,
                container = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.weight(1f)
            )
            SgrmsStatCard(
                value = "%.1f%%".format(averageMarks),
                label = "$totalSubjects subjects",
                icon = Icons.Default.Assessment,
                tint = com.wilanwell.sgrms.ui.theme.SgrmsColors.Success,
                container = com.wilanwell.sgrms.ui.theme.SgrmsColors.SuccessContainer,
                modifier = Modifier.weight(1f)
            )
        }

        SgrmsInfoSection("Subjects") {
            if (subjectItems.isEmpty()) {
                Text("Your grades will appear here after your teacher adds marks.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 16.dp))
            } else {
                subjectItems.forEachIndexed { index, item ->
                    if (index > 0) SgrmsRowDivider()
                    val complete = item.grade.status.equals("Complete", ignoreCase = true)
                    SgrmsMarkRow(
                        title = item.subjectName,
                        subtitle = listOf(item.subjectCode, item.grade.remarks).filter { it.isNotBlank() }.joinToString(" · "),
                        marks = if (complete) item.grade.marks else null,
                        grade = if (complete) item.grade.gradeLetter.ifBlank { GradeCalculator.gradeLetter(item.grade.marks) } else "Pending"
                    )
                }
            }
        }

        SgrmsInfoSection("Teacher's remarks") {
            Text(reportRemarks, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 14.dp))
        }
    }
}
