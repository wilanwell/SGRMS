package com.wilanwell.sgrms.ui.admin

import com.wilanwell.sgrms.R
import androidx.compose.ui.res.stringResource
import com.wilanwell.sgrms.ui.components.SgrmsPasswordField
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
import com.wilanwell.sgrms.ui.components.TopBar
import com.wilanwell.sgrms.ui.theme.ColorBackground
import com.wilanwell.sgrms.ui.theme.ColorSurface
import com.wilanwell.sgrms.ui.theme.ColorPrimary
import com.wilanwell.sgrms.ui.theme.ColorOnSurfaceVariant
import com.wilanwell.sgrms.viewmodel.TeacherFirebaseViewModel
import com.wilanwell.sgrms.ui.components.SgrmsButtonRow
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.wilanwell.sgrms.ui.theme.SgrmsColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.imePadding
import com.wilanwell.sgrms.ui.theme.SgrmsRadius
import com.wilanwell.sgrms.ui.components.sgrmsFieldColors

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
                        cancelText = stringResource(R.string.cancel),
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
                title = stringResource(R.string.add_teacher_title),
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
                        text = stringResource(R.string.teacher_information),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = stringResource(R.string.create_a_teacher_profile_and_firebase),
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
                        label = { Text(stringResource(R.string.staff_id)) },
                        placeholder = { Text(stringResource(R.string.example_stf001)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Badge,
                                contentDescription = stringResource(R.string.staff_id)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(SgrmsRadius.Field),
                        singleLine = true,
                        isError = staffIdError,
                        supportingText = {
                            if (staffIdError) {
                                Text(stringResource(R.string.staff_id_is_required))
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
                        label = { Text(stringResource(R.string.teacher_name)) },
                        placeholder = { Text(stringResource(R.string.enter_teacher_full_name)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = stringResource(R.string.teacher_name)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(SgrmsRadius.Field),
                        singleLine = true,
                        isError = teacherNameError,
                        supportingText = {
                            if (teacherNameError) {
                                Text(stringResource(R.string.teacher_name_is_required))
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
                        label = { Text(stringResource(R.string.email)) },
                        placeholder = { Text(stringResource(R.string.example_teacher_sgrms_com)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = stringResource(R.string.email)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(SgrmsRadius.Field),
                        singleLine = true,
                        isError = emailError,
                        supportingText = {
                            if (emailError) {
                                Text(stringResource(R.string.email_is_required))
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
                        label = { Text(stringResource(R.string.phone_number)) },
                        placeholder = { Text(stringResource(R.string.example_0123456789)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = stringResource(R.string.phone_number)
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
                        label = { Text(stringResource(R.string.department)) },
                        placeholder = { Text(stringResource(R.string.example_academic_department)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Work,
                                contentDescription = stringResource(R.string.department)
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
                        label = { Text(stringResource(R.string.position)) },
                        placeholder = { Text(stringResource(R.string.example_teacher)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Work,
                                contentDescription = stringResource(R.string.position)
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
                        label = stringResource(R.string.login_password),
                        placeholder = stringResource(R.string.enter_teacher_login_password),
                        isError = passwordError,
                        errorMessage = stringResource(R.string.password_is_required)
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