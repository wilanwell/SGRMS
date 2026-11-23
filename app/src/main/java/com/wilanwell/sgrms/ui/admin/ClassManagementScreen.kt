package com.wilanwell.sgrms.ui.admin

import com.wilanwell.sgrms.data.model.assignedTeacherName
import com.wilanwell.sgrms.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.School
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import com.wilanwell.sgrms.data.model.ClassModel
import com.wilanwell.sgrms.ui.components.SgrmsDeleteDialog
import com.wilanwell.sgrms.viewmodel.ClassFirebaseViewModel
import com.wilanwell.sgrms.viewmodel.StudentFirebaseViewModel
import com.wilanwell.sgrms.viewmodel.TeacherFirebaseViewModel
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.wilanwell.sgrms.ui.theme.SgrmsColors
import androidx.compose.foundation.layout.Box
import com.wilanwell.sgrms.ui.components.EmptyContent
import com.wilanwell.sgrms.ui.components.ListFab
import com.wilanwell.sgrms.ui.components.ListItemPadding
import com.wilanwell.sgrms.ui.components.RowAction
import com.wilanwell.sgrms.ui.components.SgrmsIconTile
import com.wilanwell.sgrms.ui.components.SgrmsListItemCard
import com.wilanwell.sgrms.ui.components.SgrmsListScreen
import com.wilanwell.sgrms.ui.components.SgrmsOverflowMenu
import com.wilanwell.sgrms.ui.components.SgrmsStatusChip
import com.wilanwell.sgrms.ui.components.ChipTone
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Group

