package com.example.sgrms.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sgrms.data.firebase.GradeFirebaseService
import com.example.sgrms.data.firebase.StudentFirebaseService
import com.example.sgrms.data.firebase.SubjectFirebaseService
import com.example.sgrms.data.repository.GradeFirebaseRepository
import com.example.sgrms.data.repository.StudentFirebaseRepository
import com.example.sgrms.data.repository.SubjectFirebaseRepository
import com.example.sgrms.ui.components.SgrmsDashboardSummaryCard
import com.example.sgrms.ui.components.SgrmsProgressItem
import com.example.sgrms.ui.components.SgrmsSummaryCardStyle
import com.example.sgrms.ui.components.TopBar
import com.example.sgrms.ui.theme.BackgroundGrey
import com.example.sgrms.ui.theme.CardWhite
import com.example.sgrms.ui.theme.PrimaryBlue
import com.example.sgrms.ui.theme.TextGrey
import com.example.sgrms.viewmodel.GradeFirebaseViewModel
import com.example.sgrms.viewmodel.GradeFirebaseViewModelFactory
import com.example.sgrms.viewmodel.StudentFirebaseViewModel
import com.example.sgrms.viewmodel.StudentFirebaseViewModelFactory
import com.example.sgrms.viewmodel.SubjectFirebaseViewModel
import com.example.sgrms.viewmodel.SubjectFirebaseViewModelFactory
import com.example.sgrms.utils.GradeCalculator

data class StudentDashboardGradeItem(
    val gradeId: String,
    val subjectId: String,
    val subjectName: String,
    val marks: Double,
    val gradeLetter: String
)

@Composable
fun StudentDashboardScreen(
    studentId: Int = 0,
    firebaseStudentId: String = "",
    onProfileClick: () -> Unit = {},
    onReportClick: () -> Unit = {}
) {
    val currentStudentId = firebaseStudentId.ifBlank {
        if (studentId != 0) {
            studentId.toString()
        } else {
            ""
        }
    }

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

    LaunchedEffect(currentStudentId) {
        if (currentStudentId.isNotBlank()) {
            studentViewModel.loadStudentById(currentStudentId)
            gradeViewModel.loadGradesByStudentId(currentStudentId)
            subjectViewModel.loadSubjects()
        }
    }

    val student = studentUiState.selectedStudent
    val grades = gradeUiState.grades
    val subjects = subjectUiState.subjects

    val studentName = student?.studentName?.ifBlank { "Student" } ?: "Student"

    val completedGrades = grades.filter { grade ->
        grade.status.equals("Complete", ignoreCase = true)
    }

    val subjectResults = completedGrades.map { grade ->
        val subject = subjects.find { subject ->
            subject.subjectId == grade.subjectId
        }

        StudentDashboardGradeItem(
            gradeId = grade.gradeId,
            subjectId = grade.subjectId,
            subjectName = subject?.subjectName ?: "Unknown Subject",
            marks = grade.marks,
            gradeLetter = grade.gradeLetter
        )
    }.sortedWith(
        compareBy<StudentDashboardGradeItem> { item ->
            item.subjectName
        }.thenBy { item ->
            item.gradeId
        }
    )

    val totalMarks = subjectResults.sumOf { item ->
        item.marks
    }

    val averageMarks = if (subjectResults.isNotEmpty()) {
        totalMarks / subjectResults.size
    } else {
        0.0
    }

    val overallGrade = GradeCalculator.overallGrade(averageMarks)

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopBar(
                title = "SGRMS",
                showBack = false,
                showIcon = true,
                actions = {
                    IconButton(onClick = onProfileClick) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Profile",
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundGrey)
                .padding(paddingValues)
                .padding(
                    start = 18.dp,
                    end = 18.dp,
                    top = 18.dp,
                    bottom = 8.dp
                )
        ) {
            Text(
                text = "Welcome, $studentName",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "View your grades and academic performance",
                fontSize = 14.sp,
                color = TextGrey
            )

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = PrimaryBlue),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Average Mark",
                            color = Color.White,
                            fontSize = 15.sp
                        )

                        Text(
                            text = "%.2f%%".format(averageMarks),
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "Overall Grade",
                            color = Color.White,
                            fontSize = 15.sp
                        )

                        Text(
                            text = overallGrade,
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                SgrmsDashboardSummaryCard(
                    title = "Subjects",
                    value = subjectResults.size.toString(),
                    icon = Icons.Default.MenuBook,
                    modifier = Modifier.weight(1f),
                    style = SgrmsSummaryCardStyle.White,
                    height = 120.dp
                )

                SgrmsDashboardSummaryCard(
                    title = "Report",
                    value = "View",
                    icon = Icons.Default.Summarize,
                    modifier = Modifier.weight(1f),
                    style = SgrmsSummaryCardStyle.White,
                    height = 120.dp,
                    onClick = onReportClick
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
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

                    Spacer(modifier = Modifier.height(16.dp))

                    if (subjectResults.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 30.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Assessment,
                                contentDescription = "No Grades",
                                tint = TextGrey,
                                modifier = Modifier.size(44.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "No grade record found",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "Your grades will appear here after the teacher adds marks.",
                                fontSize = 13.sp,
                                color = TextGrey
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 8.dp)
                        ) {
                            itemsIndexed(
                                items = subjectResults,
                                key = { index, item ->
                                    item.gradeId.ifBlank {
                                        "${item.subjectId}-${item.subjectName}-${item.marks}-$index"
                                    }
                                }
                            ) { _, item ->
                                SgrmsProgressItem(
                                    title = item.subjectName,
                                    progress = (item.marks / 100.0).toFloat()
                                        .coerceIn(0f, 1f),
                                    percentageText = "%.1f%%".format(item.marks),
                                    subtitle = "Grade ${item.gradeLetter.ifBlank { "-" }}",
                                    subtitleAsGrade = true
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

