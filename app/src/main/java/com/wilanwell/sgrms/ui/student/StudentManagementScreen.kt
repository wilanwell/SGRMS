package com.wilanwell.sgrms.ui.student

import com.wilanwell.sgrms.R
import androidx.compose.ui.res.stringResource
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
import com.wilanwell.sgrms.data.model.StudentModel
import com.wilanwell.sgrms.ui.components.SgrmsSearchBar
import com.wilanwell.sgrms.ui.components.TopBar
import com.wilanwell.sgrms.ui.theme.ColorBackground
import com.wilanwell.sgrms.ui.theme.ColorPrimary
import com.wilanwell.sgrms.ui.theme.ColorOnSurfaceVariant
import com.wilanwell.sgrms.viewmodel.StudentFirebaseViewModel
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.wilanwell.sgrms.viewmodel.ClassFirebaseViewModel
import com.wilanwell.sgrms.ui.components.LoadMoreButton
import com.wilanwell.sgrms.ui.theme.SgrmsColors
import com.wilanwell.sgrms.ui.components.toneForStatus
import com.wilanwell.sgrms.ui.components.SgrmsStatusChip
import com.wilanwell.sgrms.ui.components.SgrmsOverflowMenu
import com.wilanwell.sgrms.ui.components.SgrmsListScreen
import com.wilanwell.sgrms.ui.components.SgrmsListItemCard
import com.wilanwell.sgrms.ui.components.SgrmsDeleteDialog
import com.wilanwell.sgrms.ui.components.SgrmsAvatar
import com.wilanwell.sgrms.ui.components.RowAction
import com.wilanwell.sgrms.ui.components.ListItemPadding
import com.wilanwell.sgrms.ui.components.ListFab
import com.wilanwell.sgrms.ui.components.EmptyContent
import com.wilanwell.sgrms.ui.components.ChipGroup
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
    val allGenderText = stringResource(R.string.all_gender)
    val allStatusText = stringResource(R.string.all_status)
    val andTheirGradesNotificationsAndReportText = stringResource(R.string.and_their_grades_notifications_and_report)
    val sortByCurrentText = stringResource(R.string.sort_by_current)
    val sortByText = stringResource(R.string.sort_by)
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
        allGenderText,
        "Male",
        "Female"
    )

    val statusList = listOf(
        allStatusText,
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
        val studentClass = student.className.ifBlank { stringResource(R.string.not_assigned) }
        val studentGender = student.gender.ifBlank { stringResource(R.string.not_specified) }
        val studentStatus = student.status.ifBlank { stringResource(R.string.active) }

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
            title = stringResource(R.string.delete_student),
            message = andTheirGradesNotificationsAndReportText.format(selectedStudentToDelete?.studentName),
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
        title = stringResource(R.string.students),
        subtitle = "${filteredStudents.size} shown · ${students.size}${if (uiState.hasMore) "+" else ""} loaded",
        searchText = searchText,
        onSearchChange = { searchText = it },
        searchPlaceholder = stringResource(R.string.search_name_code_email_or_phone),
        isLoading = uiState.isLoading,
        errorMessage = uiState.errorMessage,
        isEmpty = filteredStudents.isEmpty(),
        empty = EmptyContent(
            title = stringResource(R.string.no_students_found),
            message = stringResource(R.string.try_another_search_or_filter),
            icon = Icons.Default.Group
        ),
        chipGroups = listOf(
            ChipGroup(classList, selectedClass) { selectedClass = it },
            ChipGroup(statusList, selectedStatus) { selectedStatus = it },
            ChipGroup(genderList, selectedGender) { selectedGender = it }
        ),
        fab = if (canAddStudent) ListFab(stringResource(R.string.add_student), Icons.Default.PersonAdd, onAddStudentClick) else null,
        headerActions = {
            SgrmsOverflowMenu(
                contentDescription = stringResource(R.string.sort_and_export),
                actions = sortList.map { option ->
                    RowAction(
                        label = if (option == selectedSort) sortByCurrentText.format(option) else sortByText.format(option),
                        icon = Icons.AutoMirrored.Filled.Sort
                    ) { selectedSort = option }
                } + RowAction(stringResource(R.string.export_csv), Icons.Default.FileDownload) {
                    exportStudentListToCsv(context = context, students = filteredStudents)
                }
            )
        }
    ) {
        items(items = filteredStudents, key = { it.studentId }) { student ->
            Box(ListItemPadding) {
                SgrmsListItemCard(
                    title = student.studentName.ifBlank { stringResource(R.string.unnamed_student) },
                    subtitle = listOf(
                        student.studentCode,
                        student.className.ifBlank { stringResource(R.string.no_class) },
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
                    val status = student.status.ifBlank { stringResource(R.string.active) }
                    SgrmsStatusChip(text = status, tone = toneForStatus(status))
                    SgrmsOverflowMenu(
                        contentDescription = stringResource(R.string.actions_for, student.studentName),
                        actions = listOf(
                            RowAction(stringResource(R.string.view), Icons.Default.Visibility) { onStudentDetailClick(student.studentId) },
                            RowAction(stringResource(R.string.edit), Icons.Default.Edit) { onEditStudentClick(student.studentId) },
                            RowAction(stringResource(R.string.delete), Icons.Default.Delete, destructive = true) {
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
