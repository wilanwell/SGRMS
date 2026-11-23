package com.wilanwell.sgrms.ui.admin

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.wilanwell.sgrms.data.model.TeacherModel
import com.wilanwell.sgrms.ui.components.SgrmsDeleteDialog
import com.wilanwell.sgrms.viewmodel.TeacherFirebaseViewModel
import java.io.File
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.wilanwell.sgrms.ui.components.ChipGroup
import com.wilanwell.sgrms.ui.components.EmptyContent
import com.wilanwell.sgrms.ui.components.ListFab
import com.wilanwell.sgrms.ui.components.ListItemPadding
import com.wilanwell.sgrms.ui.components.RowAction
import com.wilanwell.sgrms.ui.components.SgrmsAvatar
import com.wilanwell.sgrms.ui.components.SgrmsListItemCard
import com.wilanwell.sgrms.ui.components.SgrmsListScreen
import com.wilanwell.sgrms.ui.components.SgrmsOverflowMenu
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Add

@Composable
fun TeacherManagementScreen(
    onBackClick: () -> Unit = {},
    onAddTeacherClick: () -> Unit = {},
    onViewTeacherClick: (String) -> Unit = {},
    onEditTeacherClick: (String) -> Unit = {}
) {
    val context = LocalContext.current

    val teacherViewModel: TeacherFirebaseViewModel = hiltViewModel()

    val uiState by teacherViewModel.uiState.collectAsState()

    var searchText by remember { mutableStateOf("") }

    var selectedDepartment by remember { mutableStateOf("All Departments") }

    var selectedTeacherToDelete by remember { mutableStateOf<TeacherModel?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        teacherViewModel.loadTeachers()
    }

    val teachers = uiState.teachers

    val departmentList = listOf("All Departments") + teachers
        .map { teacher ->
            teacher.department.ifBlank { "Not specified" }
        }
        .distinct()
        .sorted()

    val filteredTeachers = teachers.filter { teacher ->
        val teacherDepartment = teacher.department.ifBlank { "Not specified" }

        val matchesSearch =
            teacher.teacherName.contains(searchText, ignoreCase = true) ||
                    teacher.staffId.contains(searchText, ignoreCase = true) ||
                    teacher.email.contains(searchText, ignoreCase = true) ||
                    teacher.department.contains(searchText, ignoreCase = true) ||
                    teacher.position.contains(searchText, ignoreCase = true)

        val matchesDepartment =
            selectedDepartment == "All Departments" || teacherDepartment == selectedDepartment

        matchesSearch && matchesDepartment
    }

    if (showDeleteDialog && selectedTeacherToDelete != null) {
        SgrmsDeleteDialog(
            title = "Delete Teacher",
            message = "Are you sure you want to delete this teacher account? This will remove the teacher profile from Firestore.",
            onConfirmClick = {
                selectedTeacherToDelete?.let { teacher ->
                    teacherViewModel.deleteTeacher(
                        teacherId = teacher.teacherId
                    )
                }

                showDeleteDialog = false
                selectedTeacherToDelete = null
            },
            onDismissClick = {
                showDeleteDialog = false
                selectedTeacherToDelete = null
            }
        )
    }

    SgrmsListScreen(
        title = "Teachers",
        subtitle = "${filteredTeachers.size} of ${teachers.size} teachers",
        searchText = searchText,
        onSearchChange = { searchText = it },
        searchPlaceholder = "Search name, staff ID or email",
        isLoading = uiState.isLoading,
        errorMessage = uiState.errorMessage,
        isEmpty = filteredTeachers.isEmpty(),
        empty = EmptyContent("No teachers found", "Add a teacher account or change the filter.", Icons.Default.SupervisorAccount),
        chipGroups = listOf(ChipGroup(departmentList, selectedDepartment) { selectedDepartment = it }),
        fab = ListFab("Add teacher", Icons.Default.PersonAdd, onAddTeacherClick),
        headerActions = {
            SgrmsOverflowMenu(
                contentDescription = "Export",
                actions = listOf(
                    RowAction("Export CSV", Icons.Default.FileDownload) {
                        exportTeacherListToCsv(context = context, teachers = filteredTeachers)
                    }
                )
            )
        }
    ) {
        items(items = filteredTeachers, key = { it.teacherId }) { teacher ->
            Box(ListItemPadding) {
                SgrmsListItemCard(
                    title = teacher.teacherName.ifBlank { "Unnamed teacher" },
                    subtitle = listOf(teacher.staffId, teacher.department, teacher.position)
                        .filter { it.isNotBlank() }
                        .joinToString(" · "),
                    leading = {
                        SgrmsAvatar(
                            name = teacher.teacherName,
                            size = 44.dp,
                            imageUrl = teacher.profileImageUrl,
                            legacyImageBase64 = teacher.profileImageBase64
                        )
                    },
                    onClick = { onViewTeacherClick(teacher.teacherId) }
                ) {
                    SgrmsOverflowMenu(
                        contentDescription = "Actions for ${teacher.teacherName}",
                        actions = listOf(
                            RowAction("View", Icons.Default.Visibility) { onViewTeacherClick(teacher.teacherId) },
                            RowAction("Edit", Icons.Default.Edit) { onEditTeacherClick(teacher.teacherId) },
                            RowAction("Delete", Icons.Default.Delete, destructive = true) {
                                selectedTeacherToDelete = teacher
                                showDeleteDialog = true
                            }
                        )
                    )
                }
            }
        }
    }
}

fun exportTeacherListToCsv(
    context: Context,
    teachers: List<TeacherModel>
) {
    val fileName = "teacher_list.csv"
    val file = File(context.cacheDir, fileName)

    val csvContent = buildString {
        appendLine("Teacher Name,Staff ID,Email,Department,Position")

        teachers.forEach { teacher ->
            val row = listOf(
                teacher.teacherName.ifBlank { "Unknown Teacher" },
                teacher.staffId.ifBlank { "Not specified" },
                teacher.email.ifBlank { "Not specified" },
                teacher.department.ifBlank { "Not specified" },
                teacher.position.ifBlank { "Teacher" }
            )

            appendLine(
                row.joinToString(",") { value ->
                    "\"${value.replace("\"", "\"\"")}\""
                }
            )
        }
    }

    file.writeText(csvContent)

    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )

    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/csv"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, "Teacher List")
        putExtra(Intent.EXTRA_TITLE, "teacher_list.csv")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(
        Intent.createChooser(
            shareIntent,
            "Export Teacher List"
        )
    )
}