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
import com.example.sgrms.ui.components.toneForStatus
import com.example.sgrms.ui.components.SgrmsStatusChip
import com.example.sgrms.ui.components.SgrmsOverflowMenu
import com.example.sgrms.ui.components.SgrmsListScreen
import com.example.sgrms.ui.components.SgrmsListItemCard
import com.example.sgrms.ui.components.SgrmsDeleteDialog
import com.example.sgrms.ui.components.SgrmsAvatar
import com.example.sgrms.ui.components.RowAction
import com.example.sgrms.ui.components.ListItemPadding
import com.example.sgrms.ui.components.ListFab
import com.example.sgrms.ui.components.EmptyContent
import com.example.sgrms.ui.components.ChipGroup
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.automirrored.filled.Sort

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
        SgrmsDeleteDialog(
            title = "Delete student?",
            message = "${selectedStudentToDelete?.studentName} and their grades, notifications and report will be removed. This cannot be undone.",
            onConfirmClick = {
                selectedStudentToDelete?.let { studentViewModel.deleteStudent(studentId = it.studentId) }
                showDeleteDialog = false
                selectedStudentToDelete = null
            },
            onDismissClick = {
                showDeleteDialog = false
                selectedStudentToDelete = null
            }
        )
    }

    SgrmsListScreen(
        title = "Students",
        subtitle = "${filteredStudents.size} shown · ${students.size}${if (uiState.hasMore) "+" else ""} loaded",
        searchText = searchText,
        onSearchChange = { searchText = it },
        searchPlaceholder = "Search name, code, email or phone",
        isLoading = uiState.isLoading,
        errorMessage = uiState.errorMessage,
        isEmpty = filteredStudents.isEmpty(),
        empty = EmptyContent(
            title = "No students found",
            message = "Try another search or filter.",
            icon = Icons.Default.Group
        ),
        chipGroups = listOf(
            ChipGroup(classList, selectedClass) { selectedClass = it },
            ChipGroup(statusList, selectedStatus) { selectedStatus = it },
            ChipGroup(genderList, selectedGender) { selectedGender = it }
        ),
        fab = if (canAddStudent) ListFab("Add student", Icons.Default.PersonAdd, onAddStudentClick) else null,
        headerActions = {
            SgrmsOverflowMenu(
                contentDescription = "Sort and export",
                actions = sortList.map { option ->
                    RowAction(
                        label = if (option == selectedSort) "Sort by $option (current)" else "Sort by $option",
                        icon = Icons.AutoMirrored.Filled.Sort
                    ) { selectedSort = option }
                } + RowAction("Export CSV", Icons.Default.FileDownload) {
                    exportStudentListToCsv(context = context, students = filteredStudents)
                }
            )
        }
    ) {
        items(items = filteredStudents, key = { it.studentId }) { student ->
            Box(ListItemPadding) {
                SgrmsListItemCard(
                    title = student.studentName.ifBlank { "Unnamed student" },
                    subtitle = listOf(
                        student.studentCode,
                        student.className.ifBlank { "No class" },
                        student.gender
                    ).filter { it.isNotBlank() }.joinToString(" · "),
                    leading = {
                        SgrmsAvatar(
                            name = student.studentName,
                            size = 44.dp,
                            imageUrl = student.profileImageUrl,
                            legacyImageBase64 = student.profileImageBase64
                        )
                    },
                    onClick = { onStudentDetailClick(student.studentId) }
                ) {
                    val status = student.status.ifBlank { "Active" }
                    SgrmsStatusChip(text = status, tone = toneForStatus(status))
                    SgrmsOverflowMenu(
                        contentDescription = "Actions for ${student.studentName}",
                        actions = listOf(
                            RowAction("View", Icons.Default.Visibility) { onStudentDetailClick(student.studentId) },
                            RowAction("Edit", Icons.Default.Edit) { onEditStudentClick(student.studentId) },
                            RowAction("Delete", Icons.Default.Delete, destructive = true) {
                                selectedStudentToDelete = student
                                showDeleteDialog = true
                            }
                        )
                    )
                }
            }
        }

        if (uiState.hasMore) {
            item(key = "load_more") {
                LoadMoreButton(isLoading = uiState.isLoadingMore, onClick = { studentViewModel.loadMoreStudents() })
            }
        }
    }

}
