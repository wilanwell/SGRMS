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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.sgrms.data.repository.GradeFirebaseRepository
import com.example.sgrms.data.repository.StudentFirebaseRepository
import com.example.sgrms.data.repository.SubjectFirebaseRepository
import com.example.sgrms.ui.components.TopBar
import com.example.sgrms.ui.theme.BackgroundGrey
import com.example.sgrms.ui.theme.CardWhite
import com.example.sgrms.ui.theme.PrimaryBlue
import com.example.sgrms.ui.theme.SuccessGreen
import com.example.sgrms.ui.theme.TextGrey
import com.example.sgrms.ui.theme.WarningOrange
import com.example.sgrms.utils.ReportPdfGenerator
import com.example.sgrms.utils.ReportPdfGradeItem
import com.example.sgrms.utils.createReportPdfDataFromDatabase
import com.example.sgrms.viewmodel.GradeFirebaseViewModel
import com.example.sgrms.viewmodel.GradeFirebaseViewModelFactory
import com.example.sgrms.viewmodel.StudentFirebaseViewModel
import com.example.sgrms.viewmodel.StudentFirebaseViewModelFactory
import com.example.sgrms.viewmodel.SubjectFirebaseViewModel
import com.example.sgrms.viewmodel.SubjectFirebaseViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt
import kotlinx.coroutines.launch
import com.example.sgrms.utils.GradeCalculator
import com.example.sgrms.ui.theme.markColor

data class StudentReportSubjectItem(
    val grade: GradeModel,
    val subjectName: String,
    val subjectCode: String
)

@Composable
fun StudentReportSummaryScreen(
    studentId: String = "",
    onBackClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

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

    val reportRemarks = buildStudentReportRemarks(subjectItems)

    val reportDate = getStudentReportCurrentDateText()

    fun downloadPdf() {
        if (completedSubjectItems.isEmpty()) {
            return
        }

        coroutineScope.launch {
            val reportForPdf = createReportPdfDataFromDatabase(
                reportId = studentId,
                studentId = studentId,
                totalMarks = totalMarks,
                averageMarks = averageMarks,
                overallGrade = overallGrade,
                remarks = reportRemarks,
                generatedDate = reportDate,
                student = student
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
                .background(BackgroundGrey)
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
                        tint = PrimaryBlue
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

@Composable
fun StudentReportHeaderCard(
    student: StudentModel?,
    reportDate: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryBlue),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Student",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.size(14.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = student?.studentName?.ifBlank { "Unknown Student" }
                            ?: "Unknown Student",
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Text(
                        text = "${student?.studentCode?.ifBlank { "-" } ?: "-"} • ${student?.className?.ifBlank { "Not assigned" } ?: "Not assigned"}",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.88f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "Report Date",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.size(8.dp))

                Text(
                    text = "Report Date: $reportDate",
                    fontSize = 14.sp,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun StudentMainPerformanceCard(
    averageMarks: Double,
    overallGrade: String,
    totalSubjects: Int,
    totalMarks: Double,
    performanceStatus: String
) {
    val progress = (averageMarks / 100.0).toFloat().coerceIn(0f, 1f)
    val gradeColor = markColor(averageMarks)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = "Performance",
                    tint = PrimaryBlue,
                    modifier = Modifier.size(28.dp)
                )

                Spacer(modifier = Modifier.size(10.dp))

                Column {
                    Text(
                        text = "Academic Performance",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Summary based on completed subjects",
                        fontSize = 13.sp,
                        color = TextGrey
                    )
                }
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
                        color = gradeColor
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
                        color = gradeColor
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
                color = gradeColor,
                trackColor = BackgroundGrey
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StudentReportMiniInfo(
                    label = "Subjects",
                    value = totalSubjects.toString()
                )

                StudentReportMiniInfo(
                    label = "Total Marks",
                    value = "%.0f".format(totalMarks)
                )

                StudentReportMiniInfo(
                    label = "Status",
                    value = performanceStatus
                )
            }
        }
    }
}

@Composable
fun StudentReportSummaryCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(108.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
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
                tint = PrimaryBlue,
                modifier = Modifier.size(26.dp)
            )

            Column {
                Text(
                    text = value,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = title,
                    fontSize = 13.sp,
                    color = TextGrey
                )
            }
        }
    }
}

@Composable
fun StudentReportRemarksCard(
    remarks: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Remarks",
                    tint = PrimaryBlue,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.size(8.dp))

                Text(
                    text = "Report Remarks",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
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
fun StudentReportSubjectCard(
    item: StudentReportSubjectItem
) {
    val markColor = markColor(item.grade.marks)
    val progress = (item.grade.marks / 100.0).toFloat().coerceIn(0f, 1f)

    Card(
        modifier = Modifier.fillMaxWidth(),
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
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(PrimaryBlue.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = "Subject",
                        tint = PrimaryBlue,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.size(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = item.subjectName.ifBlank { "Unknown Subject" },
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = item.subjectCode.ifBlank { "-" },
                        fontSize = 13.sp,
                        color = TextGrey
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = item.grade.gradeLetter.ifBlank { "-" },
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = markColor
                    )

                    Text(
                        text = "${item.grade.marks.roundToInt()}%",
                        fontSize = 14.sp,
                        color = TextGrey
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LinearProgressIndicator(
                progress = {
                    progress
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = markColor,
                trackColor = BackgroundGrey
            )

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(color = BackgroundGrey)

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StudentReportMiniInfo(
                    label = "Marks",
                    value = "%.1f".format(item.grade.marks)
                )

                StudentReportMiniInfo(
                    label = "Grade",
                    value = item.grade.gradeLetter.ifBlank { "-" }
                )

                StudentReportMiniInfo(
                    label = "Status",
                    value = item.grade.status.ifBlank { "Pending" }
                )
            }

            if (item.grade.remarks.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Remarks: ${item.grade.remarks}",
                    fontSize = 13.sp,
                    color = TextGrey
                )
            }
        }
    }
}

@Composable
fun StudentReportMiniInfo(
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

fun buildStudentReportRemarks(
    subjectItems: List<StudentReportSubjectItem>
): String {
    val completedRemarks = subjectItems
        .filter { item ->
            item.grade.status.equals("Complete", ignoreCase = true) &&
                    item.grade.remarks.isNotBlank()
        }
        .map { item ->
            "${item.subjectName}: ${item.grade.remarks}"
        }
        .toMutableList()

    val pendingCount = subjectItems.count { item ->
        !item.grade.status.equals("Complete", ignoreCase = true)
    }

    if (pendingCount > 0) {
        completedRemarks.add(
            "Needs update: $pendingCount subject grade record(s) are still pending."
        )
    }

    return if (completedRemarks.isNotEmpty()) {
        completedRemarks.joinToString(separator = "\n")
    } else {
        "No specific remarks from completed grade records."
    }
}

fun getStudentReportCurrentDateText(): String {
    val formatter = SimpleDateFormat(
        "dd/MM/yyyy",
        Locale.getDefault()
    )

    return formatter.format(Date())
}