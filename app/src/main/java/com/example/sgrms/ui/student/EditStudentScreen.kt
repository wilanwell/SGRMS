package com.example.sgrms.ui.student

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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.sgrms.ui.components.SgrmsButtonRow
import com.example.sgrms.ui.components.SgrmsDropdownField
import com.example.sgrms.ui.components.SgrmsEmptyStateCard
import com.example.sgrms.ui.components.SgrmsFormCard
import com.example.sgrms.ui.components.SgrmsTextField
import com.example.sgrms.ui.components.TopBar
import com.example.sgrms.ui.theme.ColorBackground
import com.example.sgrms.viewmodel.ClassFirebaseViewModel
import com.example.sgrms.viewmodel.StudentFirebaseViewModel
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.sgrms.ui.theme.SgrmsColors

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
            .navigationBarsPadding()
    ) {
        TopBar(
            title = "Edit Student",
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
            if (selectedStudent == null && !uiState.isLoading) {
                SgrmsEmptyStateCard(
                    icon = Icons.Default.Person,
                    title = "Student not found",
                    message = "The selected student record could not be loaded."
                )
            } else {
                SgrmsFormCard(
                    title = "Student Information",
                    subtitle = "Update class, gender, status, and contact information",
                    icon = Icons.Default.Person
                ) {
                    SgrmsTextField(
                        value = studentName,
                        onValueChange = {},
                        label = "Student Name",
                        placeholder = "Student name",
                        readOnly = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    SgrmsTextField(
                        value = studentCode,
                        onValueChange = {},
                        label = "Student Code",
                        placeholder = "Student code",
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
                        label = "Email",
                        placeholder = "Example: student@sgrms.com",
                        icon = Icons.Default.Email,
                        isError = emailError,
                        errorMessage = "Email is required"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    SgrmsTextField(
                        value = phoneNumber,
                        onValueChange = {
                            phoneNumber = it
                            phoneNumberError = false
                            clearMessages()
                        },
                        label = "Phone Number",
                        placeholder = "Example: 0123456789",
                        icon = Icons.Default.Phone,
                        isError = phoneNumberError,
                        errorMessage = "Phone number is required"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    SgrmsDropdownField(
                        value = selectedClass,
                        label = "Class",
                        placeholder = "Select class",
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
                        errorMessage = "Class is required"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    SgrmsDropdownField(
                        value = selectedGender,
                        label = "Gender",
                        placeholder = "Select gender",
                        expanded = genderExpanded,
                        onExpandedChange = {
                            genderExpanded = it
                        },
                        items = genderList,
                        onItemSelected = {
                            selectedGender = it
                            genderError = false
                            clearMessages()
                        },
                        isError = genderError,
                        errorMessage = "Gender is required"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    SgrmsDropdownField(
                        value = selectedStatus,
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
                            clearMessages()
                        },
                        isError = statusError,
                        errorMessage = "Status is required"
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

                    Spacer(modifier = Modifier.height(24.dp))

                    SgrmsButtonRow(
                        cancelText = "Cancel",
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
    }
}