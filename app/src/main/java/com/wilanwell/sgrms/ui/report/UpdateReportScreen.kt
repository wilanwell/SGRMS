package com.wilanwell.sgrms.ui.report

import com.wilanwell.sgrms.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wilanwell.sgrms.ui.components.SgrmsButtonRow
import com.wilanwell.sgrms.ui.components.SgrmsEmptyStateCard
import com.wilanwell.sgrms.ui.components.SgrmsFormCard
import com.wilanwell.sgrms.ui.components.SgrmsTextField
import com.wilanwell.sgrms.ui.components.TopBar
import com.wilanwell.sgrms.ui.theme.ColorBackground
import com.wilanwell.sgrms.ui.theme.ColorPrimary
import com.wilanwell.sgrms.viewmodel.GradeFirebaseViewModel
import com.wilanwell.sgrms.viewmodel.ReportRemarksViewModel
import com.wilanwell.sgrms.viewmodel.StudentFirebaseViewModel
import com.wilanwell.sgrms.viewmodel.SubjectFirebaseViewModel
import com.wilanwell.sgrms.utils.GradeCalculator
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.wilanwell.sgrms.ui.theme.SgrmsColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.imePadding

@Composable
fun UpdateReportScreen(
    reportId: String = "",
    onBackClick: () -> Unit = {},
    onSaveClick: () -> Unit = {}
) {
    val theSelectedReportCouldNotBeText = stringResource(R.string.the_selected_report_could_not_be)
    val studentViewModel: StudentFirebaseViewModel = hiltViewModel()

    val gradeViewModel: GradeFirebaseViewModel = hiltViewModel()

    val subjectViewModel: SubjectFirebaseViewModel = hiltViewModel()

    val studentUiState by studentViewModel.uiState.collectAsState()
    val gradeUiState by gradeViewModel.uiState.collectAsState()
    val subjectUiState by subjectViewModel.uiState.collectAsState()

    val remarksViewModel: ReportRemarksViewModel = hiltViewModel()
    val remarksUiState by remarksViewModel.uiState.collectAsState()

    var remarks by remember { mutableStateOf("") }
    var remarksError by remember { mutableStateOf(false) }
    val localMessage = remarksUiState.successMessage
    val localErrorMessage = remarksUiState.errorMessage
    val isSaving = remarksUiState.isSaving

    // Fill the field once the saved remarks have loaded.
    LaunchedEffect(remarksUiState.remarks) {
        if (remarks.isBlank()) remarks = remarksUiState.remarks
    }

    LaunchedEffect(remarksUiState.saved) {
        if (remarksUiState.saved) onSaveClick()
    }

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

    val completedGrades = allGrades.filter { grade ->
        grade.status.equals("Complete", ignoreCase = true)
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

    val isLoading =
        studentUiState.isLoading ||
                gradeUiState.isLoading ||
                subjectUiState.isLoading ||
                isSaving

    val errorMessage = when {
        localErrorMessage.isNotBlank() -> localErrorMessage
        studentUiState.errorMessage.isNotBlank() -> studentUiState.errorMessage
        gradeUiState.errorMessage.isNotBlank() -> gradeUiState.errorMessage
        subjectUiState.errorMessage.isNotBlank() -> subjectUiState.errorMessage
        else -> ""
    }

    fun clearMessages() {
        remarksError = false
        remarksViewModel.clearMessages()
        studentViewModel.clearMessage()
        gradeViewModel.clearMessage()
        subjectViewModel.clearMessage()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorBackground)
            .imePadding()
    ) {
        TopBar(
            title = stringResource(R.string.update_report),
            showBack = true,
            onBackClick = onBackClick,
            showIcon = false
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = 32.dp
                )
        ) {
            when {
                isLoading && student == null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = ColorPrimary
                        )
                    }
                }

                reportId.isBlank() || student == null -> {
                    SgrmsEmptyStateCard(
                        icon = Icons.Default.Summarize,
                        title = stringResource(R.string.report_not_found),
                        message = if (errorMessage.isNotBlank()) {
                            errorMessage
                        } else {
                            theSelectedReportCouldNotBeText
                        }
                    )
                }

                else -> {
                    SgrmsFormCard(
                        title = stringResource(R.string.report_information),
                        subtitle = stringResource(R.string.update_additional_report_remarks),
                        icon = Icons.Default.Edit
                    ) {
                        SgrmsTextField(
                            value = student.studentName.ifBlank { stringResource(R.string.unknown_student) },
                            onValueChange = {},
                            label = stringResource(R.string.student_name),
                            placeholder = stringResource(R.string.student_name_2),
                            readOnly = true
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        SgrmsTextField(
                            value = student.className.ifBlank { stringResource(R.string.not_assigned) },
                            onValueChange = {},
                            label = stringResource(R.string.class_label),
                            placeholder = stringResource(R.string.class_label),
                            readOnly = true
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        SgrmsTextField(
                            value = completedGrades.size.toString(),
                            onValueChange = {},
                            label = stringResource(R.string.completed_subjects),
                            placeholder = stringResource(R.string.completed_subjects_2),
                            readOnly = true
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        SgrmsTextField(
                            value = "%.2f".format(totalMarks),
                            onValueChange = {},
                            label = stringResource(R.string.total_marks),
                            placeholder = stringResource(R.string.total_marks_2),
                            readOnly = true
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        SgrmsTextField(
                            value = "%.2f%%".format(averageMarks),
                            onValueChange = {},
                            label = stringResource(R.string.average_marks),
                            placeholder = stringResource(R.string.average_marks_2),
                            readOnly = true
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        SgrmsTextField(
                            value = overallGrade,
                            onValueChange = {},
                            label = stringResource(R.string.overall_grade_2),
                            placeholder = stringResource(R.string.overall_grade),
                            readOnly = true
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        SgrmsTextField(
                            value = remarks,
                            onValueChange = {
                                remarks = it
                                clearMessages()
                            },
                            label = stringResource(R.string.additional_remarks),
                            placeholder = stringResource(R.string.enter_additional_report_remarks),
                            singleLine = false,
                            modifier = Modifier.height(130.dp),
                            isError = remarksError,
                            errorMessage = stringResource(R.string.remarks_are_required)
                        )

                        if (errorMessage.isNotBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = errorMessage,
                                color = SgrmsColors.Error,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        if (localMessage.isNotBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = localMessage,
                                color = ColorPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }


                    }
                }
            }
        }

        Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 8.dp) {
            SgrmsButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                                        cancelText = stringResource(R.string.cancel),
                                        confirmText = if (isSaving) {
                                            "Saving..."
                                        } else {
                                            "Save"
                                        },
                                        onCancelClick = onBackClick,
                                        onConfirmClick = {
                                            remarksError = remarks.isBlank()
            
                                            if (!remarksError) {
                                                remarksViewModel.saveRemarks(
                                                    studentId = reportId,
                                                    remarks = remarks
                                                )
                                            }
                                        }
                                    )
        }
    }
}