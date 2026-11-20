package com.example.sgrms.ui.grade

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
import com.example.sgrms.data.model.GradeModel
import com.example.sgrms.ui.components.SgrmsButtonRow
import com.example.sgrms.ui.components.SgrmsDropdownField
import com.example.sgrms.ui.components.SgrmsFormCard
import com.example.sgrms.ui.components.SgrmsTextField
import com.example.sgrms.ui.components.TopBar
import com.example.sgrms.ui.theme.BackgroundGrey
import com.example.sgrms.viewmodel.GradeFirebaseViewModel
import com.example.sgrms.viewmodel.StudentFirebaseViewModel
import com.example.sgrms.viewmodel.SubjectFirebaseViewModel
import com.example.sgrms.utils.GradeCalculator
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun EditGradeScreen(
    gradeId: String = "",
    onBackClick: () -> Unit = {},
    onSaveClick: () -> Unit = {}
) {
    val gradeViewModel: GradeFirebaseViewModel = hiltViewModel()

    val studentViewModel: StudentFirebaseViewModel = hiltViewModel()

    val subjectViewModel: SubjectFirebaseViewModel = hiltViewModel()

    val gradeUiState by gradeViewModel.uiState.collectAsState()
    val studentUiState by studentViewModel.uiState.collectAsState()
    val subjectUiState by subjectViewModel.uiState.collectAsState()

    val selectedGrade = gradeUiState.selectedGrade
    val studentList = studentUiState.students
    val subjectList = subjectUiState.subjects

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
        listOf("No student available")
    } else {
        studentList.map { student ->
            val className = student.className.ifBlank { "Not assigned" }
            "${student.studentName} (${student.studentCode}) - $className"
        }
    }

    val subjectDropdownItems = if (subjectList.isEmpty()) {
        listOf("No subject available")
    } else {
        subjectList.map { subject ->
            "${subject.subjectName} (${subject.subjectCode})"
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
                val className = student.className.ifBlank { "Not assigned" }
                "${student.studentName} (${student.studentCode}) - $className"
            } else {
                ""
            }

            selectedSubjectDisplay = if (subject != null) {
                "${subject.subjectName} (${subject.subjectCode})"
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
            .background(BackgroundGrey)
            .navigationBarsPadding()
    ) {
        TopBar(
            title = "Edit Grade",
            showBack = true,
            onBackClick = onBackClick,
            showIcon = false
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = 32.dp
                )
        ) {
            SgrmsFormCard(
                title = "Grade Information",
                subtitle = "Update student subject mark and grade",
                icon = Icons.Default.Edit
            ) {
                SgrmsDropdownField(
                    value = selectedStudentDisplay.ifBlank { "Select student" },
                    label = "Student",
                    placeholder = "Select student",
                    expanded = studentExpanded,
                    onExpandedChange = {
                        studentExpanded = it
                    },
                    items = studentDropdownItems,
                    onItemSelected = { selectedText ->
                        if (selectedText != "No student available") {
                            val selectedStudent = studentList.find { student ->
                                val className = student.className.ifBlank { "Not assigned" }
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
                    errorMessage = "Student is required"
                )

                Spacer(modifier = Modifier.height(16.dp))

                SgrmsDropdownField(
                    value = selectedSubjectDisplay.ifBlank { "Select subject" },
                    label = "Subject",
                    placeholder = "Select subject",
                    expanded = subjectExpanded,
                    onExpandedChange = {
                        subjectExpanded = it
                    },
                    items = subjectDropdownItems,
                    onItemSelected = { selectedText ->
                        if (selectedText != "No subject available") {
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
                    errorMessage = "Subject is required"
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
                    label = "Marks",
                    placeholder = "Enter marks",
                    isError = marksError,
                    errorMessage = "Marks must be between 0 and 100",
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
                    label = "Grade Letter",
                    placeholder = "Auto generated grade",
                    readOnly = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                SgrmsDropdownField(
                    value = selectedStatus.ifBlank { "Select status" },
                    label = "Status",
                    placeholder = "Select status",
                    expanded = statusExpanded,
                    onExpandedChange = {
                        statusExpanded = it
                    },
                    items = statusList,
                    onItemSelected = {
                        selectedStatus = it
                        statusError = false
                        statusExpanded = false
                        clearMessages()
                    },
                    isError = statusError,
                    errorMessage = "Status is required"
                )

                Spacer(modifier = Modifier.height(16.dp))

                SgrmsTextField(
                    value = remarks,
                    onValueChange = {
                        remarks = it
                        clearMessages()
                    },
                    label = "Remarks",
                    placeholder = "Optional notes",
                    singleLine = false,
                    modifier = Modifier.height(110.dp)
                )

                if (errorMessage.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))

                    androidx.compose.material3.Text(
                        text = errorMessage,
                        color = androidx.compose.ui.graphics.Color.Red
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                SgrmsButtonRow(
                    cancelText = "Cancel",
                    confirmText = if (isLoading) {
                        "Saving..."
                    } else {
                        "Save Changes"
                    },
                    onCancelClick = onBackClick,
                    onConfirmClick = {
                        val marksValue = marks.toDoubleOrNull()

                        studentError = selectedStudentId.isBlank() ||
                                selectedStudentDisplay == "No student available"

                        subjectError = selectedSubjectId.isBlank() ||
                                selectedSubjectDisplay == "No subject available"

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
                            localErrorMessage = "Invalid grade ID"
                        }
                    }
                )
            }
        }
    }
}