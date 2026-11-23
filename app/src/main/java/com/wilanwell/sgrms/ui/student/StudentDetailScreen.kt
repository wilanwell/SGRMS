package com.wilanwell.sgrms.ui.student

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wilanwell.sgrms.viewmodel.GradeFirebaseViewModel
import com.wilanwell.sgrms.viewmodel.StudentFirebaseViewModel
import com.wilanwell.sgrms.viewmodel.SubjectFirebaseViewModel
import com.wilanwell.sgrms.utils.GradeCalculator
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.wilanwell.sgrms.ui.components.SgrmsAvatar
import com.wilanwell.sgrms.ui.components.SgrmsStatusChip
import com.wilanwell.sgrms.ui.components.ChipTone
import com.wilanwell.sgrms.ui.components.toneForStatus
import com.wilanwell.sgrms.ui.components.toneForGrade
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Home
import com.wilanwell.sgrms.ui.components.SgrmsDetailScreen
import com.wilanwell.sgrms.ui.components.SgrmsDetailHero
import com.wilanwell.sgrms.ui.components.SgrmsInfoSection
import com.wilanwell.sgrms.ui.components.SgrmsInfoRow
import com.wilanwell.sgrms.ui.components.SgrmsRowDivider
import com.wilanwell.sgrms.ui.components.SgrmsMarkRow
import com.wilanwell.sgrms.ui.components.SgrmsEmptyState
import com.wilanwell.sgrms.ui.components.SgrmsPrimaryButton
import com.wilanwell.sgrms.ui.components.SgrmsSecondaryButton

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

    SgrmsDetailScreen(
        title = "Student",
        onBack = onBackClick,
        isLoading = isLoading && student == null,
        errorMessage = if (student != null) errorMessage else "",
        bottomBar = if (student != null) {
            {
                SgrmsSecondaryButton(text = "Edit", icon = Icons.Default.Edit, onClick = onEditClick, modifier = Modifier.weight(1f))
                SgrmsPrimaryButton(text = "Grades", icon = Icons.Default.Assessment, onClick = onViewGradeClick, modifier = Modifier.weight(1f))
            }
        } else {
            null
        }
    ) {
        if (student == null) {
            SgrmsEmptyState(
                title = "Student not found",
                message = errorMessage.ifBlank { "The selected student record could not be loaded." },
                icon = Icons.Default.Person
            )
            return@SgrmsDetailScreen
        }

        SgrmsDetailHero(
            title = studentName,
            subtitle = "$studentCode · $className",
            leading = {
                SgrmsAvatar(
                    name = studentName,
                    size = 88.dp,
                    imageUrl = student.profileImageUrl,
                    legacyImageBase64 = student.profileImageBase64
                )
            }
        ) {
            SgrmsStatusChip(status, toneForStatus(status))
            SgrmsStatusChip(
                if (subjectResults.isEmpty()) "No grades" else "Overall $overallGrade",
                if (subjectResults.isEmpty()) ChipTone.Neutral else toneForGrade(overallGrade)
            )
        }

        SgrmsInfoSection("Contact") {
            SgrmsInfoRow(icon = Icons.Default.Email, label = "Email", value = email)
            SgrmsRowDivider()
            SgrmsInfoRow(icon = Icons.Default.Phone, label = "Phone", value = phoneNumber)
            SgrmsRowDivider()
            SgrmsInfoRow(icon = Icons.Default.Person, label = "Gender", value = gender)
        }

        if (student.guardianName.isNotBlank() || student.guardianPhone.isNotBlank() || student.address.isNotBlank() || student.dateOfBirth.isNotBlank()) {
            SgrmsInfoSection("Personal") {
                SgrmsInfoRow(icon = Icons.Default.Cake, label = "Date of birth", value = student.dateOfBirth)
                SgrmsRowDivider()
                SgrmsInfoRow(icon = Icons.Default.FamilyRestroom, label = "Guardian", value = listOf(student.guardianName, student.guardianPhone).filter { it.isNotBlank() }.joinToString(" · "))
                SgrmsRowDivider()
                SgrmsInfoRow(icon = Icons.Default.Home, label = "Address", value = student.address)
            }
        }

        SgrmsInfoSection(
            title = "Subjects",
            trailing = {
                if (subjectResults.isNotEmpty()) {
                    Text("Avg ${"%.1f".format(averageMarks)}%", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        ) {
            if (subjectResults.isEmpty()) {
                Text(
                    "No grades recorded yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                subjectResults.forEachIndexed { index, subject ->
                    if (index > 0) SgrmsRowDivider()
                    SgrmsMarkRow(title = subject.subjectName, subtitle = subject.subjectCode, marks = subject.marks, grade = subject.grade)
                }
            }
        }
    }
}
