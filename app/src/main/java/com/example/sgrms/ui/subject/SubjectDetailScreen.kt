package com.example.sgrms.ui.subject

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.unit.dp
import com.example.sgrms.data.model.GradeModel
import com.example.sgrms.viewmodel.GradeFirebaseViewModel
import com.example.sgrms.viewmodel.StudentFirebaseViewModel
import com.example.sgrms.viewmodel.SubjectFirebaseViewModel
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.sgrms.ui.components.SgrmsAvatar
import com.example.sgrms.ui.components.SgrmsIconTile
import com.example.sgrms.ui.components.SgrmsStatusChip
import com.example.sgrms.ui.components.ChipTone
import com.example.sgrms.ui.components.toneForGrade
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import com.example.sgrms.ui.components.SgrmsDetailScreen
import com.example.sgrms.ui.components.SgrmsDetailHero
import com.example.sgrms.ui.components.SgrmsInfoSection
import com.example.sgrms.ui.components.SgrmsInfoRow
import com.example.sgrms.ui.components.SgrmsRowDivider
import com.example.sgrms.ui.components.SgrmsEmptyState
import com.example.sgrms.ui.components.SgrmsListRow
import com.example.sgrms.ui.components.SgrmsPrimaryButton
import com.example.sgrms.ui.components.SgrmsSecondaryButton

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

    val gradeByStudent = subjectGrades.associateBy { it.studentId }

    SgrmsDetailScreen(
        title = "Subject",
        onBack = onBackClick,
        isLoading = subjectUiState.isLoading && subject == null,
        errorMessage = subjectUiState.errorMessage.ifBlank { studentGradeErrorMessage },
        bottomBar = if (subject != null) {
            {
                SgrmsSecondaryButton(text = "Edit", icon = Icons.Default.Edit, onClick = onEditClick, modifier = Modifier.weight(1f))
                SgrmsPrimaryButton(text = "Add student", icon = Icons.Default.PersonAdd, onClick = { showAddStudentDialog = true }, modifier = Modifier.weight(1f))
            }
        } else {
            null
        }
    ) {
        if (subject == null) {
            SgrmsEmptyState(title = "Subject not found", message = "The selected subject could not be loaded.", icon = Icons.AutoMirrored.Filled.MenuBook)
            return@SgrmsDetailScreen
        }

        SgrmsDetailHero(
            title = subject.subjectName.ifBlank { "Unnamed subject" },
            subtitle = subject.subjectCode,
            leading = {
                SgrmsIconTile(
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    tint = MaterialTheme.colorScheme.primary,
                    container = MaterialTheme.colorScheme.primaryContainer,
                    size = 72.dp
                )
            }
        ) {
            if (subject.category.isNotBlank()) SgrmsStatusChip(subject.category, ChipTone.Neutral)
            SgrmsStatusChip("${studentsTakingSubject.size} students", ChipTone.Primary)
        }

        SgrmsInfoSection("Details") {
            SgrmsInfoRow(icon = Icons.Default.Person, label = "Teacher", value = subject.teacherName.ifBlank { "Not assigned" })
            SgrmsRowDivider()
            SgrmsInfoRow(icon = Icons.Default.Category, label = "Category", value = subject.category.ifBlank { "-" })
        }

        SgrmsInfoSection(
            title = "Students taking this subject",
            trailing = {
                androidx.compose.material3.TextButton(onClick = onViewStudentsClick) { Text("All students") }
            }
        ) {
            when {
                isStudentGradeLoading -> Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                studentsTakingSubject.isEmpty() -> Text(
                    "No students yet. Use Add student to enrol one.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                else -> studentsTakingSubject.forEachIndexed { index, student ->
                    if (index > 0) SgrmsRowDivider()
                    val grade = gradeByStudent[student.studentId]
                    val complete = grade?.status.equals("Complete", ignoreCase = true)
                    SgrmsListRow(
                        title = student.studentName,
                        subtitle = listOf(student.studentCode, student.className).filter { it.isNotBlank() }.joinToString(" · "),
                        leading = { SgrmsAvatar(name = student.studentName, size = 40.dp) },
                        trailing = {
                            if (grade != null && complete) {
                                val letter = grade.gradeLetter.ifBlank { com.example.sgrms.utils.GradeCalculator.gradeLetter(grade.marks) }
                                SgrmsStatusChip("$letter · ${"%.0f".format(grade.marks)}", toneForGrade(letter))
                            } else {
                                SgrmsStatusChip("Pending", ChipTone.Warning)
                            }
                        }
                    )
                }
            }
        }
    }
}
