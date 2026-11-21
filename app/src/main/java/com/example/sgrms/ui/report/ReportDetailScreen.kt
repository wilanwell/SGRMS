package com.example.sgrms.ui.report

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sgrms.ui.components.TopBar
import com.example.sgrms.ui.theme.ColorBackground
import com.example.sgrms.ui.theme.ColorSurface
import com.example.sgrms.ui.theme.ColorPrimary
import com.example.sgrms.ui.theme.SgrmsColors
import com.example.sgrms.ui.theme.ColorOnSurfaceVariant
import com.example.sgrms.utils.ReportPdfData
import com.example.sgrms.utils.ReportPdfGenerator
import com.example.sgrms.utils.ReportPdfGradeItem
import com.example.sgrms.viewmodel.GradeFirebaseViewModel
import com.example.sgrms.viewmodel.ReportRemarksViewModel
import com.example.sgrms.viewmodel.StudentFirebaseViewModel
import com.example.sgrms.viewmodel.SubjectFirebaseViewModel
import com.example.sgrms.utils.GradeCalculator
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.sgrms.utils.ReportCalculator

@Composable
fun ReportDetailScreen(
    reportId: String = "",
    onBackClick: () -> Unit = {},
    onSaveReportClick: () -> Unit = {},
    onUpdateReportClick: () -> Unit = {}
) {
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
            subjectName = subject?.subjectName ?: "Unknown Subject",
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
            subjectName = subject?.subjectName ?: "Unknown Subject",
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
        "Needs Update"
    } else {
        "Up to Date"
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
    val generatedRemarks = ReportCalculator.buildRemarks(allGrades) { subjectNames[it] }

    val reportRemarks = if (additionalRemarks.isNotBlank()) {
        "$generatedRemarks\n\nAdditional remarks: $additionalRemarks"
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

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopBar(
                title = "Report Detail",
                showBack = true,
                onBackClick = onBackClick,
                showIcon = false
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ColorBackground)
                .navigationBarsPadding()
                .padding(paddingValues)
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp
                )
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = ColorPrimary
                        )
                    }
                }

                reportId.isBlank() || student == null -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = ColorSurface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Assessment,
                                contentDescription = "Report Not Found",
                                tint = ColorOnSurfaceVariant,
                                modifier = Modifier.size(44.dp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Report not found",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = if (errorMessage.isNotBlank()) {
                                    errorMessage
                                } else {
                                    "The selected report may have been deleted."
                                },
                                fontSize = 14.sp,
                                color = ColorOnSurfaceVariant
                            )
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 32.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            ReportStudentInfoCard(
                                student = student,
                                generatedDate = report.generatedDate,
                                reportStatusText = reportStatusText,
                                reportStatusColor = reportStatusColor,
                                reportStatusIcon = reportStatusIcon
                            )
                        }

                        item {
                            ReportPerformanceSummaryCard(
                                totalMarks = totalMarks,
                                averageMarks = averageMarks,
                                overallGrade = overallGrade,
                                completedSubjectCount = completedGrades.size
                            )
                        }

                        item {
                            ReportStatusCard(
                                statusText = reportStatusText,
                                statusColor = reportStatusColor,
                                statusIcon = reportStatusIcon,
                                remarks = reportRemarks
                            )
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = onUpdateReportClick,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(52.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = ColorPrimary
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Update Report",
                                        tint = ColorPrimary
                                    )

                                    Spacer(modifier = Modifier.size(8.dp))

                                    Text(
                                        text = "Update",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Button(
                                    onClick = {
                                        ReportPdfGenerator.saveReportAsPdf(
                                            context = context,
                                            report = report,
                                            student = student,
                                            gradeItems = pdfGradeItems
                                        )

                                        onSaveReportClick()
                                    },
                                    enabled = completedGrades.isNotEmpty(),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(52.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ColorPrimary,
                                        disabledContainerColor = ColorPrimary.copy(alpha = 0.45f)
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Save Report",
                                        tint = Color.White
                                    )

                                    Spacer(modifier = Modifier.size(8.dp))

                                    Text(
                                        text = "Save",
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        if (errorMessage.isNotBlank()) {
                            item {
                                Text(
                                    text = errorMessage,
                                    color = SgrmsColors.Error,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        item {
                            Text(
                                text = "Subject Performance",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (reportGradeList.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = ColorSurface),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.MenuBook,
                                            contentDescription = "No Grade",
                                            tint = ColorOnSurfaceVariant,
                                            modifier = Modifier.size(44.dp)
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(
                                            text = "No grade record found",
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Bold
                                        )

                                        Text(
                                            text = "Grade records will appear here after subject marks are added.",
                                            fontSize = 14.sp,
                                            color = ColorOnSurfaceVariant
                                        )
                                    }
                                }
                            }
                        } else {
                            items(
                                items = reportGradeList,
                                key = { item ->
                                    item.grade.gradeId
                                }
                            ) { item ->
                                ReportGradeCard(
                                    item = item
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
