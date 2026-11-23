package com.wilanwell.sgrms.ui.subject

import com.wilanwell.sgrms.ui.components.LocalSessionAccess
import com.wilanwell.sgrms.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import com.wilanwell.sgrms.data.model.SubjectModel
import com.wilanwell.sgrms.viewmodel.SubjectFirebaseViewModel
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.wilanwell.sgrms.ui.components.ChipGroup
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
import com.wilanwell.sgrms.ui.components.SgrmsDeleteDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility

@Composable
fun SubjectManagementScreen(
    onBackClick: () -> Unit = {},
    onAddSubjectClick: () -> Unit = {},
    onSubjectDetailClick: (String) -> Unit = {},
    onEditSubjectClick: (String) -> Unit = {}
) {
    val access = LocalSessionAccess.current
    val addASubjectOrChangeTheText = stringResource(R.string.add_a_subject_or_change_the)
    val allCategoriesText = stringResource(R.string.all_categories)
    val sortByCurrentText = stringResource(R.string.sort_by_current)
    val sortByText = stringResource(R.string.sort_by)
    val willBeRemovedFromTheSubjectText = stringResource(R.string.will_be_removed_from_the_subject)
    val subjectViewModel: SubjectFirebaseViewModel = hiltViewModel()

    val uiState by subjectViewModel.uiState.collectAsState()

    var searchText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All Categories") }
    var selectedSort by remember { mutableStateOf("Name") }


    var selectedSubjectToDelete by remember { mutableStateOf<SubjectModel?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        subjectViewModel.loadSubjects()
    }

    val subjects = uiState.subjects

    val categoryList = listOf(
        allCategoriesText,
        "Core",
        "Humanities",
        "Applied",
        "Elective"
    )

    val sortList = listOf(
        "Name",
        "Code",
        "Category"
    )

    val filteredSubjects = subjects.filter { subject ->
        val matchesSearch =
            subject.subjectName.contains(searchText, ignoreCase = true) ||
                    subject.subjectCode.contains(searchText, ignoreCase = true) ||
                    subject.teacherName.contains(searchText, ignoreCase = true) ||
                    subject.category.contains(searchText, ignoreCase = true)

        val matchesCategory =
            selectedCategory == "All Categories" || subject.category == selectedCategory

        matchesSearch && matchesCategory
    }.let { list ->
        when (selectedSort) {
            "Code" -> list.sortedBy { subject ->
                subject.subjectCode.lowercase()
            }

            "Category" -> list.sortedBy { subject ->
                subject.category.lowercase()
            }

            else -> list.sortedBy { subject ->
                subject.subjectName.lowercase()
            }
        }
    }

    if (showDeleteDialog && selectedSubjectToDelete != null) {
        SgrmsDeleteDialog(
            title = stringResource(R.string.delete_subject),
            message = willBeRemovedFromTheSubjectText.format(selectedSubjectToDelete?.subjectName),
            onConfirmClick = {
                selectedSubjectToDelete?.let { subject ->
                    subjectViewModel.deleteSubject(
                        subjectId = subject.subjectId,
                        onSuccess = { subjectViewModel.loadSubjects() }
                    )
                }
                showDeleteDialog = false
                selectedSubjectToDelete = null
            },
            onDismissClick = {
                showDeleteDialog = false
                selectedSubjectToDelete = null
            }
        )
    }

    SgrmsListScreen(
        title = stringResource(R.string.subjects),
        subtitle = stringResource(R.string.of_subjects, filteredSubjects.size, subjects.size),
        searchText = searchText,
        onSearchChange = { searchText = it },
        searchPlaceholder = stringResource(R.string.search_subject_name_code_or_teacher),
        isLoading = uiState.isLoading,
        errorMessage = uiState.errorMessage,
        isEmpty = filteredSubjects.isEmpty(),
        empty = EmptyContent(stringResource(R.string.no_subjects_found), addASubjectOrChangeTheText, Icons.AutoMirrored.Filled.MenuBook),
        chipGroups = listOf(ChipGroup(categoryList, selectedCategory) { selectedCategory = it }),
        fab = ListFab(stringResource(R.string.add_subject), Icons.Default.Add, onAddSubjectClick),
        headerActions = {
            SgrmsOverflowMenu(
                contentDescription = stringResource(R.string.sort),
                actions = sortList.map { option ->
                    RowAction(if (option == selectedSort) sortByCurrentText.format(option) else sortByText.format(option), Icons.AutoMirrored.Filled.Sort) {
                        selectedSort = option
                    }
                }
            )
        }
    ) {
        items(items = filteredSubjects, key = { it.subjectId }) { subject ->
            Box(ListItemPadding) {
                SgrmsListItemCard(
                    title = subject.subjectName.ifBlank { stringResource(R.string.unnamed_subject) },
                    subtitle = listOf(subject.subjectCode, subject.teacherName.ifBlank { stringResource(R.string.no_teacher) })
                        .filter { it.isNotBlank() }
                        .joinToString(" · "),
                    leading = {
                        SgrmsIconTile(
                            icon = Icons.AutoMirrored.Filled.MenuBook,
                            tint = MaterialTheme.colorScheme.primary,
                            container = MaterialTheme.colorScheme.primaryContainer,
                            size = 44.dp
                        )
                    },
                    onClick = { onSubjectDetailClick(subject.subjectId) }
                ) {
                    if (subject.category.isNotBlank()) SgrmsStatusChip(subject.category, ChipTone.Neutral)
                    SgrmsOverflowMenu(
                        contentDescription = stringResource(R.string.actions_for, subject.subjectName),
                        actions = listOfNotNull(
                            RowAction(stringResource(R.string.view), Icons.Default.Visibility) { onSubjectDetailClick(subject.subjectId) },
                            if (access.canWriteSubject(subject.teacherId)) {
                                RowAction(stringResource(R.string.edit), Icons.Default.Edit) { onEditSubjectClick(subject.subjectId) }
                            } else null,
                            if (access.canWriteSubject(subject.teacherId)) {
                                RowAction(stringResource(R.string.delete), Icons.Default.Delete, destructive = true) {
                                    selectedSubjectToDelete = subject
                                    showDeleteDialog = true
                                }
                            } else null
                        )
                    )
                }
            }
        }
    }
}