@Composable
fun ClassManagementScreen(
    onBackClick: () -> Unit = {},
    onViewClassClick: (String) -> Unit = {},
    onAddStudentClick: () -> Unit = {}
) {
    val createAClassThenAddStudentsText = stringResource(R.string.create_a_class_then_add_students)
    val students3Text = stringResource(R.string.students_count)
    val t1StudentText = stringResource(R.string.t_1_student)
    val classViewModel: ClassFirebaseViewModel = hiltViewModel()

    val uiState by classViewModel.uiState.collectAsState()

    val teacherViewModel: TeacherFirebaseViewModel = hiltViewModel()
    val studentViewModel: StudentFirebaseViewModel = hiltViewModel()
    val teacherUiState by teacherViewModel.uiState.collectAsState()
    val studentUiState by studentViewModel.uiState.collectAsState()

    val teachers = remember(teacherUiState.teachers) {
        teacherUiState.teachers
            .filter { it.teacherName.isNotBlank() }
            .map { ClassTeacherItem(teacherId = it.teacherId, teacherName = it.teacherName) }
            .sortedBy { it.teacherName }
    }
    val students = studentUiState.students

    var searchText by remember { mutableStateOf("") }

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    var selectedClassToEdit by remember { mutableStateOf<ClassModel?>(null) }
    var selectedClassToDelete by remember { mutableStateOf<ClassModel?>(null) }
    var selectedClassToView by remember { mutableStateOf<ClassModel?>(null) }

    LaunchedEffect(Unit) {
        classViewModel.loadClasses()
        teacherViewModel.loadTeachers()
        studentViewModel.loadStudents()
    }

    val classes = uiState.classes

    val filteredClasses = classes.filter { schoolClass ->
        schoolClass.className.contains(searchText, ignoreCase = true) ||
                schoolClass.academicYear.contains(searchText, ignoreCase = true) ||
                schoolClass.description.contains(searchText, ignoreCase = true) ||
                schoolClass.classTeacherName.contains(searchText, ignoreCase = true)
    }

    if (showAddDialog) {
        AddClassDialog(
            teachers = teachers,
            onDismiss = {
                showAddDialog = false
            },
            onSave = { className, academicYear, description, classTeacherId, classTeacherName ->
                classViewModel.addClass(
                    className = className.trim(),
                    academicYear = academicYear.trim(),
                    classTeacherId = classTeacherId,
                    classTeacherName = classTeacherName,
                    description = description.trim(),
                    onSuccess = {
                        showAddDialog = false
                    }
                )
            }
        )
    }

    if (showEditDialog && selectedClassToEdit != null) {
        EditClassDialog(
            schoolClass = selectedClassToEdit!!,
            teachers = teachers,
            onDismiss = {
                showEditDialog = false
                selectedClassToEdit = null
            },
            onSave = { updatedClass ->
                classViewModel.updateClass(
                    schoolClass = updatedClass,
                    onSuccess = {
                        showEditDialog = false
                        selectedClassToEdit = null
                    }
                )
            }
        )
    }

    if (showDeleteDialog && selectedClassToDelete != null) {
        SgrmsDeleteDialog(
            title = stringResource(R.string.delete_class),
            message = stringResource(R.string.are_you_sure_you_want_to_delete),
            onConfirmClick = {
                selectedClassToDelete?.let { schoolClass ->
                    classViewModel.deleteClass(
                        classId = schoolClass.classId,
                        onSuccess = {
                            showDeleteDialog = false
                            selectedClassToDelete = null
                        }
                    )
                }
            },
            onDismissClick = {
                showDeleteDialog = false
                selectedClassToDelete = null
            }
        )
    }

    selectedClassToView?.let { schoolClass ->
        val studentsInClass = students.filter { student ->
            student.classId == schoolClass.classId ||
                    student.className == schoolClass.className
        }

        ClassStudentsDialog(
            schoolClass = schoolClass,
            students = studentsInClass,
            onDismiss = {
                selectedClassToView = null
            },
            onAddStudentClick = {
                selectedClassToView = null
                onAddStudentClick()
            }
        )
    }

    val studentCountByClass = remember(students, classes) {
        classes.associate { schoolClass ->
            schoolClass.classId to students.count { it.classId == schoolClass.classId || it.className == schoolClass.className }
        }
    }

    SgrmsListScreen(
        title = stringResource(R.string.classes),
        subtitle = stringResource(R.string.classes_students, classes.size, students.size),
        searchText = searchText,
        onSearchChange = { searchText = it },
        searchPlaceholder = stringResource(R.string.search_class_year_or_class_teacher),
        isLoading = uiState.isLoading,
        errorMessage = uiState.errorMessage,
        isEmpty = filteredClasses.isEmpty(),
        empty = EmptyContent(stringResource(R.string.no_classes_yet), createAClassThenAddStudentsText, Icons.Default.School),
        fab = ListFab(stringResource(R.string.add_class), Icons.Default.Add) { showAddDialog = true }
    ) {
        items(items = filteredClasses, key = { it.classId }) { schoolClass ->
            val count = studentCountByClass[schoolClass.classId] ?: 0
            Box(ListItemPadding) {
                SgrmsListItemCard(
                    title = schoolClass.className.ifBlank { stringResource(R.string.unnamed_class) },
                    subtitle = listOf(
                        schoolClass.academicYear,
                        stringResource(R.string.teacher_value, schoolClass.assignedTeacherName.ifBlank { stringResource(R.string.not_assigned) })
                    ).filter { it.isNotBlank() }.joinToString(" · "),
                    leading = {
                        SgrmsIconTile(
                            icon = Icons.Default.School,
                            tint = com.wilanwell.sgrms.ui.theme.SgrmsColors.Pink,
                            container = com.wilanwell.sgrms.ui.theme.SgrmsColors.PinkContainer,
                            size = 44.dp
                        )
                    },
                    onClick = { onViewClassClick(schoolClass.classId) }
                ) {
                    SgrmsStatusChip(if (count == 1) t1StudentText else students3Text.format(count), ChipTone.Primary)
                    SgrmsOverflowMenu(
                        contentDescription = stringResource(R.string.actions_for, schoolClass.className),
                        actions = listOf(
                            RowAction(stringResource(R.string.view), Icons.Default.Visibility) { onViewClassClick(schoolClass.classId) },
                            RowAction(stringResource(R.string.students), Icons.Default.Group) { selectedClassToView = schoolClass },
                            RowAction(stringResource(R.string.edit), Icons.Default.Edit) {
                                selectedClassToEdit = schoolClass
                                showEditDialog = true
                            },
                            RowAction(stringResource(R.string.delete), Icons.Default.Delete, destructive = true) {
                                selectedClassToDelete = schoolClass
                                showDeleteDialog = true
                            }
                        )
                    )
                }
            }
        }
    }
}
