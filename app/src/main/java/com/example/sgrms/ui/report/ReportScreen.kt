package com.example.sgrms.ui.report

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedIconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sgrms.data.firebase.GradeFirebaseService
import com.example.sgrms.data.firebase.StudentFirebaseService
import com.example.sgrms.data.firebase.SubjectFirebaseService
import com.example.sgrms.data.model.GradeModel
import com.example.sgrms.data.model.StudentModel
import com.example.sgrms.data.model.SubjectModel
import com.example.sgrms.data.repository.GradeFirebaseRepository
import com.example.sgrms.data.repository.StudentFirebaseRepository
import com.example.sgrms.data.repository.SubjectFirebaseRepository
import com.example.sgrms.ui.components.SgrmsEmptyStateCard
import com.example.sgrms.ui.components.TopBar
import com.example.sgrms.ui.grade.calculateGradeLetter
import com.example.sgrms.ui.theme.BackgroundGrey
import com.example.sgrms.ui.theme.CardWhite
import com.example.sgrms.ui.theme.PrimaryBlue
import com.example.sgrms.ui.theme.SuccessGreen
import com.example.sgrms.ui.theme.TextGrey
import com.example.sgrms.ui.theme.WarningOrange
import com.example.sgrms.utils.ReportPdfData
import com.example.sgrms.utils.ReportPdfGenerator
import com.example.sgrms.utils.ReportPdfGradeItem
import com.example.sgrms.viewmodel.GradeFirebaseViewModel
import com.example.sgrms.viewmodel.GradeFirebaseViewModelFactory
import com.example.sgrms.viewmodel.StudentFirebaseViewModel
import com.example.sgrms.viewmodel.StudentFirebaseViewModelFactory
import com.example.sgrms.viewmodel.SubjectFirebaseViewModel
import com.example.sgrms.viewmodel.SubjectFirebaseViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ReportDisplayItem(
    val report: ReportPdfData,
    val student: StudentModel,
    val gradeItems: List<ReportPdfGradeItem>,
    val studentName: String,
    val studentCode: String,
    val className: String,
    val status: String
)

@Composable
fun ReportScreen(
    onBackClick: () -> Unit = {},
    onViewReportDetailClick: (String) -> Unit = {},
    onDownloadPdfClick: (String) -> Unit = {},
    onUpdateReportClick: (String) -> Unit = {}
) {
    val context = LocalContext.current

    val studentFirebaseService = remember {
        StudentFirebaseService()
    }

    val studentFirebaseRepository = remember {
        StudentFirebaseRepository(studentFirebaseService)
    }

    val studentViewModel: StudentFirebaseViewModel = viewModel(
        factory = StudentFirebaseViewModelFactory(studentFirebaseRepository)
    )

    val gradeFirebaseService = remember {
        GradeFirebaseService()
    }

    val gradeFirebaseRepository = remember {
        GradeFirebaseRepository(gradeFirebaseService)
    }

    val gradeViewModel: GradeFirebaseViewModel = viewModel(
        factory = GradeFirebaseViewModelFactory(gradeFirebaseRepository)
    )

    val subjectFirebaseService = remember {
        SubjectFirebaseService()
    }

    val subjectFirebaseRepository = remember {
        SubjectFirebaseRepository(subjectFirebaseService)
    }

    val subjectViewModel: SubjectFirebaseViewModel = viewModel(
        factory = SubjectFirebaseViewModelFactory(subjectFirebaseRepository)
    )

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

    val today = getCurrentDateText()

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
                calculateGradeLetter(averageMarks.toString())
            } else {
                "-"
            }

            val reportRemarks = buildReportRemarksFromGrades(
                completedGrades = completedGrades,
                allStudentGrades = allStudentGrades,
                subjects = subjects
            )

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

