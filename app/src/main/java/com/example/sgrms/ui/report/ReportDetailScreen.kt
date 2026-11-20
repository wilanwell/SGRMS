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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
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
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class ReportGradeDisplayItem(
    val grade: GradeModel,
    val subjectName: String,
    val subjectCode: String
)

@Composable
fun ReportDetailScreen(
    reportId: String = "",
    onBackClick: () -> Unit = {},
    onSaveReportClick: () -> Unit = {},
    onUpdateReportClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val firestore = remember { FirebaseFirestore.getInstance() }

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

    var additionalRemarks by remember { mutableStateOf("") }
    var localErrorMessage by remember { mutableStateOf("") }

    LaunchedEffect(reportId) {
        if (reportId.isNotBlank()) {
            studentViewModel.loadStudentById(reportId)
            gradeViewModel.loadGradesByStudentId(reportId)
            subjectViewModel.loadSubjects()

            try {
                val remarkDocument = firestore
                    .collection("reportRemarks")
                    .document(reportId)
                    .get()
                    .await()

                additionalRemarks = remarkDocument.getString("remarks").orEmpty()
            } catch (e: Exception) {
                localErrorMessage = e.message ?: "Failed to load report remarks"
            }
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
        calculateGradeLetter(averageMarks.toString())
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
        WarningOrange
    } else {
        SuccessGreen
    }

    val reportStatusIcon = if (isReportOutdated) {
        Icons.Default.Warning
    } else {
        Icons.Default.CheckCircle
    }

    val generatedRemarks = buildReportDetailRemarks(
        completedGrades = completedGrades,
        allStudentGrades = allGrades,
        subjects = subjects
    )

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
        generatedDate = getCurrentDateText()
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
                .background(BackgroundGrey)
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
                            color = PrimaryBlue
                        )
                    }
                }

                reportId.isBlank() || student == null -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
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
                                tint = TextGrey,
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
                                color = TextGrey
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
                                        contentColor = PrimaryBlue
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Update Report",
                                        tint = PrimaryBlue
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
                                        containerColor = PrimaryBlue,
                                        disabledContainerColor = PrimaryBlue.copy(alpha = 0.45f)
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
                                    color = Color.Red,
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
                                    colors = CardDefaults.cardColors(containerColor = CardWhite),
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
                                            tint = TextGrey,
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
                                            color = TextGrey
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

@Composable
fun ReportStudentInfoCard(
    student: StudentModel,
    generatedDate: String,
    reportStatusText: String,
    reportStatusColor: Color,
    reportStatusIcon: ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(
                    modifier = Modifier.size(58.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = PrimaryBlue.copy(alpha = 0.12f)
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Student",
                            tint = PrimaryBlue,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.size(14.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = student.studentName.ifBlank { "Unknown Student" },
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = student.studentCode.ifBlank { "-" },
                        fontSize = 14.sp,
                        color = TextGrey
                    )
                }

                Icon(
                    imageVector = reportStatusIcon,
                    contentDescription = reportStatusText,
                    tint = reportStatusColor,
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            HorizontalDivider(color = BackgroundGrey)

            Spacer(modifier = Modifier.height(18.dp))

            ReportInfoRow(
                icon = Icons.Default.School,
                label = "Class",
                value = student.className.ifBlank { "Not assigned" }
            )

            Spacer(modifier = Modifier.height(12.dp))

            ReportInfoRow(
                icon = Icons.Default.CalendarMonth,
                label = "Generated Date",
                value = generatedDate
            )

            Spacer(modifier = Modifier.height(12.dp))

            ReportInfoRow(
                icon = reportStatusIcon,
                label = "Report Status",
                value = reportStatusText,
                valueColor = reportStatusColor
            )
        }
    }
}

