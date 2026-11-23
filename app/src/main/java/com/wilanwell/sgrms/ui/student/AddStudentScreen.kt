package com.wilanwell.sgrms.ui.student

import com.wilanwell.sgrms.R
import androidx.compose.ui.res.stringResource
import com.wilanwell.sgrms.ui.components.SgrmsPasswordField
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
import androidx.compose.material.icons.filled.PersonAdd
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
import androidx.compose.material.icons.filled.Add
import com.wilanwell.sgrms.ui.components.SgrmsSegmentedField
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.fillMaxWidth

@Composable
fun AddStudentScreen(
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
    var password by remember { mutableStateOf("") }

    var selectedClass by remember { mutableStateOf("") }
    var selectedClassId by remember { mutableStateOf("") }
    var selectedGender by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf("Active") }

    var classExpanded by remember { mutableStateOf(false) }
    var genderExpanded by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }

    var studentNameError by remember { mutableStateOf(false) }
    var studentCodeError by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }
    var phoneNumberError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var classError by remember { mutableStateOf(false) }
    var genderError by remember { mutableStateOf(false) }

    var classLoadError by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        classViewModel.loadClasses()
    }

    LaunchedEffect(classUiState.errorMessage) {
        classLoadError = classUiState.errorMessage
    }

    fun clearMessages() {
        studentViewModel.clearMessage()
        classLoadError = ""
    }

    fun validateInput(): Boolean {
        studentNameError = studentName.isBlank()
        studentCodeError = studentCode.isBlank()
        emailError = email.isBlank()
        phoneNumberError = phoneNumber.isBlank()
        passwordError = password.isBlank()
        classError = selectedClass.isBlank() || selectedClass == "No class available"
        genderError = selectedGender.isBlank()

        return !studentNameError &&
                !studentCodeError &&
                !emailError &&
                !phoneNumberError &&
                !passwordError &&
                !classError &&
                !genderError
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorBackground)
            .imePadding()
    ) {
        TopBar(
            title = stringResource(R.string.add_student_title),
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
                title = stringResource(R.string.student_information),
                subtitle = stringResource(R.string.enter_new_student_details),
                icon = Icons.Default.PersonAdd
            ) {
                SgrmsTextField(
                    value = studentName,
                    onValueChange = {
                        studentName = it
                        studentNameError = false
                        clearMessages()
                    },
                    label = stringResource(R.string.student_name_title),
                    placeholder = stringResource(R.string.enter_student_name),
                    isError = studentNameError,
                    errorMessage = stringResource(R.string.student_name_is_required)
                )

                Spacer(modifier = Modifier.height(16.dp))

                SgrmsTextField(
                    value = studentCode,
                    onValueChange = {
                        studentCode = it
                        studentCodeError = false
                        clearMessages()
                    },
                    label = stringResource(R.string.student_code_title),
                    placeholder = stringResource(R.string.example_s001),
                    isError = studentCodeError,
                    errorMessage = stringResource(R.string.student_code_is_required)
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
                    onItemSelected = { className ->
                        if (className != "No class available") {
                            selectedClass = className
                            selectedClassId = classIdMap[className].orEmpty()
                            classError = false
                            clearMessages()
                        }

                        classExpanded = false
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
                        clearMessages()
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                SgrmsPasswordField(
                    value = password,
                    onValueChange = {
                        password = it
                        passwordError = false
                        clearMessages()
                    },
                    label = stringResource(R.string.login_password),
                    placeholder = stringResource(R.string.enter_student_login_password),
                    isError = passwordError,
                    errorMessage = stringResource(R.string.password_is_required)
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

            Spacer(modifier = Modifier.height(36.dp))
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
                                    if (validateInput()) {
                                        studentViewModel.addStudentWithAccount(
                                            studentCode = studentCode.trim(),
                                            studentName = studentName.trim(),
                                            email = email.trim(),
                                            phoneNumber = phoneNumber.trim(),
                                            classId = selectedClassId,
                                            className = selectedClass,
                                            gender = selectedGender,
                                            status = selectedStatus,
                                            password = password.trim(),
                                            onSuccess = onSaveClick
                                        )
                                    }
                                }
                            )
        }
    }
}