@Composable
fun ReportSummaryCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(112.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryBlue),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )

            Column {
                Text(
                    text = value,
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun ReportCard(
    item: ReportDisplayItem,
    onViewClick: () -> Unit,
    onDownloadClick: () -> Unit,
    onUpdateClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val statusIcon = if (item.status == "Generated") {
        Icons.Default.CheckCircle
    } else {
        Icons.Default.Warning
    }

    val statusColor = if (item.status == "Generated") {
        SuccessGreen
    } else {
        WarningOrange
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onViewClick()
            },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(PrimaryBlue.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = "Report",
                        tint = PrimaryBlue,
                        modifier = Modifier.size(29.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = item.studentName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "${item.studentCode} • ${item.className}",
                        fontSize = 13.sp,
                        color = TextGrey
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = item.report.overallGrade,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = getReportGradeColor(item.report.averageMarks)
                    )

                    Text(
                        text = "%.2f%%".format(item.report.averageMarks),
                        fontSize = 14.sp,
                        color = TextGrey
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(color = BackgroundGrey)

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Date",
                        tint = TextGrey,
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(modifier = Modifier.width(5.dp))

                    Text(
                        text = item.report.generatedDate,
                        fontSize = 13.sp,
                        color = TextGrey
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = statusIcon,
                        contentDescription = item.status,
                        tint = statusColor,
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = item.status,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }

            if (item.report.remarks.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Remarks: ${item.report.remarks}",
                    fontSize = 14.sp,
                    color = TextGrey
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedIconButton(
                    onClick = onViewClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = "View Report",
                        tint = PrimaryBlue
                    )
                }

                OutlinedIconButton(
                    onClick = onDownloadClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Download PDF",
                        tint = SuccessGreen
                    )
                }

                OutlinedIconButton(
                    onClick = onUpdateClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Update Report",
                        tint = PrimaryBlue
                    )
                }

                OutlinedIconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove Report",
                        tint = Color.Red
                    )
                }
            }
        }
    }
}

@Composable
fun ReportFilterDialog(
    selectedClass: String,
    selectedStatus: String,
    selectedDate: String,
    classList: List<String>,
    statusList: List<String>,
    dateList: List<String>,
    onClassSelected: (String) -> Unit,
    onStatusSelected: (String) -> Unit,
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    onClearFilter: () -> Unit
) {
    var classExpanded by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }
    var dateExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Filter Reports",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                ReportDialogDropdown(
                    label = "Class",
                    value = selectedClass,
                    expanded = classExpanded,
                    onExpandedChange = {
                        classExpanded = it
                    },
                    items = classList,
                    onItemSelected = { value ->
                        onClassSelected(value)
                        classExpanded = false
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                ReportDialogDropdown(
                    label = "Status",
                    value = selectedStatus,
                    expanded = statusExpanded,
                    onExpandedChange = {
                        statusExpanded = it
                    },
                    items = statusList,
                    onItemSelected = { value ->
                        onStatusSelected(value)
                        statusExpanded = false
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                ReportDialogDropdown(
                    label = "Sort",
                    value = selectedDate,
                    expanded = dateExpanded,
                    onExpandedChange = {
                        dateExpanded = it
                    },
                    items = dateList,
                    onItemSelected = { value ->
                        onDateSelected(value)
                        dateExpanded = false
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue
                )
            ) {
                Text(
                    text = "Apply",
                    color = Color.White
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onClearFilter
            ) {
                Text(
                    text = "Clear",
                    color = PrimaryBlue
                )
            }
        }
    )
}

@Composable
fun ReportDialogDropdown(
    label: String,
    value: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    items: List<String>,
    onItemSelected: (String) -> Unit
) {
    Box {
        OutlinedButton(
            onClick = {
                onExpandedChange(true)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = PrimaryBlue
            )
        ) {
            Text(
                text = "$label: $value",
                fontSize = 14.sp,
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = label,
                tint = PrimaryBlue,
                modifier = Modifier.size(18.dp)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                onExpandedChange(false)
            }
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = {
                        Text(item)
                    },
                    onClick = {
                        onItemSelected(item)
                    }
                )
            }
        }
    }
}

fun getReportGradeColor(
    percentage: Double
): Color {
    return when {
        percentage >= 80 -> SuccessGreen
        percentage >= 70 -> PrimaryBlue
        percentage >= 60 -> WarningOrange
        else -> Color.Red
    }
}

fun getCurrentDateText(): String {
    val formatter = SimpleDateFormat(
        "dd/MM/yyyy",
        Locale.getDefault()
    )

    return formatter.format(Date())
}

fun buildReportRemarksFromGrades(
    completedGrades: List<GradeModel>,
    allStudentGrades: List<GradeModel>,
    subjects: List<SubjectModel>
): String {
    val remarksList = completedGrades
        .filter { grade ->
            grade.remarks.isNotBlank()
        }
        .map { grade ->
            val subject = subjects.find { subject ->
                subject.subjectId == grade.subjectId
            }

            val subjectName = subject?.subjectName ?: "Unknown Subject"

            "$subjectName: ${grade.remarks}"
        }
        .toMutableList()

    val pendingCount = allStudentGrades.count { grade ->
        grade.status != "Complete"
    }

    if (pendingCount > 0) {
        remarksList.add(
            "Needs update: $pendingCount subject grade record(s) are still pending."
        )
    }

    return if (remarksList.isNotEmpty()) {
        remarksList.joinToString(separator = "\n")
    } else {
        "No specific remarks from completed grade records."
    }
}