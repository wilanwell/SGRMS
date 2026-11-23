package com.wilanwell.sgrms.ui.subject

import com.wilanwell.sgrms.R
import androidx.compose.ui.res.stringResource
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
import com.wilanwell.sgrms.data.model.GradeModel
import com.wilanwell.sgrms.viewmodel.GradeFirebaseViewModel
import com.wilanwell.sgrms.viewmodel.StudentFirebaseViewModel
import com.wilanwell.sgrms.viewmodel.SubjectFirebaseViewModel
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.wilanwell.sgrms.ui.components.SgrmsAvatar
import com.wilanwell.sgrms.ui.components.SgrmsIconTile
import com.wilanwell.sgrms.ui.components.SgrmsStatusChip
import com.wilanwell.sgrms.ui.components.ChipTone
import com.wilanwell.sgrms.ui.components.toneForGrade
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import com.wilanwell.sgrms.ui.components.SgrmsDetailScreen
import com.wilanwell.sgrms.ui.components.SgrmsDetailHero
import com.wilanwell.sgrms.ui.components.SgrmsInfoSection
import com.wilanwell.sgrms.ui.components.SgrmsInfoRow
import com.wilanwell.sgrms.ui.components.SgrmsRowDivider
import com.wilanwell.sgrms.ui.components.SgrmsEmptyState
import com.wilanwell.sgrms.ui.components.SgrmsListRow
import com.wilanwell.sgrms.ui.components.SgrmsPrimaryButton
import com.wilanwell.sgrms.ui.components.SgrmsSecondaryButton

@Composable
fun SubjectDetailScreen(
    subjectId: String = "",
    onBackClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onViewStudentsClick: () -> Unit = {}
) {
    val addedToSubjectText = stringResource(R.string.added_to_subject)
    val noStudentsYetUseAddStudentText = stringResource(R.string.no_students_yet_use_add_student)
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
                        remarks = addedToSubjectText,
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
        title = stringResource(R.string.subject),
        onBack = onBackClick,
        isLoading = subjectUiState.isLoading && subject == null,
        errorMessage = subjectUiState.errorMessage.ifBlank { studentGradeErrorMessage },
        bottomBar = if (subject != null) {
            {
                SgrmsSecondaryButton(text = stringResource(R.string.edit), icon = Icons.Default.Edit, onClick = onEditClick, modifier = Modifier.weight(1f))
                SgrmsPrimaryButton(text = stringResource(R.string.add_student), icon = Icons.Default.PersonAdd, onClick = { showAddStudentDialog = true }, modifier = Modifier.weight(1f))
            }
        } else {
            null
        }
    ) {
        if (subject == null) {
            SgrmsEmptyState(title = stringResource(R.string.subject_not_found), message = stringResource(R.string.the_selected_subject_could_not_be), icon = Icons.AutoMirrored.Filled.MenuBook)
            return@SgrmsDetailScreen
        }

        SgrmsDetailHero(
            title = subject.subjectName.ifBlank { stringResource(R.string.unnamed_subject) },
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
            SgrmsStatusChip(stringResource(R.string.students_3, studentsTakingSubject.size), ChipTone.Primary)
        }

        SgrmsInfoSection(stringResource(R.string.details)) {
            SgrmsInfoRow(icon = Icons.Default.Person, label = stringResource(R.string.teacher), value = subject.teacherName.ifBlank { stringResource(R.string.not_assigned) })
            SgrmsRowDivider()
            SgrmsInfoRow(icon = Icons.Default.Category, label = stringResource(R.string.category), value = subject.category.ifBlank { "-" })
        }

        SgrmsInfoSection(
            title = stringResource(R.string.students_taking_this_subject),
            trailing = {
                androidx.compose.material3.TextButton(onClick = onViewStudentsClick) { Text(stringResource(R.string.all_students)) }
            }
        ) {
            when {
                isStudentGradeLoading -> Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                studentsTakingSubject.isEmpty() -> Text(
                    noStudentsYetUseAddStudentText,
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
                        leading = { SgrmsAvatar(name = student.studentName, size = 40.dp, imageUrl = student.profileImageUrl, legacyImageBase64 = student.profileImageBase64) },
                        trailing = {
                            if (grade != null && complete) {
                                val letter = grade.gradeLetter.ifBlank { com.wilanwell.sgrms.utils.GradeCalculator.gradeLetter(grade.marks) }
                                SgrmsStatusChip("$letter · ${"%.0f".format(grade.marks)}", toneForGrade(letter))
                            } else {
                                SgrmsStatusChip(stringResource(R.string.pending), ChipTone.Warning)
                            }
                        }
                    )
                }
            }
        }
    }
}
