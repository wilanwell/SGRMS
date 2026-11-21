package com.example.sgrms.ui.grade

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sgrms.data.model.GradeModel
import com.example.sgrms.ui.components.SgrmsEmptyStateCard
import com.example.sgrms.ui.components.SgrmsHeaderProfileCard
import com.example.sgrms.ui.components.SgrmsPrimaryButton
import com.example.sgrms.ui.components.SgrmsSmallSummaryBox
import com.example.sgrms.ui.components.TopBar
import com.example.sgrms.ui.theme.ColorBackground
import com.example.sgrms.ui.theme.ColorSurface
import com.example.sgrms.ui.theme.ColorPrimary
import com.example.sgrms.ui.theme.SgrmsColors
import com.example.sgrms.ui.theme.ColorOnSurfaceVariant
import com.example.sgrms.viewmodel.GradeFirebaseViewModel
import com.example.sgrms.viewmodel.StudentFirebaseViewModel
import com.example.sgrms.viewmodel.SubjectFirebaseViewModel
import com.example.sgrms.utils.GradeCalculator
import com.example.sgrms.ui.theme.markColor
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

data class StudentGradeSubjectDisplayItem(
    val grade: GradeModel,
    val subjectName: String,
    val subjectCode: String
)

@Composable
fun StudentGradeDetailScreen(
    studentId: String = "",
    onBackClick: () -> Unit = {},
    onAddGradeClick: () -> Unit = {},
    onGenerateReportClick: () -> Unit = {}
) {
    val studentViewModel: StudentFirebaseViewModel = hiltViewModel()

    val gradeViewModel: GradeFirebaseViewModel = hiltViewModel()

    val subjectViewModel: SubjectFirebaseViewModel = hiltViewModel()

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
    val allGrades = gradeUiState.grades

    val completedGrades = allGrades.filter { grade ->
        grade.status.equals("Complete", ignoreCase = true)
    }

    val subjects = subjectUiState.subjects

    val studentName = student?.studentName ?: "Unknown Student"
    val className = student?.className ?: "Unknown Class"

    val gradeDisplayList = allGrades.map { grade ->
        val subject = subjects.find { subject ->
            subject.subjectId == grade.subjectId
        }

        StudentGradeSubjectDisplayItem(
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

    val subjectCount = completedGrades.size

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
                subjectUiState.isLoading

    val errorMessage = when {
        studentUiState.errorMessage.isNotBlank() -> studentUiState.errorMessage
        gradeUiState.errorMessage.isNotBlank() -> gradeUiState.errorMessage
        subjectUiState.errorMessage.isNotBlank() -> subjectUiState.errorMessage
        else -> ""
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopBar(
                title = "Student Grades",
                showBack = true,
                onBackClick = onBackClick,
                showIcon = false
            )
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(ColorBackground)
                .navigationBarsPadding()
                .padding(paddingValues),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = 32.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            when {
                isLoading -> {
                    item {
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
                }

                student == null -> {
                    item {
                        SgrmsEmptyStateCard(
                            icon = Icons.Default.Person,
                            title = "Student not found",
                            message = if (errorMessage.isNotBlank()) {
                                errorMessage
                            } else {
                                "The selected student record could not be loaded."
                            }
                        )
                    }
                }

                else -> {
                    item {
                        SgrmsHeaderProfileCard(
                            title = studentName,
                            subtitle = className,
                            icon = Icons.Default.Person
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                SgrmsSmallSummaryBox(
                                    title = "Average",
                                    value = "%.2f%%".format(averageMarks),
                                    modifier = Modifier.weight(1f)
                                )

                                SgrmsSmallSummaryBox(
                                    title = "Overall Grade",
                                    value = overallGrade,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                SgrmsSmallSummaryBox(
                                    title = "Completed Subjects",
                                    value = subjectCount.toString(),
                                    modifier = Modifier.weight(1f)
                                )

                                SgrmsSmallSummaryBox(
                                    title = "Total Marks",
                                    value = "%.2f".format(totalMarks),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    item {
                        SgrmsPrimaryButton(
                            text = "Add Grade",
                            onClick = onAddGradeClick,
                            icon = Icons.Default.Add,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        SgrmsPrimaryButton(
                            text = "Generate Report",
                            onClick = onGenerateReportClick,
                            icon = Icons.Default.Summarize,
                            modifier = Modifier.fillMaxWidth()
                        )
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.BarChart,
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

                    if (gradeDisplayList.isEmpty()) {
                        item {
                            SgrmsEmptyStateCard(
                                icon = Icons.Default.Assessment,
                                title = "No grade record found",
                                message = "Grade records will appear here after the teacher adds subject marks."
                            )
                        }
                    } else {
                        items(
                            items = gradeDisplayList,
                            key = { item ->
                                item.grade.gradeId
                            }
                        ) { item ->
                            StudentGradeSubjectCard(
                                item = item
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StudentGradeSubjectCard(
    item: StudentGradeSubjectDisplayItem
) {
    val markColor = markColor(item.grade.marks)
    val progress = (item.grade.marks / 100.0).toFloat().coerceIn(0f, 1f)
    val statusText = item.grade.status.ifBlank { "Pending" }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = ColorSurface
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
                        .background(ColorPrimary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = "Subject",
                        tint = ColorPrimary,
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
                        color = ColorOnSurfaceVariant
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
                        color = ColorOnSurfaceVariant
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
                color = ColorPrimary,
                trackColor = ColorBackground
            )

            if (item.grade.remarks.isNotBlank()) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Remarks: ${item.grade.remarks}",
                    fontSize = 15.sp,
                    color = ColorOnSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Status: $statusText",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = if (statusText.equals("Complete", ignoreCase = true)) {
                    SgrmsColors.Success
                } else {
                    SgrmsColors.WarningBar
                }
            )
        }
    }
}

