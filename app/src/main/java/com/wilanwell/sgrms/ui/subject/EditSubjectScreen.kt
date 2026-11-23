package com.wilanwell.sgrms.ui.subject

import com.wilanwell.sgrms.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wilanwell.sgrms.data.model.SubjectModel
import com.wilanwell.sgrms.ui.components.SgrmsButtonRow
import com.wilanwell.sgrms.ui.components.SgrmsDropdownField
import com.wilanwell.sgrms.ui.components.SgrmsEmptyStateCard
import com.wilanwell.sgrms.ui.components.SgrmsFormCard
import com.wilanwell.sgrms.ui.components.SgrmsTextField
import com.wilanwell.sgrms.ui.components.TopBar
import com.wilanwell.sgrms.ui.theme.ColorBackground
import com.wilanwell.sgrms.ui.theme.ColorOnSurfaceVariant
import com.wilanwell.sgrms.viewmodel.SubjectFirebaseViewModel
import com.wilanwell.sgrms.viewmodel.TeacherFirebaseViewModel
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.wilanwell.sgrms.ui.theme.SgrmsColors
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.imePadding

@Composable
fun EditSubjectScreen(
    subjectId: String = "",
    onBackClick: () -> Unit = {},
    onSaveClick: () -> Unit = {}
) {
    val noStaffIdText = stringResource(R.string.no_staff_id)
    val selectTeacherText = stringResource(R.string.select_teacher)
    val text5Text = stringResource(R.string.text_5)
    val subjectViewModel: SubjectFirebaseViewModel = hiltViewModel()

    val teacherViewModel: TeacherFirebaseViewModel = hiltViewModel()

    val subjectUiState by subjectViewModel.uiState.collectAsState()
    val teacherUiState by teacherViewModel.uiState.collectAsState()

    val categoryList = listOf(
        "Core",
        "Humanities",
        "Applied",
        "Elective"
    )

    var subjectCode by remember { mutableStateOf("") }
    var subjectName by remember { mutableStateOf("") }

    var selectedTeacherDisplay by remember { mutableStateOf("") }
    var selectedTeacherId by remember { mutableStateOf("") }
    var selectedTeacherName by remember { mutableStateOf("") }
    var teacherExpanded by remember { mutableStateOf(false) }

    var selectedCategory by remember { mutableStateOf("") }
    var categoryExpanded by remember { mutableStateOf(false) }

    var subjectCodeError by remember { mutableStateOf(false) }
    var subjectNameError by remember { mutableStateOf(false) }
    var teacherError by remember { mutableStateOf(false) }
    var categoryError by remember { mutableStateOf(false) }

    var hasFilledForm by remember { mutableStateOf(false) }

    LaunchedEffect(subjectId) {
        if (subjectId.isNotBlank()) {
            subjectViewModel.loadSubjectById(subjectId)
            teacherViewModel.loadTeachers()
        }
    }

    val teachers = teacherUiState.teachers

    val teacherDropdownItems = if (teachers.isEmpty()) {
        listOf("No teacher available")
    } else {
        teachers.map { teacher ->
            val staffIdText = teacher.staffId.ifBlank { stringResource(R.string.no_staff_id) }
            text5Text.format(teacher.teacherName, staffIdText)
        }
    }

    LaunchedEffect(subjectUiState.selectedSubject, teachers) {
        val subject = subjectUiState.selectedSubject

        if (subject != null && !hasFilledForm) {
            subjectCode = subject.subjectCode
            subjectName = subject.subjectName

            selectedTeacherId = subject.teacherId
            selectedTeacherName = subject.teacherName
            selectedCategory = subject.category

            val selectedTeacher = teachers.find { teacher ->
                teacher.teacherId == subject.teacherId
            }

            selectedTeacherDisplay = if (selectedTeacher != null) {
                val staffIdText = selectedTeacher.staffId.ifBlank { noStaffIdText }
                text5Text.format(selectedTeacher.teacherName, staffIdText)
            } else {
                subject.teacherName.ifBlank { selectTeacherText }
            }

            hasFilledForm = true
        }
    }

    val isLoading = subjectUiState.isLoading || teacherUiState.isLoading

    val errorMessage = when {
        subjectUiState.errorMessage.isNotBlank() -> subjectUiState.errorMessage
        teacherUiState.errorMessage.isNotBlank() -> teacherUiState.errorMessage
        else -> ""
    }

    fun validateInput(): Boolean {
        subjectCodeError = subjectCode.isBlank()
        subjectNameError = subjectName.isBlank()
        teacherError = selectedTeacherId.isBlank() ||
                selectedTeacherDisplay == "No teacher available"
        categoryError = selectedCategory.isBlank()

        return !subjectCodeError &&
                !subjectNameError &&
                !teacherError &&
                !categoryError
    }

    fun clearMessages() {
        subjectViewModel.clearMessage()
        teacherViewModel.clearMessage()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorBackground)
            .imePadding()
    ) {
        TopBar(
            title = stringResource(R.string.edit_subject),
            showBack = true,
            onBackClick = onBackClick,
            showIcon = false
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = 32.dp
                )
        ) {
            when {
                subjectUiState.isLoading && subjectUiState.selectedSubject == null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = com.wilanwell.sgrms.ui.theme.ColorPrimary
                        )
                    }
                }

                subjectUiState.selectedSubject == null &&
                        subjectId.isNotBlank() &&
                        !subjectUiState.isLoading &&
                        subjectUiState.errorMessage.isBlank() -> {
                    SgrmsEmptyStateCard(
                        icon = Icons.Default.Edit,
                        title = stringResource(R.string.subject_not_found),
                        message = stringResource(R.string.the_selected_subject_record_could_not)
                    )
                }

                else -> {
                    SgrmsFormCard(
                        title = stringResource(R.string.subject_information),
                        subtitle = stringResource(R.string.update_the_subject_details),
                        icon = Icons.Default.Edit
                    ) {
                        SgrmsTextField(
                            value = subjectCode,
                            onValueChange = {
                                subjectCode = it
                                subjectCodeError = false
                                clearMessages()
                            },
                            label = stringResource(R.string.subject_code),
                            placeholder = stringResource(R.string.example_math101),
                            icon = Icons.Default.Badge,
                            isError = subjectCodeError,
                            errorMessage = stringResource(R.string.subject_code_is_required)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        SgrmsTextField(
                            value = subjectName,
                            onValueChange = {
                                subjectName = it
                                subjectNameError = false
                                clearMessages()
                            },
                            label = stringResource(R.string.subject_name),
                            placeholder = stringResource(R.string.example_mathematics),
                            icon = Icons.Default.MenuBook,
                            isError = subjectNameError,
                            errorMessage = stringResource(R.string.subject_name_is_required)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        SgrmsDropdownField(
                            value = selectedTeacherDisplay.ifBlank { stringResource(R.string.select_teacher) },
                            label = stringResource(R.string.teacher_name),
                            placeholder = stringResource(R.string.select_teacher),
                            expanded = teacherExpanded,
                            onExpandedChange = {
                                teacherExpanded = it
                            },
                            items = teacherDropdownItems,
                            onItemSelected = { selectedText ->
                                if (selectedText != "No teacher available") {
                                    val selectedTeacher = teachers.find { teacher ->
                                        val staffIdText = teacher.staffId.ifBlank { noStaffIdText }
                                        "${teacher.teacherName} ($staffIdText)" == selectedText
                                    }

                                    if (selectedTeacher != null) {
                                        selectedTeacherDisplay = selectedText
                                        selectedTeacherId = selectedTeacher.teacherId
                                        selectedTeacherName = selectedTeacher.teacherName
                                        teacherError = false
                                        clearMessages()
                                    }
                                }

                                teacherExpanded = false
                            },
                            isError = teacherError,
                            errorMessage = stringResource(R.string.teacher_is_required)
                        )

                        if (selectedTeacherId.isNotBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = stringResource(R.string.teacher_id_will_be_updated_automatically),
                                fontSize = 13.sp,
                                color = ColorOnSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        SgrmsDropdownField(
                            value = selectedCategory.ifBlank { stringResource(R.string.select_category) },
                            label = stringResource(R.string.category),
                            placeholder = stringResource(R.string.select_category),
                            expanded = categoryExpanded,
                            onExpandedChange = {
                                categoryExpanded = it
                            },
                            items = categoryList,
                            onItemSelected = {
                                selectedCategory = it
                                categoryError = false
                                categoryExpanded = false
                                clearMessages()
                            },
                            isError = categoryError,
                            errorMessage = stringResource(R.string.category_is_required)
                        )

                        if (errorMessage.isNotBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = errorMessage,
                                color = SgrmsColors.Error,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }



                        Spacer(modifier = Modifier.height(36.dp))
                    }
                }
            }
        }

        Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 8.dp) {
            SgrmsButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                                        cancelText = stringResource(R.string.cancel),
                                        confirmText = if (isLoading) {
                                            "Saving..."
                                        } else {
                                            "Save"
                                        },
                                        onCancelClick = onBackClick,
                                        onConfirmClick = {
                                            if (validateInput() && subjectId.isNotBlank()) {
                                                val updatedSubject = SubjectModel(
                                                    subjectId = subjectId,
                                                    subjectCode = subjectCode.trim(),
                                                    subjectName = subjectName.trim(),
                                                    teacherId = selectedTeacherId,
                                                    teacherName = selectedTeacherName,
                                                    category = selectedCategory,
                                                    createdAt = subjectUiState.selectedSubject?.createdAt
                                                        ?: System.currentTimeMillis()
                                                )
            
                                                subjectViewModel.updateSubject(
                                                    subject = updatedSubject,
                                                    onSuccess = onSaveClick
                                                )
                                            }
                                        }
                                    )
        }
    }
}