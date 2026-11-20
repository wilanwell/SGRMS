package com.example.sgrms.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sgrms.data.firebase.TeacherFirebaseService
import com.example.sgrms.data.repository.TeacherFirebaseRepository
import com.example.sgrms.ui.components.TopBar
import com.example.sgrms.ui.theme.BackgroundGrey
import com.example.sgrms.ui.theme.CardWhite
import com.example.sgrms.ui.theme.PrimaryBlue
import com.example.sgrms.ui.theme.TextGrey
import com.example.sgrms.viewmodel.TeacherFirebaseViewModel
import com.example.sgrms.viewmodel.TeacherFirebaseViewModelFactory
import com.example.sgrms.ui.components.SgrmsButtonRow

@Composable
fun AddTeacherScreen(
    onBackClick: () -> Unit = {},
    onSaveClick: () -> Unit = {}
) {
    val teacherFirebaseService = remember {
        TeacherFirebaseService()
    }

    val teacherFirebaseRepository = remember {
        TeacherFirebaseRepository(teacherFirebaseService)
    }

    val teacherViewModel: TeacherFirebaseViewModel = viewModel(
        factory = TeacherFirebaseViewModelFactory(teacherFirebaseRepository)
    )

    val uiState by teacherViewModel.uiState.collectAsState()

    var staffId by remember { mutableStateOf("") }
    var teacherName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }
    var position by remember { mutableStateOf("Teacher") }
    var password by remember { mutableStateOf("") }

    var staffIdError by remember { mutableStateOf(false) }
    var teacherNameError by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopBar(
                title = "Add Teacher",
                showBack = true,
                onBackClick = onBackClick,
                showIcon = false
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundGrey)
                .navigationBarsPadding()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 32.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    Text(
                        text = "Teacher Information",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Create a teacher profile and Firebase login account",
                        fontSize = 14.sp,
                        color = TextGrey
                    )

                    Spacer(modifier = Modifier.height(22.dp))

                    OutlinedTextField(
                        value = staffId,
                        onValueChange = {
                            staffId = it
                            staffIdError = false
                            teacherViewModel.clearMessage()
                        },
                        label = { Text("Staff ID") },
                        placeholder = { Text("Example: STF001") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Badge,
                                contentDescription = "Staff ID"
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        isError = staffIdError,
                        supportingText = {
                            if (staffIdError) {
                                Text("Staff ID is required")
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            focusedLabelColor = PrimaryBlue,
                            cursorColor = PrimaryBlue
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = teacherName,
                        onValueChange = {
                            teacherName = it
                            teacherNameError = false
                            teacherViewModel.clearMessage()
                        },
                        label = { Text("Teacher Name") },
                        placeholder = { Text("Enter teacher full name") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Teacher Name"
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        isError = teacherNameError,
                        supportingText = {
                            if (teacherNameError) {
                                Text("Teacher name is required")
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            focusedLabelColor = PrimaryBlue,
                            cursorColor = PrimaryBlue
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            emailError = false
                            teacherViewModel.clearMessage()
                        },
                        label = { Text("Email") },
                        placeholder = { Text("Example: teacher@sgrms.com") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Email"
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        isError = emailError,
                        supportingText = {
                            if (emailError) {
                                Text("Email is required")
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            focusedLabelColor = PrimaryBlue,
                            cursorColor = PrimaryBlue
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = {
                            phoneNumber = it
                            teacherViewModel.clearMessage()
                        },
                        label = { Text("Phone Number") },
                        placeholder = { Text("Example: 0123456789") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = "Phone Number"
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            focusedLabelColor = PrimaryBlue,
                            cursorColor = PrimaryBlue
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = department,
                        onValueChange = {
                            department = it
                            teacherViewModel.clearMessage()
                        },
                        label = { Text("Department") },
                        placeholder = { Text("Example: Academic Department") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Work,
                                contentDescription = "Department"
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            focusedLabelColor = PrimaryBlue,
                            cursorColor = PrimaryBlue
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = position,
                        onValueChange = {},
                        label = { Text("Position") },
                        placeholder = { Text("Example: Teacher") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Work,
                                contentDescription = "Position"
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        readOnly = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            focusedLabelColor = PrimaryBlue,
                            cursorColor = PrimaryBlue
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            passwordError = false
                            teacherViewModel.clearMessage()
                        },
                        label = { Text("Login Password") },
                        placeholder = { Text("Enter teacher login password") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Password"
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        isError = passwordError,
                        supportingText = {
                            if (passwordError) {
                                Text("Password is required")
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            focusedLabelColor = PrimaryBlue,
                            cursorColor = PrimaryBlue
                        )
                    )

                    if (uiState.errorMessage.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = uiState.errorMessage,
                            color = Color.Red,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
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
                            staffIdError = staffId.isBlank()
                            teacherNameError = teacherName.isBlank()
                            emailError = email.isBlank()
                            passwordError = password.isBlank()

                            val isValid = !staffIdError &&
                                    !teacherNameError &&
                                    !emailError &&
                                    !passwordError

                            if (isValid) {
                                teacherViewModel.addTeacherWithAccount(
                                    staffId = staffId.trim(),
                                    teacherName = teacherName.trim(),
                                    email = email.trim(),
                                    phoneNumber = phoneNumber.trim(),
                                    department = department.trim(),
                                    position = position.trim().ifBlank { "Teacher" },
                                    password = password.trim(),
                                    onSuccess = onSaveClick
                                )
                            }
                        },
                        confirmEnabled = !uiState.isLoading
                    )
                }
            }
        }
    }
}