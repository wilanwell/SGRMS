package com.wilanwell.sgrms.ui.grade

import com.wilanwell.sgrms.ui.components.LocalSessionAccess
import com.wilanwell.sgrms.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.wilanwell.sgrms.data.model.GradeModel
import com.wilanwell.sgrms.ui.components.SgrmsButtonRow
import com.wilanwell.sgrms.ui.components.SgrmsDropdownField
import com.wilanwell.sgrms.ui.components.SgrmsFormCard
import com.wilanwell.sgrms.ui.components.SgrmsTextField
import com.wilanwell.sgrms.ui.components.TopBar
import com.wilanwell.sgrms.ui.theme.ColorBackground
import com.wilanwell.sgrms.viewmodel.GradeFirebaseViewModel
import com.wilanwell.sgrms.viewmodel.StudentFirebaseViewModel
import com.wilanwell.sgrms.viewmodel.SubjectFirebaseViewModel
import com.wilanwell.sgrms.utils.GradeCalculator
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.wilanwell.sgrms.ui.theme.SgrmsColors
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import com.wilanwell.sgrms.ui.components.SgrmsSegmentedField
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.fillMaxWidth

@Composable
fun EditGradeScreen(
    gradeId: String = "",
    onBackClick: () -> Unit = {},
    onSaveClick: () -> Unit = {}
) {
    val noStudentAvailableText = stringResource(R.string.no_student_available)
    val noSubjectAvailableText = stringResource(R.string.no_subject_available)
    val notAssignedText = stringResource(R.string.not_assigned)
    val invalidGradeIdText = stringResource(R.string.invalid_grade_id)
    val saveChangesText = stringResource(R.string.save_changes)
    val text4Text = stringResource(R.string.format_name_detail_extra)
    val text5Text = stringResource(R.string.format_with_brackets)
    val gradeViewModel: GradeFirebaseViewModel = hiltViewModel()

    val studentViewModel: StudentFirebaseViewModel = hiltViewModel()

    val subjectViewModel: SubjectFirebaseViewModel = hiltViewModel()

    val gradeUiState by gradeViewModel.uiState.collectAsState()
    val studentUiState by studentViewModel.uiState.collectAsState()
    val subjectUiState by subjectViewModel.uiState.collectAsState()

    val selectedGrade = gradeUiState.selectedGrade
    val studentList = studentUiState.students
    // Teachers grade only the subjects they teach (enforced by firestore.rules).
    val access = LocalSessionAccess.current
    val subjectList = subjectUiState.subjects.filter { access.canWriteSubject(it.teacherId) }

    val statusList = listOf(
        "Complete",
        "Pending"
    )

    var selectedStudentDisplay by remember { mutableStateOf("") }
    var selectedSubjectDisplay by remember { mutableStateOf("") }
    var selectedStudentId by remember { mutableStateOf("") }
    var selectedSubjectId by remember { mutableStateOf("") }

    var marks by remember { mutableStateOf("") }
    var gradeLetter by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf("") }
    var remarks by remember { mutableStateOf("") }

    var studentExpanded by remember { mutableStateOf(false) }
    var subjectExpanded by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }

    var studentError by remember { mutableStateOf(false) }
    var subjectError by remember { mutableStateOf(false) }
    var marksError by remember { mutableStateOf(false) }
    var statusError by remember { mutableStateOf(false) }

    var hasFilledForm by remember { mutableStateOf(false) }
    var localErrorMessage by remember { mutableStateOf("") }

    LaunchedEffect(gradeId) {
        if (gradeId.isNotBlank()) {
            gradeViewModel.loadGradeById(gradeId)
            studentViewModel.loadStudents()
            subjectViewModel.loadSubjects()
        }
    }

    val studentDropdownItems = if (studentList.isEmpty()) {
        listOf(noStudentAvailableText)
    } else {
        studentList.map { student ->
            val className = student.className.ifBlank { stringResource(R.string.not_assigned) }
            text4Text.format(student.studentName, student.studentCode, className)
        }
    }

    val subjectDropdownItems = if (subjectList.isEmpty()) {
        listOf(noSubjectAvailableText)
    } else {
        subjectList.map { subject ->
            text5Text.format(subject.subjectName, subject.subjectCode)
        }
    }

    LaunchedEffect(selectedGrade, studentList, subjectList) {
        if (
            selectedGrade != null &&
            studentList.isNotEmpty() &&
            subjectList.isNotEmpty() &&
            !hasFilledForm
        ) {
            selectedStudentId = selectedGrade.studentId
            selectedSubjectId = selectedGrade.subjectId
            marks = selectedGrade.marks.toString()
            gradeLetter = selectedGrade.gradeLetter
            selectedStatus = selectedGrade.status
            remarks = selectedGrade.remarks

            val student = studentList.find { student ->
                student.studentId == selectedGrade.studentId
            }

            val subject = subjectList.find { subject ->
                subject.subjectId == selectedGrade.subjectId
            }

            selectedStudentDisplay = if (student != null) {
                val className = student.className.ifBlank { notAssignedText }
                text4Text.format(student.studentName, student.studentCode, className)
            } else {
                ""
            }

            selectedSubjectDisplay = if (subject != null) {
                text5Text.format(subject.subjectName, subject.subjectCode)
            } else {
                ""
            }

            hasFilledForm = true
        }
    }

    val isLoading =
        gradeUiState.isLoading ||
                studentUiState.isLoading ||
                subjectUiState.isLoading

    val errorMessage = when {
        localErrorMessage.isNotBlank() -> localErrorMessage
        gradeUiState.errorMessage.isNotBlank() -> gradeUiState.errorMessage
        studentUiState.errorMessage.isNotBlank() -> studentUiState.errorMessage
        subjectUiState.errorMessage.isNotBlank() -> subjectUiState.errorMessage
        else -> ""
    }

    fun clearMessages() {
        localErrorMessage = ""
        gradeViewModel.clearMessage()
        studentViewModel.clearMessage()
        subjectViewModel.clearMessage()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorBackground)
            .imePadding()
    ) {
        TopBar(
            title = stringResource(R.string.edit_grade_title),
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
            SgrmsFormCard(
                title = stringResource(R.string.grade_information),
                subtitle = stringResource(R.string.update_student_subject_mark_and_grade),
                icon = Icons.Default.Edit
            ) {
                SgrmsDropdownField(
                    value = selectedStudentDisplay.ifBlank { stringResource(R.string.select_student) },
                    label = stringResource(R.string.student),
                    placeholder = stringResource(R.string.select_student),
                    expanded = studentExpanded,
                    onExpandedChange = {
                        studentExpanded = it
                    },
                    items = studentDropdownItems,
                    onItemSelected = { selectedText ->
                        if (selectedText != noStudentAvailableText) {
                            val selectedStudent = studentList.find { student ->
                                val className = student.className.ifBlank { notAssignedText }
                                "${student.studentName} (${student.studentCode}) - $className" == selectedText
                            }

                            if (selectedStudent != null) {
                                selectedStudentDisplay = selectedText
                                selectedStudentId = selectedStudent.studentId
                                studentError = false
                                clearMessages()
                            }
                        }

                        studentExpanded = false
                    },
                    isError = studentError,
                    errorMessage = stringResource(R.string.student_is_required)
                )

                Spacer(modifier = Modifier.height(16.dp))

                SgrmsDropdownField(
                    value = selectedSubjectDisplay.ifBlank { stringResource(R.string.select_subject) },
                    label = stringResource(R.string.subject),
                    placeholder = stringResource(R.string.select_subject),
                    expanded = subjectExpanded,
                    onExpandedChange = {
                        subjectExpanded = it
                    },
                    items = subjectDropdownItems,
                    onItemSelected = { selectedText ->
                        if (selectedText != noSubjectAvailableText) {
                            val selectedSubject = subjectList.find { subject ->
                                "${subject.subjectName} (${subject.subjectCode})" == selectedText
                            }

                            if (selectedSubject != null) {
                                selectedSubjectDisplay = selectedText
                                selectedSubjectId = selectedSubject.subjectId
                                subjectError = false
                                clearMessages()
                            }
                        }

                        subjectExpanded = false
                    },
                    isError = subjectError,
                    errorMessage = stringResource(R.string.subject_is_required)
                )

                Spacer(modifier = Modifier.height(16.dp))

                SgrmsTextField(
                    value = marks,
                    onValueChange = { value ->
                        if (
                            value.isEmpty() ||
                            value.matches(Regex("^\\d{0,3}(\\.\\d{0,2})?$"))
                        ) {
                            val marksValue = value.toDoubleOrNull()

                            if (marksValue == null || marksValue <= 100.0) {
                                marks = value
                                gradeLetter = GradeCalculator.gradeLetter(value)
                                marksError = false
                                clearMessages()
                            }
                        }
                    },
                    label = stringResource(R.string.marks),
                    placeholder = stringResource(R.string.enter_marks),
                    isError = marksError,
                    errorMessage = stringResource(R.string.marks_must_be_between_0_and),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                SgrmsTextField(
                    value = gradeLetter,
                    onValueChange = {
                        gradeLetter = it
                    },
                    label = stringResource(R.string.grade_letter),
                    placeholder = stringResource(R.string.auto_generated_grade),
                    readOnly = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                SgrmsSegmentedField(
                    label = stringResource(R.string.status),
                    options = statusList,
                    selected = selectedStatus.ifBlank { stringResource(R.string.select_status) },
                    onSelect = { it ->
                        selectedStatus = it
                        statusError = false
clearMessages()
                    },
                    isError = statusError,
                    errorMessage = stringResource(R.string.status_is_required)
                )

                Spacer(modifier = Modifier.height(16.dp))

                SgrmsTextField(
                    value = remarks,
                    onValueChange = {
                        remarks = it
                        clearMessages()
                    },
                    label = stringResource(R.string.remarks),
                    placeholder = stringResource(R.string.optional_notes),
                    singleLine = false,
                    modifier = Modifier.height(110.dp)
                )

                if (errorMessage.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))

                    androidx.compose.material3.Text(
                        text = errorMessage,
                        color = SgrmsColors.Error
                    )
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
                                    saveChangesText
                                },
                                onCancelClick = onBackClick,
                                onConfirmClick = {
                                    val marksValue = marks.toDoubleOrNull()
            
                                    studentError = selectedStudentId.isBlank() ||
                                            selectedStudentDisplay == noStudentAvailableText
            
                                    subjectError = selectedSubjectId.isBlank() ||
                                            selectedSubjectDisplay == noSubjectAvailableText
            
                                    marksError = marksValue == null ||
                                            marksValue < 0.0 ||
                                            marksValue > 100.0
            
                                    statusError = selectedStatus.isBlank()
            
                                    val isValid = !studentError &&
                                            !subjectError &&
                                            !marksError &&
                                            !statusError
            
                                    if (isValid && marksValue != null && gradeId.isNotBlank()) {
                                        val updatedGrade = GradeModel(
                                            gradeId = gradeId,
                                            studentId = selectedStudentId,
                                            subjectId = selectedSubjectId,
                                            marks = marksValue,
                                            gradeLetter = GradeCalculator.gradeLetter(marks),
                                            status = selectedStatus,
                                            remarks = remarks.trim(),
                                            createdByTeacherId = selectedGrade?.createdByTeacherId ?: "",
                                            createdAt = selectedGrade?.createdAt ?: System.currentTimeMillis()
                                        )
            
                                        gradeViewModel.updateGrade(
                                            grade = updatedGrade,
                                            subjectName = selectedSubjectDisplay,
                                            onSuccess = onSaveClick
                                        )
                                    } else if (gradeId.isBlank()) {
                                        localErrorMessage = invalidGradeIdText
                                    }
                                }
                            )
        }
    }
}