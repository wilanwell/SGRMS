package com.wilanwell.sgrms.ui.report

import com.wilanwell.sgrms.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.wilanwell.sgrms.utils.ReportPdfData
import com.wilanwell.sgrms.utils.ReportPdfGenerator
import com.wilanwell.sgrms.utils.ReportPdfGradeItem
import com.wilanwell.sgrms.viewmodel.GradeFirebaseViewModel
import com.wilanwell.sgrms.viewmodel.StudentFirebaseViewModel
import com.wilanwell.sgrms.viewmodel.SubjectFirebaseViewModel
import com.wilanwell.sgrms.utils.GradeCalculator
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.wilanwell.sgrms.utils.ReportCalculator
import com.wilanwell.sgrms.ui.theme.SgrmsColors
import com.wilanwell.sgrms.ui.components.ChipGroup
import com.wilanwell.sgrms.ui.components.EmptyContent
import com.wilanwell.sgrms.ui.components.ListItemPadding
import com.wilanwell.sgrms.ui.components.RowAction
import com.wilanwell.sgrms.ui.components.SgrmsAvatar
import com.wilanwell.sgrms.ui.components.SgrmsListItemCard
import com.wilanwell.sgrms.ui.components.SgrmsListScreen
import com.wilanwell.sgrms.ui.components.SgrmsOverflowMenu
import com.wilanwell.sgrms.ui.components.SgrmsStatusChip
import com.wilanwell.sgrms.ui.components.ChipTone
import com.wilanwell.sgrms.ui.components.toneForGrade
import com.wilanwell.sgrms.ui.components.SgrmsDeleteDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.EditNote
import com.wilanwell.sgrms.ui.components.SgrmsStatCard

