package com.wilanwell.sgrms.ui.grade

import com.wilanwell.sgrms.ui.components.LocalSessionAccess
import com.wilanwell.sgrms.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.wilanwell.sgrms.viewmodel.GradeFirebaseViewModel
import com.wilanwell.sgrms.viewmodel.StudentFirebaseViewModel
import com.wilanwell.sgrms.viewmodel.SubjectFirebaseViewModel
import com.wilanwell.sgrms.data.model.GradeModel
import com.wilanwell.sgrms.ui.components.LoadMoreButton
import com.wilanwell.sgrms.ui.components.ChipGroup
import com.wilanwell.sgrms.ui.components.EmptyContent
import com.wilanwell.sgrms.ui.components.ListFab
import com.wilanwell.sgrms.ui.components.ListItemPadding
import com.wilanwell.sgrms.ui.components.RowAction
import com.wilanwell.sgrms.ui.components.SgrmsAvatar
import com.wilanwell.sgrms.ui.components.SgrmsListItemCard
import com.wilanwell.sgrms.ui.components.SgrmsListScreen
import com.wilanwell.sgrms.ui.components.SgrmsOverflowMenu
import com.wilanwell.sgrms.ui.components.SgrmsStatusChip
import com.wilanwell.sgrms.ui.components.ChipTone
import com.wilanwell.sgrms.ui.components.toneForGrade
import com.wilanwell.sgrms.ui.components.SgrmsDeleteDialog
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.EditNote

