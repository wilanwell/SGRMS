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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sgrms.ui.components.SgrmsEmptyStateCard
import com.example.sgrms.ui.components.TopBar
import com.example.sgrms.ui.theme.BackgroundGrey
import com.example.sgrms.ui.theme.PrimaryBlue
import com.example.sgrms.ui.theme.TextGrey
import com.example.sgrms.utils.ReportPdfData
import com.example.sgrms.utils.ReportPdfGenerator
import com.example.sgrms.utils.ReportPdfGradeItem
import com.example.sgrms.viewmodel.GradeFirebaseViewModel
import com.example.sgrms.viewmodel.StudentFirebaseViewModel
import com.example.sgrms.viewmodel.SubjectFirebaseViewModel
import com.example.sgrms.utils.GradeCalculator
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.sgrms.utils.ReportCalculator

@Composable
fun ReportScreen(
    onBackClick: () -> Unit = {},
    onViewReportDetailClick: (String) -> Unit = {},
    onDownloadPdfClick: (String) -> Unit = {},
    onUpdateReportClick: (String) -> Unit = {}
) {
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

    var showFilterDialog by remember { mutableStateOf(false) }

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
            student.className.ifBlank { "Not assigned" }
        }
        .filter { className ->
            className.isNotBlank()
        }
        .distinct()
        .sorted()

    val statusList = listOf(
        "All",
        "Generated",
        "Needs Update"
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
                studentName = student.studentName.ifBlank { "Unknown Student" },
                studentCode = student.studentCode.ifBlank { "-" },
                className = student.className.ifBlank { "Not assigned" },
                status = if (hasPendingGrades || completedGrades.isEmpty()) {
                    "Needs Update"
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
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                selectedReportToDelete = null
            },
            title = {
                Text(
                    text = "Remove Report",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "This will remove the report from the current list only. The grade records in Firebase will not be deleted."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedReportToDelete?.let { item ->
                            hiddenReportStudentIds.add(item.student.studentId)
                        }

                        showDeleteDialog = false
                        selectedReportToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red
                    )
                ) {
                    Text(
                        text = "Remove",
                        color = Color.White
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        selectedReportToDelete = null
                    }
                ) {
                    Text(
                        text = "Cancel",
                        color = PrimaryBlue
                    )
                }
            }
        )
    }

    if (showFilterDialog) {
        ReportFilterDialog(
            selectedClass = selectedClass,
            selectedStatus = selectedStatus,
            selectedDate = selectedDate,
            classList = classList,
            statusList = statusList,
            dateList = dateList,
            onClassSelected = {
                selectedClass = it
            },
            onStatusSelected = {
                selectedStatus = it
            },
            onDateSelected = {
                selectedDate = it
            },
            onDismiss = {
                showFilterDialog = false
            },
            onClearFilter = {
                selectedClass = "All Classes"
                selectedStatus = "All"
                selectedDate = "Latest"
                showFilterDialog = false
            }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopBar(
                title = "Report Manager",
                showBack = false,
                showIcon = true,
                icon = Icons.Default.Description
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    hiddenReportStudentIds.clear()
                    studentViewModel.loadStudents()
                    gradeViewModel.loadGrades()
                    subjectViewModel.loadSubjects()
                },
                containerColor = PrimaryBlue,
                contentColor = Color.White,
                icon = {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Generate Reports"
                    )
                },
                text = {
                    Text(
                        text = "Generate",
                        fontWeight = FontWeight.Bold
                    )
                },
                modifier = Modifier.padding(end = 4.dp)
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundGrey)
                .padding(paddingValues)
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp
                )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ReportSummaryCard(
                    title = "Reports",
                    value = totalReports.toString(),
                    icon = Icons.Default.Description,
                    modifier = Modifier.weight(1f)
                )

                ReportSummaryCard(
                    title = "Average",
                    value = "%.1f%%".format(averageClassMark),
                    icon = Icons.Default.TrendingUp,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = searchText,
                onValueChange = {
                    searchText = it
                },
                label = {
                    Text("Search Report")
                },
                placeholder = {
                    Text("Search by student, code, class, or grade")
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = TextGrey
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryBlue,
                    focusedLabelColor = PrimaryBlue,
                    cursorColor = PrimaryBlue
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Report List (${filteredReports.size})",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                OutlinedButton(
                    onClick = {
                        showFilterDialog = true
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = PrimaryBlue
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filter Reports",
                        tint = PrimaryBlue,
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "Filter",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (errorMessage.isNotBlank()) {
                Text(
                    text = errorMessage,
                    color = Color.Red,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(10.dp))
            }

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = PrimaryBlue
                        )
                    }
                }

                filteredReports.isEmpty() -> {
                    SgrmsEmptyStateCard(
                        icon = Icons.Default.Description,
                        title = "No report found",
                        message = "Tap Generate to create reports from completed grade records"
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = filteredReports,
                            key = { item ->
                                item.report.reportId
                            }
                        ) { item ->
                            ReportCard(
                                item = item,
                                onViewClick = {
                                    onViewReportDetailClick(item.report.reportId)
                                },
                                onDownloadClick = {
                                    ReportPdfGenerator.saveReportAsPdf(
                                        context = context,
                                        report = item.report,
                                        student = item.student,
                                        gradeItems = item.gradeItems
                                    )

                                    onDownloadPdfClick(item.report.reportId)
                                },
                                onUpdateClick = {
                                    onUpdateReportClick(item.report.reportId)
                                },
                                onDeleteClick = {
                                    selectedReportToDelete = item
                                    showDeleteDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