@Composable
fun ReportPerformanceSummaryCard(
    totalMarks: Double,
    averageMarks: Double,
    overallGrade: String,
    completedSubjectCount: Int
) {
    val markColor = getReportDetailMarkColor(averageMarks)
    val progress = (averageMarks / 100.0).toFloat().coerceIn(0f, 1f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = "Performance Summary",
                    tint = PrimaryBlue,
                    modifier = Modifier.size(28.dp)
                )

                Spacer(modifier = Modifier.size(10.dp))

                Text(
                    text = "Performance Summary",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "%.1f%%".format(averageMarks),
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold,
                        color = markColor
                    )

                    Text(
                        text = "Average Marks",
                        fontSize = 13.sp,
                        color = TextGrey
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = overallGrade,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold,
                        color = markColor
                    )

                    Text(
                        text = "Overall Grade",
                        fontSize = 13.sp,
                        color = TextGrey
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LinearProgressIndicator(
                progress = {
                    progress
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(9.dp),
                color = PrimaryBlue,
                trackColor = BackgroundGrey
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ReportSmallInfo(
                    label = "Subjects",
                    value = completedSubjectCount.toString()
                )

                ReportSmallInfo(
                    label = "Total Marks",
                    value = "%.2f".format(totalMarks)
                )

                ReportSmallInfo(
                    label = "Grade",
                    value = overallGrade
                )
            }
        }
    }
}

@Composable
fun ReportStatusCard(
    statusText: String,
    statusColor: Color,
    statusIcon: ImageVector,
    remarks: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = statusIcon,
                    contentDescription = statusText,
                    tint = statusColor,
                    modifier = Modifier.size(28.dp)
                )

                Spacer(modifier = Modifier.size(10.dp))

                Text(
                    text = statusText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = remarks.ifBlank { "No remarks available." },
                fontSize = 14.sp,
                color = TextGrey,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun ReportInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color = Color.Black
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = PrimaryBlue,
            modifier = Modifier.size(22.dp)
        )

        Spacer(modifier = Modifier.size(10.dp))

        Column {
            Text(
                text = label,
                fontSize = 13.sp,
                color = TextGrey
            )

            Text(
                text = value.ifBlank { "-" },
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
        }
    }
}

@Composable
fun ReportGradeCard(
    item: ReportGradeDisplayItem
) {
    val markColor = getReportDetailMarkColor(item.grade.marks)
    val progress = (item.grade.marks / 100.0).toFloat().coerceIn(0f, 1f)
    val statusText = item.grade.status.ifBlank { "Pending" }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardWhite
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(PrimaryBlue.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = "Subject",
                        tint = PrimaryBlue,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.size(14.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = item.subjectName.ifBlank { "Unknown Subject" },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = item.subjectCode.ifBlank { "-" },
                        fontSize = 14.sp,
                        color = TextGrey
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = item.grade.gradeLetter.ifBlank { "-" },
                        fontSize = 27.sp,
                        fontWeight = FontWeight.Bold,
                        color = markColor
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "%.1f%%".format(item.grade.marks),
                        fontSize = 15.sp,
                        color = TextGrey
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LinearProgressIndicator(
                progress = {
                    progress
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = PrimaryBlue,
                trackColor = BackgroundGrey
            )

            if (item.grade.remarks.isNotBlank()) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Remarks: ${item.grade.remarks}",
                    fontSize = 15.sp,
                    color = TextGrey
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Status: $statusText",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = if (statusText.equals("Complete", ignoreCase = true)) {
                    SuccessGreen
                } else {
                    WarningOrange
                }
            )
        }
    }
}

@Composable
fun ReportSmallInfo(
    label: String,
    value: String
) {
    Column {
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryBlue
        )

        Text(
            text = label,
            fontSize = 11.sp,
            color = TextGrey
        )
    }
}

fun getReportDetailMarkColor(
    marks: Double
): Color {
    return when {
        marks >= 80 -> SuccessGreen
        marks >= 60 -> PrimaryBlue
        marks >= 50 -> WarningOrange
        else -> Color.Red
    }
}

fun buildReportDetailRemarks(
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
        !grade.status.equals("Complete", ignoreCase = true)
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