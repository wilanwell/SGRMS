package com.example.sgrms.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sgrms.ui.components.SgrmsEmptyStateCard
import com.example.sgrms.ui.components.SgrmsInfoRow
import com.example.sgrms.ui.components.TopBar
import com.example.sgrms.ui.theme.BackgroundGrey
import com.example.sgrms.ui.theme.CardWhite
import com.example.sgrms.ui.theme.PrimaryBlue
import com.example.sgrms.ui.theme.TextGrey
import com.example.sgrms.viewmodel.TeacherFirebaseViewModel
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun TeacherDetailScreen(
    teacherId: String,
    onBackClick: () -> Unit = {},
    onEditClick: () -> Unit = {}
) {
    val teacherViewModel: TeacherFirebaseViewModel = hiltViewModel()

    val uiState by teacherViewModel.uiState.collectAsState()

    LaunchedEffect(teacherId) {
        if (teacherId.isNotBlank()) {
            teacherViewModel.loadTeacherById(teacherId)
        }
    }

    val teacher = uiState.selectedTeacher

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopBar(
                title = "Teacher Details",
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
                .padding(16.dp)
        ) {
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
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
                    TeacherProfileHeaderCard(
                        teacherName = teacher.teacherName,
                        username = teacher.staffId,
                        role = teacher.position.ifBlank { "Teacher" }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    TeacherDetailSectionCard(
                        title = "Account Information"
                    ) {
                        SgrmsInfoRow(
                            icon = Icons.Default.Person,
                            label = "User ID",
                            value = teacher.userId.ifBlank { "-" }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        SgrmsInfoRow(
                            icon = Icons.Default.Email,
                            label = "Email",
                            value = teacher.email.ifBlank { "Not specified" }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        SgrmsInfoRow(
                            icon = Icons.Default.SupervisorAccount,
                            label = "Role",
                            value = "Teacher"
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    TeacherDetailSectionCard(
                        title = "Teacher Information"
                    ) {
                        SgrmsInfoRow(
                            icon = Icons.Default.Badge,
                            label = "Staff ID",
                            value = teacher.staffId.ifBlank { "Not specified" }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        SgrmsInfoRow(
                            icon = Icons.Default.Person,
                            label = "Teacher Name",
                            value = teacher.teacherName.ifBlank { "Unknown Teacher" }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        SgrmsInfoRow(
                            icon = Icons.Default.Phone,
                            label = "Phone",
                            value = teacher.phoneNumber.ifBlank { "Not specified" }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        SgrmsInfoRow(
                            icon = Icons.Default.Work,
                            label = "Department",
                            value = teacher.department.ifBlank { "Not specified" }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        SgrmsInfoRow(
                            icon = Icons.Default.Work,
                            label = "Position",
                            value = teacher.position.ifBlank { "Teacher" }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    TeacherDetailSectionCard(
                        title = "Assigned Classes"
                    ) {
                        SgrmsInfoRow(
                            icon = Icons.Default.Work,
                            label = "Classes",
                            value = "No assigned class"
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = onEditClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryBlue
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Teacher",
                            tint = Color.White
                        )

                        Spacer(modifier = Modifier.size(8.dp))

                        Text(
                            text = "Edit Teacher",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TeacherProfileHeaderCard(
    teacherName: String,
    username: String,
    role: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryBlue),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SupervisorAccount,
                    contentDescription = "Teacher",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.size(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = teacherName.ifBlank { "Unknown Teacher" },
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = "${username.ifBlank { "-" }} • ${role.ifBlank { "-" }}",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }
    }
}

@Composable
fun TeacherDetailSectionCard(
    title: String,
    content: @Composable () -> Unit
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
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(14.dp))

            content()
        }
    }
}