package com.example.sgrms.ui.subject

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
import com.example.sgrms.data.model.SubjectModel
import com.example.sgrms.viewmodel.SubjectFirebaseViewModel
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.sgrms.ui.components.ChipGroup
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
import com.example.sgrms.ui.components.SgrmsDeleteDialog
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
        "All Categories",
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
            title = "Delete subject?",
            message = "${selectedSubjectToDelete?.subjectName} will be removed from the subject list.",
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
        title = "Subjects",
        subtitle = "${filteredSubjects.size} of ${subjects.size} subjects",
        searchText = searchText,
        onSearchChange = { searchText = it },
        searchPlaceholder = "Search subject name, code or teacher",
        isLoading = uiState.isLoading,
        errorMessage = uiState.errorMessage,
        isEmpty = filteredSubjects.isEmpty(),
        empty = EmptyContent("No subjects found", "Add a subject or change the filter.", Icons.AutoMirrored.Filled.MenuBook),
        chipGroups = listOf(ChipGroup(categoryList, selectedCategory) { selectedCategory = it }),
        fab = ListFab("Add subject", Icons.Default.Add, onAddSubjectClick),
        headerActions = {
            SgrmsOverflowMenu(
                contentDescription = "Sort",
                actions = sortList.map { option ->
                    RowAction(if (option == selectedSort) "Sort by $option (current)" else "Sort by $option", Icons.AutoMirrored.Filled.Sort) {
                        selectedSort = option
                    }
                }
            )
        }
    ) {
        items(items = filteredSubjects, key = { it.subjectId }) { subject ->
            Box(ListItemPadding) {
                SgrmsListItemCard(
                    title = subject.subjectName.ifBlank { "Unnamed subject" },
                    subtitle = listOf(subject.subjectCode, subject.teacherName.ifBlank { "No teacher" })
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
                        contentDescription = "Actions for ${subject.subjectName}",
                        actions = listOf(
                            RowAction("View", Icons.Default.Visibility) { onSubjectDetailClick(subject.subjectId) },
                            RowAction("Edit", Icons.Default.Edit) { onEditSubjectClick(subject.subjectId) },
                            RowAction("Delete", Icons.Default.Delete, destructive = true) {
                                selectedSubjectToDelete = subject
                                showDeleteDialog = true
                            }
                        )
                    )
                }
            }
        }
    }
}
