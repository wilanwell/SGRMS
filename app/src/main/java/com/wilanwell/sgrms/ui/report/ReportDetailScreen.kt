package com.wilanwell.sgrms.ui.report

import com.wilanwell.sgrms.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.wilanwell.sgrms.ui.theme.SgrmsColors
import com.wilanwell.sgrms.utils.ReportPdfData
import com.wilanwell.sgrms.utils.ReportPdfGenerator
import com.wilanwell.sgrms.utils.ReportPdfGradeItem
import com.wilanwell.sgrms.viewmodel.GradeFirebaseViewModel
import com.wilanwell.sgrms.viewmodel.ReportRemarksViewModel
import com.wilanwell.sgrms.viewmodel.StudentFirebaseViewModel
import com.wilanwell.sgrms.viewmodel.SubjectFirebaseViewModel
import com.wilanwell.sgrms.utils.GradeCalculator
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.wilanwell.sgrms.utils.ReportCalculator
import com.wilanwell.sgrms.ui.components.SgrmsAvatar
import com.wilanwell.sgrms.ui.components.SgrmsStatusChip
import com.wilanwell.sgrms.ui.components.ChipTone
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Grade
import com.wilanwell.sgrms.ui.components.SgrmsDetailScreen
import com.wilanwell.sgrms.ui.components.SgrmsDetailHero
import com.wilanwell.sgrms.ui.components.SgrmsInfoSection
import com.wilanwell.sgrms.ui.components.SgrmsRowDivider
import com.wilanwell.sgrms.ui.components.SgrmsMarkRow
import com.wilanwell.sgrms.ui.components.SgrmsStatCard
import com.wilanwell.sgrms.ui.components.SgrmsPrimaryButton
import com.wilanwell.sgrms.ui.components.SgrmsSecondaryButton
import com.wilanwell.sgrms.ui.components.SgrmsEmptyState
import androidx.compose.material.icons.filled.Person

