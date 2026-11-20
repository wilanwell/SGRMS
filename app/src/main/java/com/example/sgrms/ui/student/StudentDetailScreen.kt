package com.example.sgrms.ui.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.sgrms.ui.components.ProfileImageView
import com.example.sgrms.ui.components.SgrmsEmptyStateCard
import com.example.sgrms.ui.components.TopBar
import com.example.sgrms.ui.theme.BackgroundGrey
import com.example.sgrms.ui.theme.CardWhite
import com.example.sgrms.ui.theme.PrimaryBlue
import com.example.sgrms.ui.theme.SuccessGreen
import com.example.sgrms.ui.theme.TextGrey
import com.example.sgrms.viewmodel.GradeFirebaseViewModel
import com.example.sgrms.viewmodel.GradeFirebaseViewModelFactory
import com.example.sgrms.viewmodel.StudentFirebaseViewModel
import com.example.sgrms.viewmodel.StudentFirebaseViewModelFactory
import com.example.sgrms.viewmodel.SubjectFirebaseViewModel
import com.example.sgrms.viewmodel.SubjectFirebaseViewModelFactory

data class StudentDetailSubjectItem(
    val subjectName: String,
    val subjectCode: String,
    val marks: Double,
    val grade: String
)

@Composable
fun StudentDetailScreen(
    studentId: String = "",
    onBackClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    onViewGradeClick: () -> Unit = {}
) {
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

    val subjectResults = grades.map { grade ->
        val subject = subjects.find { subject ->
            subject.subjectId == grade.subjectId
        }

        StudentDetailSubjectItem(
            subjectName = subject?.subjectName ?: "Unknown Subject",
            subjectCode = subject?.subjectCode ?: "-",
            marks = grade.marks,
            grade = grade.gradeLetter
        )
    }.sortedBy { subject ->
        subject.subjectName
    }

    val studentName = student?.studentName ?: "Unknown Student"
    val studentCode = student?.studentCode ?: "-"
    val email = student?.email ?: "-"
    val phoneNumber = student?.phoneNumber ?: "-"
    val gender = student?.gender ?: "-"
    val className = student?.className ?: "-"
    val status = student?.status?.ifBlank { "Active" } ?: "Active"

    val totalMarks = subjectResults.sumOf { subject ->
        subject.marks
    }

    val averageMarks = if (subjectResults.isNotEmpty()) {
        totalMarks / subjectResults.size
    } else {
        0.0
    }

    val overallGrade = when {
        averageMarks >= 80 -> "A"
        averageMarks >= 70 -> "B"
        averageMarks >= 60 -> "C"
        averageMarks >= 50 -> "D"
        subjectResults.isEmpty() -> "-"
        else -> "E"
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGrey)
            .navigationBarsPadding()
    ) {
        TopBar(
            title = "Student Detail",
            showBack = true,
            onBackClick = onBackClick,
            showIcon = false
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = 32.dp
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

                student == null -> {
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

                else -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            ProfileImageView(
                                imageBase64 = student.profileImageBase64,
                                size = 96
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = studentName,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "$studentCode • $className",
                                fontSize = 14.sp,
                                color = TextGrey
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = status,
                                fontSize = 14.sp,
                                color = if (status == "Active") {
                                    SuccessGreen
                                } else {
                                    Color.Red
                                },
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp)
                        ) {
                            Text(
                                text = "Student Information",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            StudentDetailInfoRow(
                                icon = Icons.Default.Badge,
                                title = "Student Code",
                                value = studentCode
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 12.dp),
                                color = BackgroundGrey
                            )

                            StudentDetailInfoRow(
                                icon = Icons.Default.Email,
                                title = "Email",
                                value = email.ifBlank { "Not specified" }
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 12.dp),
                                color = BackgroundGrey
                            )

                            StudentDetailInfoRow(
                                icon = Icons.Default.Phone,
                                title = "Phone Number",
                                value = phoneNumber.ifBlank { "Not specified" }
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 12.dp),
                                color = BackgroundGrey
                            )

                            StudentDetailInfoRow(
                                icon = if (gender == "Male") {
                                    Icons.Default.Male
                                } else {
                                    Icons.Default.Female
                                },
                                title = "Gender",
                                value = gender.ifBlank { "Not specified" }
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 12.dp),
                                color = BackgroundGrey
                            )

                            StudentDetailInfoRow(
                                icon = Icons.Default.Class,
                                title = "Class",
                                value = className.ifBlank { "Not assigned" }
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 12.dp),
                                color = BackgroundGrey
                            )

                            StudentDetailInfoRow(
                                icon = Icons.Default.TaskAlt,
                                title = "Status",
                                value = status.ifBlank { "Active" }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = PrimaryBlue),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
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
                                    text = "Average",
                                    color = Color.White,
                                    fontSize = 15.sp
                                )

                                Text(
                                    text = "%.2f%%".format(averageMarks),
                                    color = Color.White,
                                    fontSize = 30.sp,
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
                                    fontSize = 30.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

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
                                    imageVector = Icons.Default.School,
                                    contentDescription = "Academic Performance",
                                    tint = PrimaryBlue
                                )

                                Spacer(modifier = Modifier.size(8.dp))

                                Text(
                                    text = "Academic Performance",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            if (subjectResults.isEmpty()) {
                                Text(
                                    text = "No grade record found for this student.",
                                    fontSize = 14.sp,
                                    color = TextGrey
                                )
                            } else {
                                subjectResults.forEach { subject ->
                                    StudentDetailSubjectRow(subject = subject)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

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
                                contentDescription = "Edit Student",
                                tint = PrimaryBlue
                            )

                            Spacer(modifier = Modifier.size(8.dp))

                            Text(
                                text = "Edit",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = onViewGradeClick,
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryBlue
                            )
                        ) {
                            Text(
                                text = "View Grades",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(36.dp))
                }
            }
        }
    }
}

@Composable
fun StudentDetailInfoRow(
    icon: ImageVector,
    title: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = PrimaryBlue,
            modifier = Modifier.size(26.dp)
        )

        Spacer(modifier = Modifier.size(14.dp))

        Column {
            Text(
                text = title,
                fontSize = 14.sp,
                color = TextGrey
            )

            Text(
                text = value.ifBlank { "-" },
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun StudentDetailSubjectRow(
    subject: StudentDetailSubjectItem
) {
    Column(
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = subject.subjectName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = "${subject.subjectCode} • Grade ${subject.grade.ifBlank { "-" }}",
                    fontSize = 13.sp,
                    color = getStudentGradeColor(subject.marks),
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "%.2f%%".format(subject.marks),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = getStudentGradeColor(subject.marks)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = {
                (subject.marks / 100.0).toFloat().coerceIn(0f, 1f)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = getStudentGradeColor(subject.marks),
            trackColor = BackgroundGrey
        )
    }
}

fun getStudentGradeColor(
    marks: Double
): Color {
    return when {
        marks >= 80 -> SuccessGreen
        marks >= 70 -> PrimaryBlue
        marks >= 60 -> Color(0xFFFF9800)
        marks >= 50 -> Color(0xFFFF5722)
        else -> Color.Red
    }
}