package com.example.sgrms.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sgrms.ui.components.SgrmsDashboardSummaryCard
import com.example.sgrms.ui.components.SgrmsSummaryCardStyle
import com.example.sgrms.ui.components.TopBar
import com.example.sgrms.ui.theme.BackgroundGrey
import com.example.sgrms.ui.theme.CardWhite
import com.example.sgrms.ui.theme.PrimaryBlue
import com.example.sgrms.ui.theme.TextGrey
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import com.example.sgrms.viewmodel.AdminDashboardViewModel

@Composable
fun AdminDashboardScreen(
    adminName: String = "Admin",
    onUserManagementClick: () -> Unit = {},
    onAddTeacherClick: () -> Unit = {},
    onAddStudentAccountClick: () -> Unit = {},
    onStudentManagementClick: () -> Unit = {},
    onSubjectManagementClick: () -> Unit = {},
    onClassManagementClick: () -> Unit = {},
    onReportClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    val viewModel: AdminDashboardViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()

    val summary = uiState.summary
    val totalAccounts = summary.totalAccounts
    val adminCount = summary.adminAccounts
    val teacherAccountCount = summary.teacherAccounts
    val studentAccountCount = summary.studentAccounts
    val teacherCount = summary.teachers
    val studentCount = summary.students
    val subjectCount = summary.subjects
    val classCount = summary.classes

    val isLoading = uiState.isLoading
    val errorMessage = uiState.errorMessage

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopBar(
                title = "SGRMS Admin",
                showBack = false,
                showIcon = true,
                actions = {
                    IconButton(onClick = onProfileClick) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Profile",
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
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
                    start = 18.dp,
                    end = 18.dp,
                    top = 18.dp,
                    bottom = 8.dp
                )
        ) {
            Text(
                text = "Welcome, ${adminName.ifBlank { "Admin" }}",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Manage user accounts and monitor school records",
                fontSize = 14.sp,
                color = TextGrey
            )

            if (isLoading) {
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = PrimaryBlue,
                        strokeWidth = 2.dp
                    )

                    Spacer(modifier = Modifier.size(8.dp))

                    Text(
                        text = "Loading dashboard data...",
                        fontSize = 14.sp,
                        color = TextGrey
                    )
                }
            }

            if (errorMessage.isNotBlank()) {
                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = errorMessage,
                    fontSize = 14.sp,
                    color = Color.Red,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PrimaryBlue),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(62.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = "Admin",
                            tint = Color.White,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    Spacer(modifier = Modifier.size(14.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "System Overview",
                            color = Color.White,
                            fontSize = 16.sp
                        )

                        Text(
                            text = "$totalAccounts Accounts",
                            color = Color.White,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "$adminCount Admin • $teacherAccountCount Teacher • $studentAccountCount Student",
                            color = Color.White.copy(alpha = 0.88f),
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                SgrmsDashboardSummaryCard(
                    title = "Teachers",
                    value = teacherCount.toString(),
                    icon = Icons.Default.SupervisorAccount,
                    modifier = Modifier.weight(1f),
                    style = SgrmsSummaryCardStyle.White,
                    height = 120.dp,
                    onClick = onUserManagementClick
                )

                SgrmsDashboardSummaryCard(
                    title = "Students",
                    value = studentCount.toString(),
                    icon = Icons.Default.Group,
                    modifier = Modifier.weight(1f),
                    style = SgrmsSummaryCardStyle.White,
                    height = 120.dp,
                    onClick = onStudentManagementClick
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                SgrmsDashboardSummaryCard(
                    title = "Subjects",
                    value = subjectCount.toString(),
                    icon = Icons.Default.MenuBook,
                    modifier = Modifier.weight(1f),
                    style = SgrmsSummaryCardStyle.White,
                    height = 120.dp,
                    onClick = onSubjectManagementClick
                )

                SgrmsDashboardSummaryCard(
                    title = "Classes",
                    value = classCount.toString(),
                    icon = Icons.Default.Class,
                    modifier = Modifier.weight(1f),
                    style = SgrmsSummaryCardStyle.White,
                    height = 120.dp,
                    onClick = onClassManagementClick
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Admin Actions",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            AdminActionCard(
                title = "User Management",
                description = "View, manage, and update user accounts",
                icon = Icons.Default.ManageAccounts,
                onClick = onUserManagementClick
            )

            Spacer(modifier = Modifier.height(12.dp))

            AdminActionCard(
                title = "Add Teacher Account",
                description = "Create a new teacher profile and login account",
                icon = Icons.Default.PersonAdd,
                onClick = onAddTeacherClick
            )

            Spacer(modifier = Modifier.height(12.dp))

            AdminActionCard(
                title = "Add Student Account",
                description = "Create a student profile and linked login account",
                icon = Icons.Default.Group,
                onClick = onAddStudentAccountClick
            )
        }
    }
}

@Composable
fun AdminActionCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(88.dp)
            .clickable {
                onClick()
            },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardWhite
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(PrimaryBlue.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.size(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = description,
                    fontSize = 13.sp,
                    color = TextGrey
                )
            }

            Icon(
                imageVector = Icons.Default.Assessment,
                contentDescription = "Open",
                tint = TextGrey,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}