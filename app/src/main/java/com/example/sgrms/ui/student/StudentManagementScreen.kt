package com.example.sgrms.ui.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sgrms.data.model.StudentModel
import com.example.sgrms.ui.components.SgrmsSearchBar
import com.example.sgrms.ui.components.TopBar
import com.example.sgrms.ui.theme.ColorBackground
import com.example.sgrms.ui.theme.ColorPrimary
import com.example.sgrms.ui.theme.ColorOnSurfaceVariant
import com.example.sgrms.viewmodel.StudentFirebaseViewModel
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.sgrms.viewmodel.ClassFirebaseViewModel
import com.example.sgrms.ui.components.LoadMoreButton
import com.example.sgrms.ui.theme.SgrmsColors

@Composable
fun StudentManagementScreen(
    onBackClick: () -> Unit = {},
    onAddStudentClick: () -> Unit = {},
    canAddStudent: Boolean = true,
    onStudentDetailClick: (String) -> Unit = {},
    onEditStudentClick: (String) -> Unit = {}
) {
    val context = LocalContext.current

    val studentViewModel: StudentFirebaseViewModel = hiltViewModel()

    val uiState by studentViewModel.uiState.collectAsState()

    var searchText by remember { mutableStateOf("") }

    var selectedClass by remember { mutableStateOf("All Classes") }
    var selectedGender by remember { mutableStateOf("All Gender") }
    var selectedStatus by remember { mutableStateOf("All Status") }
    var selectedSort by remember { mutableStateOf("Name") }

    var showFilterDialog by remember { mutableStateOf(false) }

    var selectedStudentToDelete by remember { mutableStateOf<StudentModel?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val classViewModel: ClassFirebaseViewModel = hiltViewModel()
    val classUiState by classViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        classViewModel.loadClasses()
    }

    // The class filter runs on the server; changing it reloads page one.
    val classIdByName = classUiState.classes
        .filter { it.className.isNotBlank() }
        .associate { it.className to it.classId }

    val selectedClassId = classIdByName[selectedClass]

    LaunchedEffect(selectedClassId) {
        studentViewModel.loadFirstStudentPage(classId = selectedClassId)
    }

    val students = uiState.students

    val classList = listOf("All Classes") + classIdByName.keys.sorted()

    val genderList = listOf(
        "All Gender",
        "Male",
        "Female"
    )

    val statusList = listOf(
        "All Status",
        "Active",
        "Inactive"
    )

    val sortList = listOf(
        "Name",
        "Code",
        "Class",
        "Gender",
        "Status"
    )

    val filteredStudents = students.filter { student ->
        val studentClass = student.className.ifBlank { "Not assigned" }
        val studentGender = student.gender.ifBlank { "Not specified" }
        val studentStatus = student.status.ifBlank { "Active" }

        val matchesSearch =
            student.studentName.contains(searchText, ignoreCase = true) ||
                    student.studentCode.contains(searchText, ignoreCase = true) ||
                    student.email.contains(searchText, ignoreCase = true) ||
                    student.phoneNumber.contains(searchText, ignoreCase = true) ||
                    studentClass.contains(searchText, ignoreCase = true) ||
                    studentGender.contains(searchText, ignoreCase = true) ||
                    studentStatus.contains(searchText, ignoreCase = true)

        // Filtered on the server by classId (see loadFirstStudentPage).
        val matchesClass = true

        val matchesGender =
            selectedGender == "All Gender" || studentGender == selectedGender

        val matchesStatus =
            selectedStatus == "All Status" || studentStatus == selectedStatus

        matchesSearch && matchesClass && matchesGender && matchesStatus
    }.let { list ->
        when (selectedSort) {
            "Code" -> list.sortedBy { student ->
                student.studentCode.lowercase()
            }

            "Class" -> list.sortedBy { student ->
                student.className.lowercase()
            }

            "Gender" -> list.sortedBy { student ->
                student.gender.lowercase()
            }

            "Status" -> list.sortedBy { student ->
                student.status.lowercase()
            }

            else -> list.sortedBy { student ->
                student.studentName.lowercase()
            }
        }
    }

    if (showDeleteDialog && selectedStudentToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                selectedStudentToDelete = null
            },
            title = {
                Text(
                    text = "Delete Student",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete ${selectedStudentToDelete?.studentName}? This student profile will be removed from Firestore."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedStudentToDelete?.let { student ->
                            studentViewModel.deleteStudent(
                                studentId = student.studentId,
                            )
                        }

                        showDeleteDialog = false
                        selectedStudentToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SgrmsColors.Error
                    )
                ) {
                    Text(
                        text = "Delete",
                        color = Color.White
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        selectedStudentToDelete = null
                    }
                ) {
                    Text(
                        text = "Cancel",
                        color = ColorPrimary
                    )
                }
            }
        )
    }

    if (showFilterDialog) {
        StudentFilterDialog(
            selectedClass = selectedClass,
            selectedGender = selectedGender,
            selectedStatus = selectedStatus,
            selectedSort = selectedSort,
            classList = classList,
            genderList = genderList,
            statusList = statusList,
            sortList = sortList,
            onClassSelected = {
                selectedClass = it
            },
            onGenderSelected = {
                selectedGender = it
            },
            onStatusSelected = {
                selectedStatus = it
            },
            onSortSelected = {
                selectedSort = it
            },
            onDismiss = {
                showFilterDialog = false
            },
            onClearFilter = {
                selectedClass = "All Classes"
                selectedGender = "All Gender"
                selectedStatus = "All Status"
                selectedSort = "Name"
                showFilterDialog = false
            }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopBar(
                title = "Students",
                showBack = false
            )
        },
        floatingActionButton = {
            // Only an Admin can create login accounts (see firestore.rules).
            if (canAddStudent) FloatingActionButton(
                onClick = onAddStudentClick,
                containerColor = ColorPrimary,
                contentColor = Color.White,
                modifier = Modifier
            ) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = "Add Student"
                )
            }
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ColorBackground)
                .padding(paddingValues)
                .padding(start = 16.dp, end = 16.dp, top = 16.dp)
        ) {
            SgrmsSearchBar(
                value = searchText,
                onValueChange = {
                    searchText = it
                },
                label = "Search Student",
                placeholder = "Search by name, code, email, phone, class, gender, or status"
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Student List",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Students loaded: ${students.size}${if (uiState.hasMore) "+" else ""}",
                        fontSize = 13.sp,
                        color = ColorOnSurfaceVariant
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            showFilterDialog = true
                        },
                        modifier = Modifier.height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = ColorPrimary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter Students",
                            tint = ColorPrimary,
                            modifier = Modifier.size(18.dp)
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            text = "Filter",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            exportStudentListToCsv(
                                context = context,
                                students = filteredStudents
                            )
                        },
                        modifier = Modifier.height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = ColorPrimary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = "Export",
                            tint = ColorPrimary,
                            modifier = Modifier.size(18.dp)
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            text = "Export",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "${filteredStudents.size} shown",
                fontSize = 13.sp,
                color = ColorPrimary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = ColorPrimary
                        )
                    }
                }

                uiState.errorMessage.isNotBlank() -> {
                    Text(
                        text = uiState.errorMessage,
                        color = SgrmsColors.Error,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                filteredStudents.isEmpty() -> {
                    EmptyStudentCard()
                }

                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 8.dp)
                    ) {
                        items(
                            items = filteredStudents,
                            key = { student ->
                                student.studentId
                            }
                        ) { student ->
                            StudentCard(
                                student = student,
                                onViewClick = {
                                    onStudentDetailClick(student.studentId)
                                },
                                onEditClick = {
                                    onEditStudentClick(student.studentId)
                                },
                                onDeleteClick = {
                                    selectedStudentToDelete = student
                                    showDeleteDialog = true
                                }
                            )
                        }

                        if (uiState.hasMore) {
                            item(key = "load_more") {
                                LoadMoreButton(
                                    isLoading = uiState.isLoadingMore,
                                    onClick = { studentViewModel.loadMoreStudents() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
