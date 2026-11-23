package com.example.sgrms.ui.admin

import com.example.sgrms.ui.components.SgrmsPasswordField
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sgrms.ui.components.TopBar
import com.example.sgrms.ui.theme.ColorBackground
import com.example.sgrms.ui.theme.ColorSurface
import com.example.sgrms.ui.theme.ColorPrimary
import com.example.sgrms.ui.theme.ColorOnSurfaceVariant
import com.example.sgrms.viewmodel.TeacherFirebaseViewModel
import com.example.sgrms.ui.components.SgrmsButtonRow
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.sgrms.ui.theme.SgrmsColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.imePadding
import com.example.sgrms.ui.theme.SgrmsRadius
import com.example.sgrms.ui.components.sgrmsFieldColors

@Composable
fun AddTeacherScreen(
    onBackClick: () -> Unit = {},
    onSaveClick: () -> Unit = {}
) {
    val teacherViewModel: TeacherFirebaseViewModel = hiltViewModel()

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
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 8.dp) {
                SgrmsButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
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
        },
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
                .background(ColorBackground)
                .imePadding()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 32.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(SgrmsRadius.Card),
                colors = CardDefaults.cardColors(containerColor = ColorSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                        color = ColorOnSurfaceVariant
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
                        shape = RoundedCornerShape(SgrmsRadius.Field),
                        singleLine = true,
                        isError = staffIdError,
                        supportingText = {
                            if (staffIdError) {
                                Text("Staff ID is required")
                            }
                        },
                        colors = sgrmsFieldColors()
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
                        shape = RoundedCornerShape(SgrmsRadius.Field),
                        singleLine = true,
                        isError = teacherNameError,
                        supportingText = {
                            if (teacherNameError) {
                                Text("Teacher name is required")
                            }
                        },
                        colors = sgrmsFieldColors()
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
                        shape = RoundedCornerShape(SgrmsRadius.Field),
                        singleLine = true,
                        isError = emailError,
                        supportingText = {
                            if (emailError) {
                                Text("Email is required")
                            }
                        },
                        colors = sgrmsFieldColors()
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
                        shape = RoundedCornerShape(SgrmsRadius.Field),
                        singleLine = true,
                        colors = sgrmsFieldColors()
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
                        shape = RoundedCornerShape(SgrmsRadius.Field),
                        singleLine = true,
                        colors = sgrmsFieldColors()
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
                        shape = RoundedCornerShape(SgrmsRadius.Field),
                        singleLine = true,
                        readOnly = true,
                        colors = sgrmsFieldColors()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    SgrmsPasswordField(
                        value = password,
                        onValueChange = {
                            password = it
                            passwordError = false
                            teacherViewModel.clearMessage()
                        },
                        label = "Login Password",
                        placeholder = "Enter teacher login password",
                        isError = passwordError,
                        errorMessage = "Password is required"
                    )

                    if (uiState.errorMessage.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = uiState.errorMessage,
                            color = SgrmsColors.Error,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }


                }
            }
        }
    }
}