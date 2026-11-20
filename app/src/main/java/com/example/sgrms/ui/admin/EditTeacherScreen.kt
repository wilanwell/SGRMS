package com.example.sgrms.ui.admin

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sgrms.data.firebase.TeacherFirebaseService
import com.example.sgrms.data.model.TeacherModel
import com.example.sgrms.data.repository.TeacherFirebaseRepository
import com.example.sgrms.ui.components.SgrmsButtonRow
import com.example.sgrms.ui.components.SgrmsDropdownField
import com.example.sgrms.ui.components.SgrmsEmptyStateCard
import com.example.sgrms.ui.components.SgrmsTextField
import com.example.sgrms.ui.components.TopBar
import com.example.sgrms.ui.theme.BackgroundGrey
import com.example.sgrms.ui.theme.CardWhite
import com.example.sgrms.ui.theme.PrimaryBlue
import com.example.sgrms.ui.theme.TextGrey
import com.example.sgrms.viewmodel.TeacherFirebaseViewModel
import com.example.sgrms.viewmodel.TeacherFirebaseViewModelFactory

@Composable
fun EditTeacherScreen(
    teacherId: String,
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

    var positionExpanded by remember { mutableStateOf(false) }

    val positionList = listOf(
        "Teacher",
        "Head Teacher",
        "Senior Teacher"
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
        topBar = {
            TopBar(
                title = "Edit Teacher",
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
                            color = PrimaryBlue
                        )
                    }
                }

                teacher == null -> {
                    SgrmsEmptyStateCard(
                        icon = Icons.Default.SupervisorAccount,
                        title = "Teacher not found",
                        message = "The selected teacher account could not be loaded."
                    )
                }

                else -> {
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
                                text = "Update teacher profile information",
                                fontSize = 14.sp,
                                color = TextGrey
                            )

                            Spacer(modifier = Modifier.height(22.dp))

                            SgrmsTextField(
                                value = staffId,
                                onValueChange = {
                                    staffId = it
                                    staffIdError = false
                                    teacherViewModel.clearMessage()
                                },
                                label = "Staff ID",
                                placeholder = "Example: STF001",
                                icon = Icons.Default.Badge,
                                isError = staffIdError,
                                errorMessage = "Staff ID is required"
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            SgrmsTextField(
                                value = teacherName,
                                onValueChange = {
                                    teacherName = it
                                    teacherNameError = false
                                    teacherViewModel.clearMessage()
                                },
                                label = "Teacher Name",
                                placeholder = "Enter teacher full name",
                                icon = Icons.Default.Person,
                                isError = teacherNameError,
                                errorMessage = "Teacher name is required"
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            SgrmsTextField(
                                value = email,
                                onValueChange = {
                                    email = it
                                    emailError = false
                                    teacherViewModel.clearMessage()
                                },
                                label = "Email",
                                placeholder = "Example: teacher@sgrms.com",
                                icon = Icons.Default.Email,
                                isError = emailError,
                                errorMessage = "Email is required"
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            SgrmsTextField(
                                value = phoneNumber,
                                onValueChange = {
                                    phoneNumber = it
                                    teacherViewModel.clearMessage()
                                },
                                label = "Phone Number",
                                placeholder = "Example: 0123456789",
                                icon = Icons.Default.Phone
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            SgrmsTextField(
                                value = department,
                                onValueChange = {
                                    department = it
                                    teacherViewModel.clearMessage()
                                },
                                label = "Department",
                                placeholder = "Example: Academic Department",
                                icon = Icons.Default.Work
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            SgrmsDropdownField(
                                value = position.ifBlank { "Teacher" },
                                label = "Position",
                                placeholder = "Select position",
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

                                    val isValid = !staffIdError &&
                                            !teacherNameError &&
                                            !emailError

                                    if (isValid) {
                                        val updatedTeacher = TeacherModel(
                                            teacherId = teacher.teacherId,
                                            staffId = staffId.trim(),
                                            teacherName = teacherName.trim(),
                                            email = email.trim(),
                                            phoneNumber = phoneNumber.trim(),
                                            department = department.trim(),
                                            position = position.trim().ifBlank { "Teacher" },
                                            userId = teacher.userId,
                                            profileImageBase64 = teacher.profileImageBase64,
                                            officeLocation = teacher.officeLocation,
                                            qualification = teacher.qualification,
                                            specialization = teacher.specialization,
                                            address = teacher.address,
                                            profileNote = teacher.profileNote,
                                            createdAt = teacher.createdAt,
                                            updatedAt = System.currentTimeMillis()
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
                }
            }
        }
    }
}