@Composable
fun ReportDetailScreen(
    reportId: String = "",
    onBackClick: () -> Unit = {},
    onSaveReportClick: () -> Unit = {},
    onUpdateReportClick: () -> Unit = {}
) {
    val remarkTextsValue = remarkTexts()
    val additionalRemarks2Text = stringResource(R.string.remarks_with_additional)
    val needsUpdateText = stringResource(R.string.needs_update)
    val upToDateText = stringResource(R.string.up_to_date)
    val context = LocalContext.current
    val studentViewModel: StudentFirebaseViewModel = hiltViewModel()

    val gradeViewModel: GradeFirebaseViewModel = hiltViewModel()

    val subjectViewModel: SubjectFirebaseViewModel = hiltViewModel()

    val studentUiState by studentViewModel.uiState.collectAsState()
    val gradeUiState by gradeViewModel.uiState.collectAsState()
    val subjectUiState by subjectViewModel.uiState.collectAsState()

    val remarksViewModel: ReportRemarksViewModel = hiltViewModel()
    val remarksUiState by remarksViewModel.uiState.collectAsState()
    val additionalRemarks = remarksUiState.remarks
    val localErrorMessage = remarksUiState.errorMessage

    LaunchedEffect(reportId) {
        if (reportId.isNotBlank()) {
            studentViewModel.loadStudentById(reportId)
            gradeViewModel.loadGradesByStudentId(reportId)
            subjectViewModel.loadSubjects()

            remarksViewModel.loadRemarks(reportId)
        }
    }

    val student = studentUiState.selectedStudent
    val allGrades = gradeUiState.grades
    val subjects = subjectUiState.subjects

    val completedGrades = allGrades.filter { grade ->
        grade.status.equals("Complete", ignoreCase = true)
    }

    val reportGradeList = allGrades.map { grade ->
        val subject = subjects.find { subject ->
            subject.subjectId == grade.subjectId
        }

        ReportGradeDisplayItem(
            grade = grade,
            subjectName = subject?.subjectName ?: stringResource(R.string.unknown_subject),
            subjectCode = subject?.subjectCode ?: "-"
        )
    }.sortedBy { item ->
        item.subjectName.lowercase()
    }

    val pdfGradeItems = completedGrades.map { grade ->
        val subject = subjects.find { subject ->
            subject.subjectId == grade.subjectId
        }

        ReportPdfGradeItem(
            grade = grade,
            subjectName = subject?.subjectName ?: stringResource(R.string.unknown_subject),
            subjectCode = subject?.subjectCode ?: "-"
        )
    }.sortedBy { item ->
        item.subjectName.lowercase()
    }

    val totalMarks = completedGrades.sumOf { grade ->
        grade.marks
    }

    val averageMarks = if (completedGrades.isNotEmpty()) {
        totalMarks / completedGrades.size
    } else {
        0.0
    }

    val overallGrade = if (completedGrades.isNotEmpty()) {
        GradeCalculator.gradeLetter(averageMarks)
    } else {
        "-"
    }

    val isReportOutdated = allGrades.any { grade ->
        !grade.status.equals("Complete", ignoreCase = true)
    } || completedGrades.isEmpty()

    val reportStatusText = if (isReportOutdated) {
        needsUpdateText
    } else {
        upToDateText
    }

    val reportStatusColor = if (isReportOutdated) {
        SgrmsColors.WarningBar
    } else {
        SgrmsColors.Success
    }

    val reportStatusIcon = if (isReportOutdated) {
        Icons.Default.Warning
    } else {
        Icons.Default.CheckCircle
    }

    val subjectNames = subjects.associate { it.subjectId to it.subjectName }
    val generatedRemarks = ReportCalculator.buildRemarks(allGrades, remarkTextsValue) { subjectNames[it] }

    val reportRemarks = if (additionalRemarks.isNotBlank()) {
        additionalRemarks2Text.format(generatedRemarks, additionalRemarks)
    } else {
        generatedRemarks
    }

    val report = ReportPdfData(
        reportId = reportId,
        studentId = reportId,
        totalMarks = totalMarks,
        averageMarks = averageMarks,
        overallGrade = overallGrade,
        remarks = reportRemarks,
        generatedDate = ReportCalculator.todayText()
    )

    val isLoading =
        studentUiState.isLoading ||
                gradeUiState.isLoading ||
                subjectUiState.isLoading

    val errorMessage = when {
        localErrorMessage.isNotBlank() -> localErrorMessage
        studentUiState.errorMessage.isNotBlank() -> studentUiState.errorMessage
        gradeUiState.errorMessage.isNotBlank() -> gradeUiState.errorMessage
        subjectUiState.errorMessage.isNotBlank() -> subjectUiState.errorMessage
        else -> ""
    }

    SgrmsDetailScreen(
        title = stringResource(R.string.report),
        onBack = onBackClick,
        isLoading = isLoading && student == null,
        errorMessage = errorMessage.ifBlank { localErrorMessage },
        bottomBar = if (student == null) null else {
            {
            SgrmsSecondaryButton(text = stringResource(R.string.remarks), icon = Icons.Default.EditNote, onClick = onUpdateReportClick, modifier = Modifier.weight(1f))
            SgrmsPrimaryButton(
                text = stringResource(R.string.save_pdf),
                icon = Icons.Default.PictureAsPdf,
                enabled = completedGrades.isNotEmpty(),
                onClick = {
                    ReportPdfGenerator.saveReportAsPdf(
                        context = context,
                        report = report,
                        student = student,
                        gradeItems = pdfGradeItems
                    )
                    onSaveReportClick()
                },
                modifier = Modifier.weight(1f)
            )
            }
        }
    ) {
        if (student == null) {
            SgrmsEmptyState(title = stringResource(R.string.report_not_found), message = stringResource(R.string.this_student_or_report_could_not), icon = Icons.Default.Person)
            return@SgrmsDetailScreen
        }

        SgrmsDetailHero(
            title = student?.studentName ?: stringResource(R.string.unknown_student),
            subtitle = listOfNotNull(student?.studentCode, student?.className).filter { it.isNotBlank() }.joinToString(" · "),
            leading = {
                SgrmsAvatar(
                    name = student?.studentName ?: stringResource(R.string.student),
                    size = 72.dp,
                    imageUrl = student?.profileImageUrl.orEmpty(),
                    legacyImageBase64 = student?.profileImageBase64.orEmpty()
                )
            }
        ) {
            SgrmsStatusChip(reportStatusText, if (isReportOutdated) ChipTone.Warning else ChipTone.Success)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SgrmsStatCard(
                value = overallGrade,
                label = stringResource(R.string.overall_grade),
                icon = Icons.Default.Grade,
                tint = MaterialTheme.colorScheme.primary,
                container = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.weight(1f)
            )
            SgrmsStatCard(
                value = "%.1f%%".format(averageMarks),
                label = stringResource(R.string.average),
                icon = Icons.Default.Assessment,
                tint = com.wilanwell.sgrms.ui.theme.SgrmsColors.Success,
                container = com.wilanwell.sgrms.ui.theme.SgrmsColors.SuccessContainer,
                modifier = Modifier.weight(1f)
            )
        }

        SgrmsInfoSection(stringResource(R.string.subjects)) {
            if (reportGradeList.isEmpty()) {
                Text(stringResource(R.string.no_grade_records_yet), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 16.dp))
            } else {
                reportGradeList.forEachIndexed { index, item ->
                    if (index > 0) SgrmsRowDivider()
                    val complete = item.grade.status.equals("Complete", ignoreCase = true)
                    SgrmsMarkRow(
                        title = item.subjectName,
                        subtitle = item.subjectCode,
                        marks = if (complete) item.grade.marks else null,
                        grade = if (complete) item.grade.gradeLetter.ifBlank { com.wilanwell.sgrms.utils.GradeCalculator.gradeLetter(item.grade.marks) } else "Pending"
                    )
                }
            }
        }

        SgrmsInfoSection(stringResource(R.string.remarks)) {
            Text(
                reportRemarks,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(vertical = 14.dp)
            )
        }
    }
}
