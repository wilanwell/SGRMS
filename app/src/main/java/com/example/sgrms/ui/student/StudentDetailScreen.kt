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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sgrms.ui.components.ProfileImageView
import com.example.sgrms.ui.components.SgrmsEmptyStateCard
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun StudentDetailScreen(
    studentId: String = "",
    onBackClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    onViewGradeClick: () -> Unit = {}
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

    val overallGrade = GradeCalculator.overallGrade(
        averageMarks = averageMarks,
        hasGrades = subjectResults.isNotEmpty()
    )

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
            .background(ColorBackground)
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
                            color = ColorPrimary
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
                        colors = CardDefaults.cardColors(containerColor = ColorSurface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            ProfileImageView(
                                imageUrl = student.profileImageUrl,
                                legacyImageBase64 = student.profileImageBase64,
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
                                color = ColorOnSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = status,
                                fontSize = 14.sp,
                                color = if (status == "Active") {
                                    SgrmsColors.Success
                                } else {
                                    SgrmsColors.Error
                                },
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = ColorSurface),
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
                                color = ColorBackground
                            )

                            StudentDetailInfoRow(
                                icon = Icons.Default.Email,
                                title = "Email",
                                value = email.ifBlank { "Not specified" }
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 12.dp),
                                color = ColorBackground
                            )

                            StudentDetailInfoRow(
                                icon = Icons.Default.Phone,
                                title = "Phone Number",
                                value = phoneNumber.ifBlank { "Not specified" }
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 12.dp),
                                color = ColorBackground
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
                                color = ColorBackground
                            )

                            StudentDetailInfoRow(
                                icon = Icons.Default.Class,
                                title = "Class",
                                value = className.ifBlank { "Not assigned" }
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 12.dp),
                                color = ColorBackground
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
                        colors = CardDefaults.cardColors(containerColor = ColorPrimary),
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
                        colors = CardDefaults.cardColors(containerColor = ColorSurface),
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
                                    tint = ColorPrimary
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
                                    color = ColorOnSurfaceVariant
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
                                contentColor = ColorPrimary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Student",
                                tint = ColorPrimary
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
                                containerColor = ColorPrimary
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
