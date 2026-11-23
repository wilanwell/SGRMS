package com.example.sgrms.ui.admin

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
import com.example.sgrms.data.model.ClassModel
import com.example.sgrms.ui.components.SgrmsDeleteDialog
import com.example.sgrms.viewmodel.ClassFirebaseViewModel
import com.example.sgrms.viewmodel.StudentFirebaseViewModel
import com.example.sgrms.viewmodel.TeacherFirebaseViewModel
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.sgrms.ui.theme.SgrmsColors
import androidx.compose.foundation.layout.Box
import com.example.sgrms.ui.components.EmptyContent
import com.example.sgrms.ui.components.ListFab
import com.example.sgrms.ui.components.ListItemPadding
import com.example.sgrms.ui.components.RowAction
import com.example.sgrms.ui.components.SgrmsIconTile
import com.example.sgrms.ui.components.SgrmsListItemCard
import com.example.sgrms.ui.components.SgrmsListScreen
import com.example.sgrms.ui.components.SgrmsOverflowMenu
import com.example.sgrms.ui.components.SgrmsStatusChip
import com.example.sgrms.ui.components.ChipTone
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
            title = "Delete Class",
            message = "Are you sure you want to delete this class? This action cannot be undone.",
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
        title = "Classes",
        subtitle = "${classes.size} classes · ${students.size} students",
        searchText = searchText,
        onSearchChange = { searchText = it },
        searchPlaceholder = "Search class, year or class teacher",
        isLoading = uiState.isLoading,
        errorMessage = uiState.errorMessage,
        isEmpty = filteredClasses.isEmpty(),
        empty = EmptyContent("No classes yet", "Create a class, then add students to it.", Icons.Default.School),
        fab = ListFab("Add class", Icons.Default.Add) { showAddDialog = true }
    ) {
        items(items = filteredClasses, key = { it.classId }) { schoolClass ->
            val count = studentCountByClass[schoolClass.classId] ?: 0
            Box(ListItemPadding) {
                SgrmsListItemCard(
                    title = schoolClass.className.ifBlank { "Unnamed class" },
                    subtitle = listOf(
                        schoolClass.academicYear,
                        "Teacher: " + schoolClass.classTeacherName.ifBlank { "Not assigned" }
                    ).filter { it.isNotBlank() }.joinToString(" · "),
                    leading = {
                        SgrmsIconTile(
                            icon = Icons.Default.School,
                            tint = com.example.sgrms.ui.theme.SgrmsColors.Pink,
                            container = com.example.sgrms.ui.theme.SgrmsColors.PinkContainer,
                            size = 44.dp
                        )
                    },
                    onClick = { onViewClassClick(schoolClass.classId) }
                ) {
                    SgrmsStatusChip(if (count == 1) "1 student" else "$count students", ChipTone.Primary)
                    SgrmsOverflowMenu(
                        contentDescription = "Actions for ${schoolClass.className}",
                        actions = listOf(
                            RowAction("View", Icons.Default.Visibility) { onViewClassClick(schoolClass.classId) },
                            RowAction("Students", Icons.Default.Group) { selectedClassToView = schoolClass },
                            RowAction("Edit", Icons.Default.Edit) {
                                selectedClassToEdit = schoolClass
                                showEditDialog = true
                            },
                            RowAction("Delete", Icons.Default.Delete, destructive = true) {
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
