package com.wilanwell.sgrms.ui.grade

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wilanwell.sgrms.data.model.GradeModel
import com.wilanwell.sgrms.ui.components.SgrmsPrimaryButton
import com.wilanwell.sgrms.viewmodel.GradeFirebaseViewModel
import com.wilanwell.sgrms.viewmodel.StudentFirebaseViewModel
import com.wilanwell.sgrms.viewmodel.SubjectFirebaseViewModel
import com.wilanwell.sgrms.utils.GradeCalculator
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.wilanwell.sgrms.ui.components.SgrmsAvatar
import com.wilanwell.sgrms.ui.components.SgrmsStatusChip
import com.wilanwell.sgrms.ui.components.ChipTone
import com.wilanwell.sgrms.ui.components.toneForGrade
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.filled.EditNote
import com.wilanwell.sgrms.ui.components.SgrmsDetailScreen
import com.wilanwell.sgrms.ui.components.SgrmsDetailHero
import com.wilanwell.sgrms.ui.components.SgrmsInfoSection
import com.wilanwell.sgrms.ui.components.SgrmsRowDivider
import com.wilanwell.sgrms.ui.components.SgrmsMarkRow
import com.wilanwell.sgrms.ui.components.SgrmsSecondaryButton
import com.wilanwell.sgrms.ui.components.SgrmsEmptyState
import androidx.compose.material.icons.filled.Person

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

    SgrmsDetailScreen(
        title = "Grades",
        onBack = onBackClick,
        isLoading = isLoading && student == null,
        errorMessage = errorMessage,
        bottomBar = if (student == null) null else {
            {
            SgrmsSecondaryButton(text = "Report", icon = Icons.Default.Summarize, onClick = onGenerateReportClick, modifier = Modifier.weight(1f))
            SgrmsPrimaryButton(text = "Enter marks", icon = Icons.Default.EditNote, onClick = onAddGradeClick, modifier = Modifier.weight(1f))
            }
        }
    ) {
        if (student == null) {
            SgrmsEmptyState(title = "Student not found", message = "This student could not be loaded. It may have been deleted.", icon = Icons.Default.Person)
            return@SgrmsDetailScreen
        }

        SgrmsDetailHero(
            title = studentName,
            subtitle = className,
            leading = {
                SgrmsAvatar(
                    name = studentName,
                    size = 72.dp,
                    imageUrl = student?.profileImageUrl.orEmpty(),
                    legacyImageBase64 = student?.profileImageBase64.orEmpty()
                )
            }
        ) {
            SgrmsStatusChip(if (completedGrades.isEmpty()) "No grades" else "Overall $overallGrade", if (completedGrades.isEmpty()) ChipTone.Neutral else toneForGrade(overallGrade))
            SgrmsStatusChip("Avg ${"%.1f".format(averageMarks)}%", ChipTone.Primary)
        }

        SgrmsInfoSection(title = "Subjects ($subjectCount graded of ${allGrades.size})") {
            if (gradeDisplayList.isEmpty()) {
                Text(
                    "No grade records yet. Use Enter marks to add them.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                gradeDisplayList.forEachIndexed { index, item ->
                    if (index > 0) SgrmsRowDivider()
                    val complete = item.grade.status.equals("Complete", ignoreCase = true)
                    SgrmsMarkRow(
                        title = item.subjectName,
                        subtitle = listOf(item.subjectCode, item.grade.remarks).filter { it.isNotBlank() }.joinToString(" · "),
                        marks = if (complete) item.grade.marks else null,
                        grade = if (complete) item.grade.gradeLetter.ifBlank { com.wilanwell.sgrms.utils.GradeCalculator.gradeLetter(item.grade.marks) } else "Pending"
                    )
                }
            }
        }
    }
}

