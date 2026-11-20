package com.example.sgrms.ui.subject

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sgrms.data.model.GradeModel
import com.example.sgrms.ui.components.SgrmsEmptyStateCard
import com.example.sgrms.ui.components.TopBar
import com.example.sgrms.ui.theme.BackgroundGrey
import com.example.sgrms.ui.theme.CardWhite
import com.example.sgrms.ui.theme.PrimaryBlue
import com.example.sgrms.ui.theme.TextGrey
import com.example.sgrms.viewmodel.GradeFirebaseViewModel
import com.example.sgrms.viewmodel.StudentFirebaseViewModel
import com.example.sgrms.viewmodel.SubjectFirebaseViewModel
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun SubjectDetailScreen(
    subjectId: String = "",
    onBackClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onViewStudentsClick: () -> Unit = {}
) {
    val subjectViewModel: SubjectFirebaseViewModel = hiltViewModel()
    val studentViewModel: StudentFirebaseViewModel = hiltViewModel()
    val gradeViewModel: GradeFirebaseViewModel = hiltViewModel()

    val subjectUiState by subjectViewModel.uiState.collectAsState()

    val studentUiState by studentViewModel.uiState.collectAsState()
    val gradeUiState by gradeViewModel.uiState.collectAsState()

    val students = studentUiState.students
    val grades = gradeUiState.grades

    val isStudentGradeLoading = studentUiState.isLoading || gradeUiState.isLoading
    val studentGradeErrorMessage = gradeUiState.errorMessage.ifBlank { studentUiState.errorMessage }

    var showAddStudentDialog by remember { mutableStateOf(false) }

    LaunchedEffect(subjectId) {
        if (subjectId.isNotBlank()) {
            subjectViewModel.loadSubjectById(subjectId)
            studentViewModel.loadStudents()
            gradeViewModel.loadGradesBySubjectId(subjectId)
        }
    }

    val subject = subjectUiState.selectedSubject

    val subjectGrades = grades.filter { grade ->
        grade.subjectId == subjectId
    }

    val studentsTakingSubject = subjectGrades.mapNotNull { grade ->
        students.find { student ->
            student.studentId == grade.studentId
        }
    }.distinctBy { student ->
        student.studentId
    }.sortedBy { student ->
        student.studentName
    }

    val availableStudents = students.filter { student ->
        subjectGrades.none { grade ->
            grade.studentId == student.studentId
        }
    }.sortedBy { student ->
        student.studentName
    }

    if (showAddStudentDialog && subject != null) {
        AddStudentToSubjectDialog(
            subjectName = subject.subjectName,
            students = availableStudents,
            onDismiss = {
                showAddStudentDialog = false
            },
            onStudentSelected = { student ->
                gradeViewModel.addGrade(
                    grade = GradeModel(
                        studentId = student.studentId,
                        subjectId = subjectId,
                        marks = 0.0,
                        gradeLetter = "",
                        remarks = "Added to subject",
                        status = "Pending",
                        createdByTeacherId = subject.teacherId,
                        createdAt = System.currentTimeMillis()
                    ),
                    onSuccess = {
                        gradeViewModel.loadGradesBySubjectId(subjectId)
                    }
                )

                showAddStudentDialog = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGrey)
            .navigationBarsPadding()
    ) {
        TopBar(
            title = "Subject Detail",
            showBack = true,
            onBackClick = onBackClick,
            showIcon = false
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = 32.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            when {
                subjectUiState.isLoading || isStudentGradeLoading -> {
                    item {
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
                }

                subject == null -> {
                    item {
                        SgrmsEmptyStateCard(
                            icon = Icons.Default.MenuBook,
                            title = "Subject not found",
                            message = when {
                                subjectUiState.errorMessage.isNotBlank() -> {
                                    subjectUiState.errorMessage
                                }

                                studentGradeErrorMessage.isNotBlank() -> {
                                    studentGradeErrorMessage
                                }

                                else -> {
                                    "The selected subject may have been deleted."
                                }
                            }
                        )
                    }
                }

                else -> {
                    item {
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
                                                imageVector = Icons.Default.MenuBook,
                                                contentDescription = "Subject Icon",
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
                                            text = subject.subjectName.ifBlank { "Unknown Subject" },
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Bold
                                        )

                                        Text(
                                            text = subject.subjectCode.ifBlank { "-" },
                                            fontSize = 15.sp,
                                            color = TextGrey
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                HorizontalDivider(color = BackgroundGrey)

                                Spacer(modifier = Modifier.height(18.dp))

                                SubjectDetailItem(
                                    icon = Icons.Default.Tag,
                                    label = "Subject Code",
                                    value = subject.subjectCode.ifBlank { "-" }
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                SubjectDetailItem(
                                    icon = Icons.Default.MenuBook,
                                    label = "Subject Name",
                                    value = subject.subjectName.ifBlank { "Unknown Subject" }
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                SubjectDetailItem(
                                    icon = Icons.Default.Person,
                                    label = "Teacher Name",
                                    value = subject.teacherName.ifBlank { "Not assigned" }
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                SubjectDetailItem(
                                    icon = Icons.Default.Group,
                                    label = "Category",
                                    value = subject.category.ifBlank { "-" }
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                SubjectDetailItem(
                                    icon = Icons.Default.Group,
                                    label = "Total Students",
                                    value = studentsTakingSubject.size.toString()
                                )

                                Spacer(modifier = Modifier.height(26.dp))

                                Button(
                                    onClick = {
                                        showAddStudentDialog = true
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = PrimaryBlue
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PersonAdd,
                                        contentDescription = "Add Student",
                                        tint = Color.White
                                    )

                                    Spacer(modifier = Modifier.size(8.dp))

                                    Text(
                                        text = "Add Student to This Subject",
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = onEditClick,
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
                                            contentDescription = "Edit Subject",
                                            tint = PrimaryBlue,
                                            modifier = Modifier.size(20.dp)
                                        )

                                        Spacer(modifier = Modifier.size(8.dp))

                                        Text(
                                            text = "Edit",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    OutlinedButton(
                                        onClick = onViewStudentsClick,
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(52.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = PrimaryBlue
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Visibility,
                                            contentDescription = "View Students",
                                            tint = PrimaryBlue,
                                            modifier = Modifier.size(20.dp)
                                        )

                                        Spacer(modifier = Modifier.size(8.dp))

                                        Text(
                                            text = "Students",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (
                        subjectUiState.errorMessage.isNotBlank() ||
                        studentGradeErrorMessage.isNotBlank()
                    ) {
                        item {
                            Text(
                                text = subjectUiState.errorMessage.ifBlank {
                                    studentGradeErrorMessage
                                },
                                color = Color.Red,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    item {
                        Text(
                            text = "Students Taking This Subject (${studentsTakingSubject.size})",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (studentsTakingSubject.isEmpty()) {
                        item {
                            SgrmsEmptyStateCard(
                                icon = Icons.Default.Group,
                                title = "No student found",
                                message = "No student has been added to this subject yet."
                            )
                        }
                    } else {
                        items(
                            items = studentsTakingSubject,
                            key = { student ->
                                student.studentId
                            }
                        ) { student ->
                            SubjectStudentCard(student = student)
                        }
                    }
                }
            }
        }
    }
}
