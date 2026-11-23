package com.wilanwell.sgrms.ui.admin

import com.wilanwell.sgrms.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wilanwell.sgrms.data.model.StudentModel
import com.wilanwell.sgrms.ui.components.SgrmsInfoRow
import com.wilanwell.sgrms.viewmodel.ClassFirebaseViewModel
import com.wilanwell.sgrms.viewmodel.StudentFirebaseViewModel
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.wilanwell.sgrms.ui.theme.SgrmsColors
import com.wilanwell.sgrms.ui.components.RowAction
import com.wilanwell.sgrms.ui.components.SgrmsAvatar
import com.wilanwell.sgrms.ui.components.SgrmsIconTile
import com.wilanwell.sgrms.ui.components.SgrmsOverflowMenu
import com.wilanwell.sgrms.ui.components.SgrmsStatusChip
import com.wilanwell.sgrms.ui.components.ChipTone
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.automirrored.filled.Notes
import com.wilanwell.sgrms.ui.components.SgrmsDetailScreen
import com.wilanwell.sgrms.ui.components.SgrmsDetailHero
import com.wilanwell.sgrms.ui.components.SgrmsInfoSection
import com.wilanwell.sgrms.ui.components.SgrmsRowDivider
import com.wilanwell.sgrms.ui.components.SgrmsEmptyState
import com.wilanwell.sgrms.ui.components.SgrmsListRow
import com.wilanwell.sgrms.ui.components.SgrmsPrimaryButton

@Composable
fun ClassDetailScreen(
    classId: String = "",
    onBackClick: () -> Unit = {},
    onAddStudentClick: () -> Unit = {}
) {
    val academicYear2Text = stringResource(R.string.academic_year_value)
    val noStudentsInThisClassYetText = stringResource(R.string.no_students_in_this_class_yet)
    val students3Text = stringResource(R.string.students_count)
    val t1StudentText = stringResource(R.string.t_1_student)
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

    SgrmsDetailScreen(
        title = stringResource(R.string.class_label),
        onBack = onBackClick,
        isLoading = classUiState.isLoading && schoolClass == null,
        errorMessage = classUiState.errorMessage.ifBlank { studentUiState.errorMessage },
        bottomBar = if (schoolClass != null) {
            {
                SgrmsPrimaryButton(
                    text = stringResource(R.string.add_student_to_class),
                    icon = Icons.Default.PersonAdd,
                    onClick = { showAddStudentDialog = true },
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            null
        }
    ) {
        if (schoolClass == null) {
            SgrmsEmptyState(title = stringResource(R.string.class_not_found), message = stringResource(R.string.the_selected_class_could_not_be), icon = Icons.Default.School)
            return@SgrmsDetailScreen
        }

        SgrmsDetailHero(
            title = schoolClass.className.ifBlank { stringResource(R.string.unnamed_class) },
            subtitle = schoolClass.academicYear.ifBlank { null }?.let { academicYear2Text.format(it) },
            leading = {
                SgrmsIconTile(
                    icon = Icons.Default.School,
                    tint = com.wilanwell.sgrms.ui.theme.SgrmsColors.Pink,
                    container = com.wilanwell.sgrms.ui.theme.SgrmsColors.PinkContainer,
                    size = 72.dp
                )
            }
        ) {
            SgrmsStatusChip(if (classStudents.size == 1) t1StudentText else students3Text.format(classStudents.size), ChipTone.Primary)
        }

        SgrmsInfoSection(stringResource(R.string.details)) {
            SgrmsInfoRow(icon = Icons.Default.Person, label = stringResource(R.string.class_teacher), value = schoolClass.classTeacherName.ifBlank { stringResource(R.string.not_assigned) })
            if (schoolClass.description.isNotBlank()) {
                SgrmsRowDivider()
                SgrmsInfoRow(icon = Icons.AutoMirrored.Filled.Notes, label = stringResource(R.string.description), value = schoolClass.description)
            }
        }

        SgrmsInfoSection(stringResource(R.string.students)) {
            if (classStudents.isEmpty()) {
                Text(
                    noStudentsInThisClassYetText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                classStudents.sortedBy { it.studentName }.forEachIndexed { index, student ->
                    if (index > 0) SgrmsRowDivider()
                    SgrmsListRow(
                        title = student.studentName,
                        subtitle = listOf(student.studentCode, student.gender).filter { it.isNotBlank() }.joinToString(" · "),
                        leading = {
                            SgrmsAvatar(
                                name = student.studentName,
                                size = 40.dp,
                                imageUrl = student.profileImageUrl,
                                legacyImageBase64 = student.profileImageBase64
                            )
                        },
                        trailing = {
                            SgrmsOverflowMenu(
                                contentDescription = stringResource(R.string.actions_for, student.studentName),
                                actions = listOf(
                                    RowAction(stringResource(R.string.remove_from_class), Icons.Default.PersonRemove, destructive = true) {
                                        selectedStudentToRemove = student
                                        showRemoveDialog = true
                                    }
                                )
                            )
                        }
                    )
                }
            }
        }
    }
}
