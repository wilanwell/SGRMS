package com.example.sgrms.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sgrms.data.model.ClassModel
import com.example.sgrms.ui.components.SgrmsDashboardSummaryCard
import com.example.sgrms.ui.components.SgrmsDeleteDialog
import com.example.sgrms.ui.components.SgrmsEmptyStateCard
import com.example.sgrms.ui.components.SgrmsSearchBar
import com.example.sgrms.ui.components.SgrmsSummaryCardStyle
import com.example.sgrms.ui.components.TopBar
import com.example.sgrms.ui.theme.BackgroundGrey
import com.example.sgrms.ui.theme.PrimaryBlue
import com.example.sgrms.viewmodel.ClassFirebaseViewModel
import com.example.sgrms.viewmodel.StudentFirebaseViewModel
import com.example.sgrms.viewmodel.TeacherFirebaseViewModel
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

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

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopBar(
                title = "Class Management",
                showBack = false,
                showIcon = true
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    showAddDialog = true
                },
                containerColor = PrimaryBlue,
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Class"
                )
            }
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundGrey)
                .padding(paddingValues)
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp
                ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 8.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SgrmsDashboardSummaryCard(
                        title = "Classes",
                        value = classes.size.toString(),
                        icon = Icons.Default.School,
                        modifier = Modifier.weight(1f),
                        style = SgrmsSummaryCardStyle.Primary,
                        height = 108.dp
                    )

                    SgrmsDashboardSummaryCard(
                        title = "Teachers",
                        value = teachers.size.toString(),
                        icon = Icons.Default.Person,
                        modifier = Modifier.weight(1f),
                        style = SgrmsSummaryCardStyle.Primary,
                        height = 108.dp
                    )
                }
            }

            item {
                SgrmsSearchBar(
                    value = searchText,
                    onValueChange = {
                        searchText = it
                    },
                    label = "Search Class",
                    placeholder = "Search by class name or academic year"
                )
            }

            if (uiState.errorMessage.isNotBlank()) {
                item {
                    Text(
                        text = uiState.errorMessage,
                        color = Color.Red,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            item {
                Text(
                    text = "Class List (${filteredClasses.size})",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (filteredClasses.isEmpty()) {
                item {
                    SgrmsEmptyStateCard(
                        icon = Icons.Default.School,
                        title = "No class found",
                        message = "Tap Add Class to create a class record"
                    )
                }
            } else {
                items(
                    items = filteredClasses,
                    key = { schoolClass ->
                        schoolClass.classId
                    }
                ) { schoolClass ->
                    ClassCard(
                        schoolClass = schoolClass,
                        classTeacherName = schoolClass.classTeacherName.ifBlank {
                            "Not assigned"
                        },
                        onViewClick = {
                            onViewClassClick(schoolClass.classId)
                        },
                        onEditClick = {
                            selectedClassToEdit = schoolClass
                            showEditDialog = true
                        },
                        onDeleteClick = {
                            selectedClassToDelete = schoolClass
                            showDeleteDialog = true
                        }
                    )
                }
            }
        }
    }
}
