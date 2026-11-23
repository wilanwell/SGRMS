package com.wilanwell.sgrms.ui.admin

import com.wilanwell.sgrms.R
import androidx.compose.ui.res.stringResource
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
    val addATeacherAccountOrChangeText = stringResource(R.string.add_a_teacher_account_or_change)
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
            teacher.department.ifBlank { stringResource(R.string.not_specified) }
        }
        .distinct()
        .sorted()

    val filteredTeachers = teachers.filter { teacher ->
        val teacherDepartment = teacher.department.ifBlank { stringResource(R.string.not_specified) }

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
            title = stringResource(R.string.delete_teacher),
            message = stringResource(R.string.are_you_sure_you_want_to_delete_this),
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
        title = stringResource(R.string.teachers),
        subtitle = stringResource(R.string.of_teachers, filteredTeachers.size, teachers.size),
        searchText = searchText,
        onSearchChange = { searchText = it },
        searchPlaceholder = stringResource(R.string.search_name_staff_id_or_email),
        isLoading = uiState.isLoading,
        errorMessage = uiState.errorMessage,
        isEmpty = filteredTeachers.isEmpty(),
        empty = EmptyContent(stringResource(R.string.no_teachers_found), addATeacherAccountOrChangeText, Icons.Default.SupervisorAccount),
        chipGroups = listOf(ChipGroup(departmentList, selectedDepartment) { selectedDepartment = it }),
        fab = ListFab(stringResource(R.string.add_teacher), Icons.Default.PersonAdd, onAddTeacherClick),
        headerActions = {
            SgrmsOverflowMenu(
                contentDescription = stringResource(R.string.export),
                actions = listOf(
                    RowAction(stringResource(R.string.export_csv), Icons.Default.FileDownload) {
                        exportTeacherListToCsv(context = context, teachers = filteredTeachers)
                    }
                )
            )
        }
    ) {
        items(items = filteredTeachers, key = { it.teacherId }) { teacher ->
            Box(ListItemPadding) {
                SgrmsListItemCard(
                    title = teacher.teacherName.ifBlank { stringResource(R.string.unnamed_teacher) },
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
                        contentDescription = stringResource(R.string.actions_for, teacher.teacherName),
                        actions = listOf(
                            RowAction(stringResource(R.string.view), Icons.Default.Visibility) { onViewTeacherClick(teacher.teacherId) },
                            RowAction(stringResource(R.string.edit), Icons.Default.Edit) { onEditTeacherClick(teacher.teacherId) },
                            RowAction(stringResource(R.string.delete), Icons.Default.Delete, destructive = true) {
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

    val notSpecified = context.getString(R.string.not_specified)
    val csvContent = buildString {
        appendLine(context.getString(R.string.csv_teacher_header))

        teachers.forEach { teacher ->
            val row = listOf(
                teacher.teacherName.ifBlank { context.getString(R.string.unknown_teacher_title) },
                teacher.staffId.ifBlank { notSpecified },
                teacher.email.ifBlank { notSpecified },
                teacher.department.ifBlank { notSpecified },
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
        putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.teacher_list))
        putExtra(Intent.EXTRA_TITLE, "teacher_list.csv")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(
        Intent.createChooser(
            shareIntent,
            context.getString(R.string.export_teacher_list)
        )
    )
}