@Composable
fun ReportScreen(
    onBackClick: () -> Unit = {},
    onViewReportDetailClick: (String) -> Unit = {},
    onDownloadPdfClick: (String) -> Unit = {},
    onUpdateReportClick: (String) -> Unit = {}
) {
    val currentText = stringResource(R.string.current)
    val needsUpdateText = stringResource(R.string.needs_update)
    val reportsAppearOnceStudentsHaveCompletedText = stringResource(R.string.reports_appear_once_students_have_completed)
    val context = LocalContext.current

    val studentViewModel: StudentFirebaseViewModel = hiltViewModel()

    val gradeViewModel: GradeFirebaseViewModel = hiltViewModel()

    val subjectViewModel: SubjectFirebaseViewModel = hiltViewModel()

    val studentUiState by studentViewModel.uiState.collectAsState()
    val gradeUiState by gradeViewModel.uiState.collectAsState()
    val subjectUiState by subjectViewModel.uiState.collectAsState()

    var searchText by remember { mutableStateOf("") }
    var selectedClass by remember { mutableStateOf("All Classes") }
    var selectedStatus by remember { mutableStateOf("All") }
    var selectedDate by remember { mutableStateOf("Latest") }


    var selectedReportToDelete by remember { mutableStateOf<ReportDisplayItem?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val hiddenReportStudentIds = remember {
        mutableStateListOf<String>()
    }

    LaunchedEffect(Unit) {
        studentViewModel.loadStudents()
        gradeViewModel.loadGrades()
        subjectViewModel.loadSubjects()
    }

    val students = studentUiState.students
    val grades = gradeUiState.grades
    val subjects = subjectUiState.subjects

    val isLoading =
        studentUiState.isLoading ||
                gradeUiState.isLoading ||
                subjectUiState.isLoading

    val errorMessage = when {
        studentUiState.errorMessage.isNotBlank() -> studentUiState.errorMessage
        gradeUiState.errorMessage.isNotBlank() -> gradeUiState.errorMessage
        subjectUiState.errorMessage.isNotBlank() -> subjectUiState.errorMessage
        else -> ""
    }

    val classList = listOf("All Classes") + students
        .map { student ->
            student.className.ifBlank { stringResource(R.string.not_assigned) }
        }
        .filter { className ->
            className.isNotBlank()
        }
        .distinct()
        .sorted()

    val statusList = listOf(
        "All",
        "Generated",
        needsUpdateText
    )

    val dateList = listOf(
        "Latest",
        "Oldest"
    )

    val today = ReportCalculator.todayText()
    val subjectNames = subjects.associate { it.subjectId to it.subjectName }

    val reportDisplayList = students.mapNotNull { student ->
        val allStudentGrades = grades.filter { grade ->
            grade.studentId == student.studentId
        }

        if (allStudentGrades.isEmpty()) {
            null
        } else {
            val completedGrades = allStudentGrades.filter { grade ->
                grade.status == "Complete"
            }

            val hasPendingGrades = allStudentGrades.any { grade ->
                grade.status != "Complete"
            }

            val gradeItems = completedGrades.map { grade ->
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

            val reportRemarks = ReportCalculator.buildRemarks(allStudentGrades) { subjectNames[it] }

            val report = ReportPdfData(
                reportId = student.studentId,
                studentId = student.studentId,
                totalMarks = totalMarks,
                averageMarks = averageMarks,
                overallGrade = overallGrade,
                remarks = reportRemarks,
                generatedDate = today
            )

            ReportDisplayItem(
                report = report,
                student = student,
                gradeItems = gradeItems,
                studentName = student.studentName.ifBlank { stringResource(R.string.unknown_student) },
                studentCode = student.studentCode.ifBlank { "-" },
                className = student.className.ifBlank { stringResource(R.string.not_assigned) },
                status = if (hasPendingGrades || completedGrades.isEmpty()) {
                    needsUpdateText
                } else {
                    "Generated"
                }
            )
        }
    }.filter { item ->
        item.student.studentId !in hiddenReportStudentIds
    }

    val sortedReports = when (selectedDate) {
        "Oldest" -> reportDisplayList.sortedBy { item ->
            item.studentName.lowercase()
        }

        else -> reportDisplayList.sortedByDescending { item ->
            item.report.generatedDate
        }.sortedBy { item ->
            item.studentName.lowercase()
        }
    }

    val filteredReports = sortedReports.filter { item ->
        val matchesSearch =
            item.studentName.contains(searchText, ignoreCase = true) ||
                    item.studentCode.contains(searchText, ignoreCase = true) ||
                    item.className.contains(searchText, ignoreCase = true) ||
                    item.report.overallGrade.contains(searchText, ignoreCase = true)

        val matchesClass =
            selectedClass == "All Classes" || item.className == selectedClass

        val matchesStatus =
            selectedStatus == "All" || item.status == selectedStatus

        matchesSearch && matchesClass && matchesStatus
    }

    val totalReports = reportDisplayList.size

    val generatedReports = reportDisplayList.filter { item ->
        item.status == "Generated"
    }

    val averageClassMark = if (generatedReports.isNotEmpty()) {
        generatedReports.map { item ->
            item.report.averageMarks
        }.average()
    } else {
        0.0
    }

    if (showDeleteDialog && selectedReportToDelete != null) {
        SgrmsDeleteDialog(
            title = stringResource(R.string.hide_this_report),
            message = stringResource(R.string.the_report_is_hidden_from_this),
            confirmText = stringResource(R.string.hide),
            onConfirmClick = {
                selectedReportToDelete?.let { hiddenReportStudentIds.add(it.student.studentId) }
                showDeleteDialog = false
                selectedReportToDelete = null
            },
            onDismissClick = {
                showDeleteDialog = false
                selectedReportToDelete = null
            }
        )
    }

    SgrmsListScreen(
        title = stringResource(R.string.reports),
        subtitle = stringResource(R.string.of_students, filteredReports.size, totalReports),
        searchText = searchText,
        onSearchChange = { searchText = it },
        searchPlaceholder = stringResource(R.string.search_student_code_or_class),
        isLoading = isLoading && students.isEmpty(),
        errorMessage = errorMessage,
        isEmpty = filteredReports.isEmpty(),
        empty = EmptyContent(stringResource(R.string.no_reports_yet), reportsAppearOnceStudentsHaveCompletedText, Icons.Default.Summarize),
        chipGroups = listOf(
            ChipGroup(statusList, selectedStatus) { selectedStatus = it },
            ChipGroup(classList, selectedClass) { selectedClass = it }
        ),
        headerActions = {
            SgrmsOverflowMenu(
                contentDescription = stringResource(R.string.sort_and_refresh),
                actions = dateList.map { option ->
                    RowAction(if (option == selectedDate) currentText.format(option) else option, Icons.AutoMirrored.Filled.Sort) { selectedDate = option }
                } + RowAction(stringResource(R.string.refresh_list), Icons.Default.Refresh) {
                    hiddenReportStudentIds.clear()
                    studentViewModel.loadStudents()
                    gradeViewModel.loadGrades()
                    subjectViewModel.loadSubjects()
                }
            )
        },
        summary = {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SgrmsStatCard(
                    value = generatedReports.size.toString(),
                    label = stringResource(R.string.reports_ready),
                    icon = Icons.Default.Summarize,
                    tint = MaterialTheme.colorScheme.primary,
                    container = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.weight(1f)
                )
                SgrmsStatCard(
                    value = "%.1f%%".format(averageClassMark),
                    label = stringResource(R.string.average_mark),
                    icon = Icons.Default.Assessment,
                    tint = com.wilanwell.sgrms.ui.theme.SgrmsColors.Success,
                    container = com.wilanwell.sgrms.ui.theme.SgrmsColors.SuccessContainer,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    ) {
        items(items = filteredReports, key = { it.report.reportId }) { item ->
            Box(ListItemPadding) {
                SgrmsListItemCard(
                    title = item.studentName,
                    subtitle = "${item.studentCode} · ${item.className} · avg ${"%.1f".format(item.report.averageMarks)}%",
                    leading = { SgrmsAvatar(name = item.studentName, size = 44.dp, imageUrl = item.student.profileImageUrl, legacyImageBase64 = item.student.profileImageBase64) },
                    onClick = { onViewReportDetailClick(item.report.reportId) }
                ) {
                    if (item.status == "Generated") {
                        SgrmsStatusChip(item.report.overallGrade.ifBlank { "-" }, toneForGrade(item.report.overallGrade))
                    } else {
                        SgrmsStatusChip(item.status, ChipTone.Warning)
                    }
                    SgrmsOverflowMenu(
                        contentDescription = stringResource(R.string.actions_for, item.studentName),
                        actions = listOf(
                            RowAction(stringResource(R.string.view_report), Icons.Default.Visibility) { onViewReportDetailClick(item.report.reportId) },
                            RowAction(stringResource(R.string.download_pdf), Icons.Default.PictureAsPdf) {
                                ReportPdfGenerator.saveReportAsPdf(
                                    context = context,
                                    report = item.report,
                                    student = item.student,
                                    gradeItems = item.gradeItems
                                )
                                onDownloadPdfClick(item.report.reportId)
                            },
                            RowAction(stringResource(R.string.update_remarks), Icons.Default.EditNote) { onUpdateReportClick(item.report.reportId) },
                            RowAction(stringResource(R.string.hide_from_list), Icons.Default.Delete, destructive = true) {
                                selectedReportToDelete = item
                                showDeleteDialog = true
                            }
                        )
                    )
                }
            }
        }
    }
}
