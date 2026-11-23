package com.wilanwell.sgrms.ui.admin

import com.wilanwell.sgrms.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wilanwell.sgrms.ui.components.SgrmsButtonRow
import com.wilanwell.sgrms.ui.components.SgrmsDropdownField
import com.wilanwell.sgrms.ui.components.SgrmsEmptyStateCard
import com.wilanwell.sgrms.ui.components.SgrmsTextField
import com.wilanwell.sgrms.ui.components.TopBar
import com.wilanwell.sgrms.ui.theme.ColorBackground
import com.wilanwell.sgrms.ui.theme.ColorSurface
import com.wilanwell.sgrms.ui.theme.ColorPrimary
import com.wilanwell.sgrms.ui.theme.ColorOnSurfaceVariant
import com.wilanwell.sgrms.viewmodel.TeacherFirebaseViewModel
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.wilanwell.sgrms.ui.theme.SgrmsColors
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.navigationBarsPadding
import com.wilanwell.sgrms.ui.theme.SgrmsRadius

@Composable
fun EditTeacherScreen(
    teacherId: String,
    onBackClick: () -> Unit = {},
    onSaveClick: () -> Unit = {}
) {
    val headTeacherText = stringResource(R.string.head_teacher)
    val seniorTeacherText = stringResource(R.string.senior_teacher)
    val teacherViewModel: TeacherFirebaseViewModel = hiltViewModel()

    val uiState by teacherViewModel.uiState.collectAsState()

    var staffId by remember { mutableStateOf("") }
    var teacherName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }
    var position by remember { mutableStateOf("Teacher") }

    var positionExpanded by remember { mutableStateOf(false) }

    val positionList = listOf(
        "Teacher",
        headTeacherText,
        seniorTeacherText
    )

    var staffIdError by remember { mutableStateOf(false) }
    var teacherNameError by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }

    LaunchedEffect(teacherId) {
        if (teacherId.isNotBlank()) {
            teacherViewModel.loadTeacherById(teacherId)
        }
    }

    val teacher = uiState.selectedTeacher

    LaunchedEffect(teacher) {
        if (teacher != null) {
            staffId = teacher.staffId
            teacherName = teacher.teacherName
            email = teacher.email
            phoneNumber = teacher.phoneNumber
            department = teacher.department
            position = teacher.position.ifBlank { "Teacher" }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            val currentTeacher = teacher
            if (currentTeacher != null) {
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

                                    val isValid = !staffIdError &&
                                            !teacherNameError &&
                                            !emailError

                                    if (isValid) {
                                        // copy() keeps fields this form does not edit
                                        // (photo, qualification, office, ...).
                                        val updatedTeacher = currentTeacher.copy(
                                            staffId = staffId.trim(),
                                            teacherName = teacherName.trim(),
                                            email = email.trim(),
                                            phoneNumber = phoneNumber.trim(),
                                            department = department.trim(),
                                            position = position.trim().ifBlank { "Teacher" }
                                        )

                                        teacherViewModel.updateTeacher(
                                            teacher = updatedTeacher,
                                            onSuccess = onSaveClick
                                        )
                                    }
                                },
                                confirmEnabled = !uiState.isLoading
                            )
                }
            }
        },
        topBar = {
            TopBar(
                title = stringResource(R.string.edit_teacher),
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
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = 8.dp
                )
        ) {
            when {
                uiState.isLoading && teacher == null -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(40.dp))

                        CircularProgressIndicator(
                            color = ColorPrimary
                        )
                    }
                }

                teacher == null -> {
                    SgrmsEmptyStateCard(
                        icon = Icons.Default.SupervisorAccount,
                        title = stringResource(R.string.teacher_not_found),
                        message = stringResource(R.string.the_selected_teacher_account_could_not)
                    )
                }

                else -> {
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
                                text = stringResource(R.string.update_teacher_profile_information),
                                fontSize = 14.sp,
                                color = ColorOnSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(22.dp))

                            SgrmsTextField(
                                value = staffId,
                                onValueChange = {
                                    staffId = it
                                    staffIdError = false
                                    teacherViewModel.clearMessage()
                                },
                                label = stringResource(R.string.staff_id),
                                placeholder = stringResource(R.string.example_stf001),
                                icon = Icons.Default.Badge,
                                isError = staffIdError,
                                errorMessage = stringResource(R.string.staff_id_is_required)
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            SgrmsTextField(
                                value = teacherName,
                                onValueChange = {
                                    teacherName = it
                                    teacherNameError = false
                                    teacherViewModel.clearMessage()
                                },
                                label = stringResource(R.string.teacher_name),
                                placeholder = stringResource(R.string.enter_teacher_full_name),
                                icon = Icons.Default.Person,
                                isError = teacherNameError,
                                errorMessage = stringResource(R.string.teacher_name_is_required)
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            SgrmsTextField(
                                value = email,
                                onValueChange = {
                                    email = it
                                    emailError = false
                                    teacherViewModel.clearMessage()
                                },
                                label = stringResource(R.string.email),
                                placeholder = stringResource(R.string.example_teacher_sgrms_com),
                                icon = Icons.Default.Email,
                                isError = emailError,
                                errorMessage = stringResource(R.string.email_is_required)
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            SgrmsTextField(
                                value = phoneNumber,
                                onValueChange = {
                                    phoneNumber = it
                                    teacherViewModel.clearMessage()
                                },
                                label = stringResource(R.string.phone_number),
                                placeholder = stringResource(R.string.example_0123456789),
                                icon = Icons.Default.Phone
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            SgrmsTextField(
                                value = department,
                                onValueChange = {
                                    department = it
                                    teacherViewModel.clearMessage()
                                },
                                label = stringResource(R.string.department),
                                placeholder = stringResource(R.string.example_academic_department),
                                icon = Icons.Default.Work
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            SgrmsDropdownField(
                                value = position.ifBlank { stringResource(R.string.teacher) },
                                label = stringResource(R.string.position),
                                placeholder = stringResource(R.string.select_position),
                                expanded = positionExpanded,
                                onExpandedChange = {
                                    positionExpanded = it
                                },
                                items = positionList,
                                onItemSelected = { selectedPosition ->
                                    position = selectedPosition
                                    positionExpanded = false
                                }
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
    }
}