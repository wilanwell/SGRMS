package com.example.sgrms.ui.admin

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sgrms.data.model.StudentModel
import com.example.sgrms.ui.components.SgrmsEmptyStateCard
import com.example.sgrms.ui.components.SgrmsInfoRow
import com.example.sgrms.ui.components.TopBar
import com.example.sgrms.ui.theme.ColorBackground
import com.example.sgrms.ui.theme.ColorSurface
import com.example.sgrms.ui.theme.ColorPrimary
import com.example.sgrms.ui.theme.ColorOnSurfaceVariant
import com.example.sgrms.viewmodel.ClassFirebaseViewModel
import com.example.sgrms.viewmodel.StudentFirebaseViewModel
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.sgrms.ui.theme.SgrmsColors

@Composable
fun ClassDetailScreen(
    classId: String = "",
    onBackClick: () -> Unit = {},
    onAddStudentClick: () -> Unit = {}
) {
    val classViewModel: ClassFirebaseViewModel = hiltViewModel()

    val studentViewModel: StudentFirebaseViewModel = hiltViewModel()

    val classUiState by classViewModel.uiState.collectAsState()
    val studentUiState by studentViewModel.uiState.collectAsState()

    var showAddStudentDialog by remember { mutableStateOf(false) }

    var showRemoveDialog by remember { mutableStateOf(false) }
    var selectedStudentToRemove by remember { mutableStateOf<StudentModel?>(null) }

    LaunchedEffect(classId) {
        if (classId.isNotBlank()) {
            classViewModel.loadClassById(classId)
            studentViewModel.loadStudents()
        }
    }

    val schoolClass = classUiState.selectedClass
    val students = studentUiState.students

    val classStudents = students.filter { student ->
        student.classId == schoolClass?.classId ||
                student.className == schoolClass?.className
    }

    val availableStudents = students.filter { student ->
        student.classId.isBlank() ||
                student.className.isBlank() ||
                student.className == "Not assigned"
    }

    val currentClass = schoolClass

    if (showAddStudentDialog && currentClass != null) {
        AddStudentToClassDialog(
            className = currentClass.className,
            students = availableStudents,
            onDismiss = {
                showAddStudentDialog = false
            },
            onStudentSelected = { student ->
                val updatedStudent = student.copy(
                    classId = currentClass.classId,
                    className = currentClass.className
                )

                studentViewModel.updateStudent(
                    student = updatedStudent,
                    onSuccess = {
                        studentViewModel.loadStudents()
                        showAddStudentDialog = false
                    }
                )
            }
        )
    }

    if (showRemoveDialog && selectedStudentToRemove != null) {
        RemoveStudentFromClassDialog(
            student = selectedStudentToRemove!!,
            onDismiss = {
                showRemoveDialog = false
                selectedStudentToRemove = null
            },
            onConfirmRemove = {
                val studentToRemove = selectedStudentToRemove

                if (studentToRemove != null) {
                    val updatedStudent = studentToRemove.copy(
                        classId = "",
                        className = ""
                    )

                    studentViewModel.updateStudent(
                        student = updatedStudent,
                        onSuccess = {
                            studentViewModel.loadStudents()
                            showRemoveDialog = false
                            selectedStudentToRemove = null
                        }
                    )
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorBackground)
            .navigationBarsPadding()
    ) {
        TopBar(
            title = "Class Detail",
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
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = ColorSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(58.dp)
                                .clip(CircleShape)
                                .background(ColorPrimary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = "Class",
                                tint = ColorPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.size(14.dp))

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = schoolClass?.className ?: "Unknown Class",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "Academic Year: ${schoolClass?.academicYear ?: "-"}",
                                fontSize = 14.sp,
                                color = ColorOnSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    HorizontalDivider(color = ColorBackground)

                    Spacer(modifier = Modifier.height(14.dp))

                    SgrmsInfoRow(
                        icon = Icons.Default.Person,
                        label = "Class Teacher",
                        value = schoolClass?.classTeacherName?.ifBlank {
                            "Not assigned"
                        } ?: "Not assigned"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    SgrmsInfoRow(
                        icon = Icons.Default.Group,
                        label = "Total Students",
                        value = classStudents.size.toString()
                    )

                    if (!schoolClass?.description.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))

                        SgrmsInfoRow(
                            icon = Icons.Default.MenuBook,
                            label = "Description",
                            value = schoolClass?.description ?: "-"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    showAddStudentDialog = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ColorPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = "Add Student",
                    tint = Color.White
                )

                Spacer(modifier = Modifier.size(8.dp))

                Text(
                    text = "Add Student to This Class",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (classUiState.errorMessage.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = classUiState.errorMessage,
                    color = SgrmsColors.Error,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            if (studentUiState.errorMessage.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = studentUiState.errorMessage,
                    color = SgrmsColors.Error,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Students in Class (${classStudents.size})",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (classStudents.isEmpty()) {
                SgrmsEmptyStateCard(
                    icon = Icons.Default.Group,
                    title = "No student in this class",
                    message = "Tap Add Student to add a student into this class."
                )
            } else {
                classStudents.forEach { student ->
                    ClassStudentCard(
                        student = student,
                        onRemoveClick = {
                            selectedStudentToRemove = student
                            showRemoveDialog = true
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}
