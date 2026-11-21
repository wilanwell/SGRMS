package com.example.sgrms.ui.report

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sgrms.ui.components.TopBar
import com.example.sgrms.ui.theme.ColorBackground
import com.example.sgrms.ui.theme.ColorPrimary
import com.example.sgrms.utils.ReportPdfGenerator
import com.example.sgrms.utils.ReportPdfGradeItem
import com.example.sgrms.utils.ReportPdfData
import com.example.sgrms.viewmodel.GradeFirebaseViewModel
import com.example.sgrms.viewmodel.ReportRemarksViewModel
import com.example.sgrms.viewmodel.StudentFirebaseViewModel
import com.example.sgrms.viewmodel.SubjectFirebaseViewModel
import kotlinx.coroutines.launch
import com.example.sgrms.utils.GradeCalculator
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.sgrms.utils.ReportCalculator

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

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopBar(
                title = "My Report Summary",
                showBack = false,
                actions = {
                    IconButton(
                        enabled = completedSubjectItems.isNotEmpty(),
                        onClick = {
                            downloadPdf()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Download PDF",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(ColorBackground)
                .padding(paddingValues),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 18.dp,
                bottom = 8.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                StudentReportHeaderCard(
                    student = student,
                    reportDate = reportDate
                )
            }

            item {
                StudentMainPerformanceCard(
                    averageMarks = averageMarks,
                    overallGrade = overallGrade,
                    totalSubjects = totalSubjects,
                    totalMarks = totalMarks,
                    performanceStatus = performanceStatus
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StudentReportSummaryCard(
                        title = "Subjects",
                        value = totalSubjects.toString(),
                        icon = Icons.Default.MenuBook,
                        modifier = Modifier.weight(1f)
                    )

                    StudentReportSummaryCard(
                        title = "Total Marks",
                        value = "%.0f".format(totalMarks),
                        icon = Icons.Default.Assessment,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StudentReportSummaryCard(
                        title = "Status",
                        value = performanceStatus,
                        icon = Icons.Default.TrendingUp,
                        modifier = Modifier.weight(1f)
                    )

                    StudentReportSummaryCard(
                        title = "Average",
                        value = "%.1f%%".format(averageMarks),
                        icon = Icons.Default.Assessment,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                StudentReportRemarksCard(
                    remarks = reportRemarks
                )
            }

            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = "Subject Performance",
                        tint = ColorPrimary
                    )

                    Spacer(modifier = Modifier.size(8.dp))

                    Text(
                        text = "Subject Performance",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            items(
                items = subjectItems,
                key = { item ->
                    item.grade.gradeId
                }
            ) { item ->
                StudentReportSubjectCard(
                    item = item
                )
            }
        }
    }
}
