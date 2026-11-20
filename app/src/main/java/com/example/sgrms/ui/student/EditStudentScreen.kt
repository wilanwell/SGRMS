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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sgrms.data.firebase.StudentFirebaseService
import com.example.sgrms.data.model.StudentModel
import com.example.sgrms.data.repository.StudentFirebaseRepository
import com.example.sgrms.ui.components.SgrmsButtonRow
import com.example.sgrms.ui.components.SgrmsDropdownField
import com.example.sgrms.ui.components.SgrmsEmptyStateCard
import com.example.sgrms.ui.components.SgrmsFormCard
import com.example.sgrms.ui.components.SgrmsTextField
import com.example.sgrms.ui.components.TopBar
import com.example.sgrms.ui.theme.BackgroundGrey
import com.example.sgrms.viewmodel.StudentFirebaseViewModel
import com.example.sgrms.viewmodel.StudentFirebaseViewModelFactory
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun EditStudentScreen(
    studentId: String = "",
    onBackClick: () -> Unit = {},
    onSaveClick: () -> Unit = {}
) {
    val studentFirebaseService = remember {
        StudentFirebaseService()
    }

    val studentFirebaseRepository = remember {
        StudentFirebaseRepository(studentFirebaseService)
    }

    val studentViewModel: StudentFirebaseViewModel = viewModel(
        factory = StudentFirebaseViewModelFactory(studentFirebaseRepository)
    )

    val uiState by studentViewModel.uiState.collectAsState()

    val firestore = remember {
        FirebaseFirestore.getInstance()
    }

    var classList by remember { mutableStateOf<List<String>>(emptyList()) }
    val classIdMap = remember { mutableStateMapOf<String, String>() }

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
        try {
            classLoadError = ""

            val classSnapshot = firestore
                .collection("classes")
                .get()
                .await()

            val loadedClasses = classSnapshot.documents.mapNotNull { document ->
                val className = document.getString("className").orEmpty()

                if (className.isNotBlank()) {
                    classIdMap[className] = document.id
                    className
                } else {
                    null
                }
            }
                .distinct()
                .sorted()

            classList = loadedClasses
        } catch (e: Exception) {
            classLoadError = e.message ?: "Failed to load class list"
        }
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
            .background(BackgroundGrey)
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
                            color = androidx.compose.ui.graphics.Color.Red
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
                                val updatedStudent = StudentModel(
                                    studentId = currentStudent.studentId,
                                    studentCode = currentStudent.studentCode,
                                    studentName = currentStudent.studentName,
                                    email = email.trim(),
                                    phoneNumber = phoneNumber.trim(),
                                    classId = selectedClassId.ifBlank {
                                        currentStudent.classId
                                    },
                                    className = selectedClass,
                                    userId = currentStudent.userId,
                                    gender = selectedGender,
                                    status = selectedStatus,
                                    createdAt = currentStudent.createdAt
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