package com.wilanwell.sgrms.ui.student

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wilanwell.sgrms.ui.components.SgrmsButtonRow
import com.wilanwell.sgrms.ui.components.SgrmsDropdownField
import com.wilanwell.sgrms.ui.components.SgrmsEmptyStateCard
import com.wilanwell.sgrms.ui.components.SgrmsFormCard
import com.wilanwell.sgrms.ui.components.SgrmsTextField
import com.wilanwell.sgrms.ui.components.TopBar
import com.wilanwell.sgrms.ui.theme.ColorBackground
import com.wilanwell.sgrms.viewmodel.ClassFirebaseViewModel
import com.wilanwell.sgrms.viewmodel.StudentFirebaseViewModel
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.wilanwell.sgrms.ui.theme.SgrmsColors
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.filled.Edit
import com.wilanwell.sgrms.ui.components.SgrmsSegmentedField
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.fillMaxWidth

@Composable
fun EditStudentScreen(
    studentId: String = "",
    onBackClick: () -> Unit = {},
    onSaveClick: () -> Unit = {}
) {
    val studentViewModel: StudentFirebaseViewModel = hiltViewModel()

    val uiState by studentViewModel.uiState.collectAsState()

    val classViewModel: ClassFirebaseViewModel = hiltViewModel()
    val classUiState by classViewModel.uiState.collectAsState()

    val classIdMap = remember(classUiState.classes) {
        classUiState.classes
            .filter { it.className.isNotBlank() }
            .associate { it.className to it.classId }
    }
    val classList = remember(classIdMap) { classIdMap.keys.sorted() }

    val classDropdownList = if (classList.isEmpty()) {
        listOf("No class available")
    } else {
        classList
    }

    val genderList = listOf(
        "Male",
        "Female"
    )

    val statusList = listOf(
        "Active",
        "Inactive"
    )

    var studentName by remember { mutableStateOf("") }
    var studentCode by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }

    var selectedClass by remember { mutableStateOf("") }
    var selectedClassId by remember { mutableStateOf("") }
    var selectedGender by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf("") }

    var classExpanded by remember { mutableStateOf(false) }
    var genderExpanded by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }

    var emailError by remember { mutableStateOf(false) }
    var phoneNumberError by remember { mutableStateOf(false) }
    var classError by remember { mutableStateOf(false) }
    var genderError by remember { mutableStateOf(false) }
    var statusError by remember { mutableStateOf(false) }

    var hasFilledForm by remember { mutableStateOf(false) }
    var classLoadError by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        classViewModel.loadClasses()
    }

    LaunchedEffect(classUiState.errorMessage) {
        classLoadError = classUiState.errorMessage
    }

    LaunchedEffect(studentId) {
        if (studentId.isNotBlank()) {
            studentViewModel.loadStudentById(studentId)
        }
    }

    val selectedStudent = uiState.selectedStudent

    LaunchedEffect(selectedStudent) {
        val student = selectedStudent

        if (student != null && !hasFilledForm) {
            studentName = student.studentName
            studentCode = student.studentCode
            email = student.email
            phoneNumber = student.phoneNumber
            selectedClass = student.className
            selectedClassId = student.classId
            selectedGender = student.gender
            selectedStatus = student.status.ifBlank { "Active" }

            hasFilledForm = true
        }
    }

    fun clearMessages() {
        studentViewModel.clearMessage()
        classLoadError = ""
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorBackground)
            .imePadding()
    ) {
        TopBar(
            title = stringResource(R.string.edit_student),
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
            if (selectedStudent == null && !uiState.isLoading) {
                SgrmsEmptyStateCard(
                    icon = Icons.Default.Person,
                    title = stringResource(R.string.student_not_found),
                    message = stringResource(R.string.the_selected_student_record_could_not)
                )
            } else {
                SgrmsFormCard(
                    title = stringResource(R.string.student_information),
                    subtitle = stringResource(R.string.update_class_gender_status_and_contact),
                    icon = Icons.Default.Person
                ) {
                    SgrmsTextField(
                        value = studentName,
                        onValueChange = {},
                        label = stringResource(R.string.student_name),
                        placeholder = stringResource(R.string.student_name_2),
                        readOnly = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    SgrmsTextField(
                        value = studentCode,
                        onValueChange = {},
                        label = stringResource(R.string.student_code),
                        placeholder = stringResource(R.string.student_code_2),
                        readOnly = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    SgrmsTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            emailError = false
                            clearMessages()
                        },
                        label = stringResource(R.string.email),
                        placeholder = stringResource(R.string.example_student_sgrms_com),
                        icon = Icons.Default.Email,
                        isError = emailError,
                        errorMessage = stringResource(R.string.email_is_required)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    SgrmsTextField(
                        value = phoneNumber,
                        onValueChange = {
                            phoneNumber = it
                            phoneNumberError = false
                            clearMessages()
                        },
                        label = stringResource(R.string.phone_number),
                        placeholder = stringResource(R.string.example_0123456789),
                        icon = Icons.Default.Phone,
                        isError = phoneNumberError,
                        errorMessage = stringResource(R.string.phone_number_is_required)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    SgrmsDropdownField(
                        value = selectedClass,
                        label = stringResource(R.string.class_label),
                        placeholder = stringResource(R.string.select_class),
                        expanded = classExpanded,
                        onExpandedChange = {
                            classExpanded = it
                        },
                        items = classDropdownList,
                        onItemSelected = { selectedItem ->
                            if (selectedItem != "No class available") {
                                selectedClass = selectedItem
                                selectedClassId = classIdMap[selectedItem].orEmpty()
                                classError = false
                                clearMessages()
                            }
                        },
                        isError = classError,
                        errorMessage = stringResource(R.string.class_is_required)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    SgrmsSegmentedField(
                        label = stringResource(R.string.gender),
                        options = genderList,
                        selected = selectedGender,
                        onSelect = { it ->
                            selectedGender = it
                            genderError = false
                            clearMessages()
                        },
                        isError = genderError,
                        errorMessage = stringResource(R.string.gender_is_required)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    SgrmsSegmentedField(
                        label = stringResource(R.string.status),
                        options = statusList,
                        selected = selectedStatus,
                        onSelect = { it ->
                            selectedStatus = it
                            statusError = false
                            clearMessages()
                        },
                        isError = statusError,
                        errorMessage = stringResource(R.string.status_is_required)
                    )

                    if (uiState.errorMessage.isNotBlank() || classLoadError.isNotBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))

                        androidx.compose.material3.Text(
                            text = uiState.errorMessage.ifBlank {
                                classLoadError
                            },
                            color = SgrmsColors.Error
                        )
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
                                    confirmText = if (uiState.isLoading) {
                                        "Saving..."
                                    } else {
                                        "Save"
                                    },
                                    onCancelClick = onBackClick,
                                    onConfirmClick = {
                                        emailError = email.isBlank()
                                        phoneNumberError = phoneNumber.isBlank()
                                        classError = selectedClass.isBlank() ||
                                                selectedClass == "No class available"
                                        genderError = selectedGender.isBlank()
                                        statusError = selectedStatus.isBlank()
            
                                        val isValid = !emailError &&
                                                !phoneNumberError &&
                                                !classError &&
                                                !genderError &&
                                                !statusError
            
                                        val currentStudent = selectedStudent
            
                                        if (isValid && currentStudent != null) {
                                            // copy() keeps fields this form does not edit
                                            // (photo, guardian details, date of birth, ...).
                                            val updatedStudent = currentStudent.copy(
                                                email = email.trim(),
                                                phoneNumber = phoneNumber.trim(),
                                                classId = selectedClassId.ifBlank {
                                                    currentStudent.classId
                                                },
                                                className = selectedClass,
                                                gender = selectedGender,
                                                status = selectedStatus
                                            )
            
                                            studentViewModel.updateStudent(
                                                student = updatedStudent,
                                                onSuccess = onSaveClick
                                            )
                                        }
                                    }
                                )
        }
    }
}