@Composable
fun GradeManagementScreen(
    onBackClick: () -> Unit = {},
    onAddGradeClick: () -> Unit = {},
    onStudentDetailClick: (String) -> Unit = {},
    onEditGradeClick: (String) -> Unit = {}
) {
    val addGradesForAClassOrText = stringResource(R.string.add_grades_for_a_class_or)
    val allStatusText = stringResource(R.string.all_status)
    val gradeViewModel: GradeFirebaseViewModel = hiltViewModel()
    val studentViewModel: StudentFirebaseViewModel = hiltViewModel()
    val subjectViewModel: SubjectFirebaseViewModel = hiltViewModel()

    val gradeUiState by gradeViewModel.uiState.collectAsState()
    val studentUiState by studentViewModel.uiState.collectAsState()
    val subjectUiState by subjectViewModel.uiState.collectAsState()

    val grades = gradeUiState.grades
    val students = studentUiState.students
    val subjects = subjectUiState.subjects
    val access = LocalSessionAccess.current
    val subjectTeacher = subjects.associate { it.subjectId to it.teacherId }

    val isLoading = gradeUiState.isLoading || studentUiState.isLoading || subjectUiState.isLoading
    val errorMessage = listOf(
        gradeUiState.errorMessage,
        studentUiState.errorMessage,
        subjectUiState.errorMessage
    ).firstOrNull { it.isNotBlank() }.orEmpty()

    var searchText by remember { mutableStateOf("") }
    var selectedClass by remember { mutableStateOf("All Classes") }
    var selectedSubject by remember { mutableStateOf("All Subjects") }
    var selectedStatus by remember { mutableStateOf("All Status") }


    var selectedGradeToDelete by remember { mutableStateOf<GradeModel?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    fun loadGradeData() {
        studentViewModel.loadStudents()
        subjectViewModel.loadSubjects()
    }

    LaunchedEffect(Unit) {
        loadGradeData()
    }

    // The subject filter runs on the server; changing it reloads page one.
    val subjectIdByName = subjects
        .filter { it.subjectName.isNotBlank() }
        .associate { it.subjectName to it.subjectId }

    val selectedSubjectId = subjectIdByName[selectedSubject]

    LaunchedEffect(selectedSubjectId) {
        gradeViewModel.loadFirstGradePage(subjectId = selectedSubjectId)
    }

    val studentsById = remember(students) { students.associateBy { it.studentId } }
    val subjectsById = remember(subjects) { subjects.associateBy { it.subjectId } }

    val gradeDisplayList = grades.map { grade ->
        val student = studentsById[grade.studentId]
        val subject = subjectsById[grade.subjectId]

        GradeDisplayItem(
            grade = grade,
            studentName = student?.studentName ?: stringResource(R.string.unknown_student_title),
            className = student?.className ?: stringResource(R.string.unknown_class),
            subjectName = subject?.subjectName ?: stringResource(R.string.unknown_subject),
            subjectCode = subject?.subjectCode ?: "-"
        )
    }

    val classList = listOf("All Classes") + students
        .map { student ->
            student.className.ifBlank { stringResource(R.string.unknown_class) }
        }
        .distinct()
        .sorted()

    val subjectList = listOf("All Subjects") + subjects
        .map { subject ->
            subject.subjectName.ifBlank { stringResource(R.string.unknown_subject) }
        }
        .distinct()
        .sorted()

    val statusList = listOf(
        allStatusText,
        "Complete",
        "Pending"
    )

    val filteredRecords = gradeDisplayList.filter { item ->
        val matchesSearch =
            item.studentName.contains(searchText, ignoreCase = true) ||
                    item.subjectName.contains(searchText, ignoreCase = true) ||
                    item.subjectCode.contains(searchText, ignoreCase = true) ||
                    item.className.contains(searchText, ignoreCase = true)

        val matchesClass =
            selectedClass == "All Classes" || item.className == selectedClass

        // Filtered on the server by subjectId (see loadFirstGradePage).
        val matchesSubject = true

        val matchesStatus =
            selectedStatus == "All Status" || item.grade.status == selectedStatus

        matchesSearch && matchesClass && matchesSubject && matchesStatus
    }

    if (showDeleteDialog && selectedGradeToDelete != null) {
        SgrmsDeleteDialog(
            title = stringResource(R.string.delete_grade),
            message = stringResource(R.string.this_grade_record_will_be_removed),
            onConfirmClick = {
                selectedGradeToDelete?.let { gradeViewModel.deleteGrade(gradeId = it.gradeId) }
                showDeleteDialog = false
                selectedGradeToDelete = null
            },
            onDismissClick = {
                showDeleteDialog = false
                selectedGradeToDelete = null
            }
        )
    }

    SgrmsListScreen(
        title = stringResource(R.string.grades),
        subtitle = "${filteredRecords.size} shown · ${grades.size}${if (gradeUiState.hasMore) "+" else ""} loaded",
        searchText = searchText,
        onSearchChange = { searchText = it },
        searchPlaceholder = stringResource(R.string.search_student_subject_or_class),
        isLoading = isLoading && grades.isEmpty(),
        errorMessage = errorMessage,
        isEmpty = filteredRecords.isEmpty(),
        empty = EmptyContent(stringResource(R.string.no_grade_records), addGradesForAClassOrText, Icons.Default.Assessment),
        chipGroups = listOf(
            ChipGroup(subjectList, selectedSubject) { selectedSubject = it },
            ChipGroup(statusList, selectedStatus) { selectedStatus = it },
            ChipGroup(classList, selectedClass) { selectedClass = it }
        ),
        fab = ListFab(stringResource(R.string.add_grades), Icons.Default.EditNote, onAddGradeClick)
    ) {
        items(items = filteredRecords, key = { it.grade.gradeId }) { item ->
            Box(ListItemPadding) {
                SgrmsListItemCard(
                    title = item.studentName,
                    subtitle = stringResource(R.string.format_dot_pair, item.subjectName, item.className),
                    leading = { SgrmsAvatar(name = item.studentName, size = 44.dp) },
                    onClick = { onStudentDetailClick(item.grade.studentId) }
                ) {
                    val complete = item.grade.status.equals("Complete", ignoreCase = true)
                    if (complete) {
                        val letter = item.grade.gradeLetter.ifBlank { com.wilanwell.sgrms.utils.GradeCalculator.gradeLetter(item.grade.marks) }
                        SgrmsStatusChip("$letter · ${"%.0f".format(item.grade.marks)}", toneForGrade(letter))
                    } else {
                        SgrmsStatusChip(item.grade.status.ifBlank { stringResource(R.string.pending) }, ChipTone.Warning)
                    }
                    SgrmsOverflowMenu(
                        contentDescription = stringResource(R.string.actions_for, item.studentName),
                        actions = listOfNotNull(
                            RowAction(stringResource(R.string.view_student), Icons.Default.Visibility) { onStudentDetailClick(item.grade.studentId) },
                            if (access.canWriteSubject(subjectTeacher[item.grade.subjectId].orEmpty())) {
                                RowAction(stringResource(R.string.edit_grade), Icons.Default.Edit) { onEditGradeClick(item.grade.gradeId) }
                            } else null,
                            if (access.canWriteSubject(subjectTeacher[item.grade.subjectId].orEmpty())) {
                                RowAction(stringResource(R.string.delete), Icons.Default.Delete, destructive = true) {
                                    selectedGradeToDelete = item.grade
                                    showDeleteDialog = true
                                }
                            } else null
                        )
                    )
                }
            }
        }

        if (gradeUiState.hasMore) {
            item(key = "load_more") {
                LoadMoreButton(isLoading = gradeUiState.isLoadingMore, onClick = { gradeViewModel.loadMoreGrades() })
            }
        }
    }
}
