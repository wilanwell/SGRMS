package com.example.sgrms.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sgrms.data.model.UserModel
import com.example.sgrms.viewmodel.UserManagementViewModel
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import com.example.sgrms.ui.components.SgrmsActionButtonRow
import com.example.sgrms.ui.components.SgrmsDashboardSummaryCard
import com.example.sgrms.ui.components.SgrmsDeleteDialog
import com.example.sgrms.ui.components.SgrmsEmptyStateCard
import com.example.sgrms.ui.components.SgrmsFilterButton
import com.example.sgrms.ui.components.SgrmsInfoRow
import com.example.sgrms.ui.components.SgrmsSearchBar
import com.example.sgrms.ui.components.SgrmsSummaryCardStyle
import com.example.sgrms.ui.components.TopBar
import com.example.sgrms.ui.theme.BackgroundGrey
import com.example.sgrms.ui.theme.CardWhite
import com.example.sgrms.ui.theme.PrimaryBlue
import com.example.sgrms.ui.theme.SuccessGreen
import com.example.sgrms.ui.theme.TextGrey
import com.example.sgrms.ui.theme.WarningOrange

@Composable
fun UserManagementScreen(
    onBackClick: () -> Unit = {},
    onAddTeacherClick: () -> Unit = {},
    onAddStudentAccountClick: () -> Unit = {},
    onEditUserClick: (String) -> Unit = {}
) {
    val viewModel: UserManagementViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()

    val users = uiState.users
    val isLoading = uiState.isLoading
    val errorMessage = uiState.errorMessage

    var searchText by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("All Roles") }
    var roleExpanded by remember { mutableStateOf(false) }

    var selectedUserToDelete by remember { mutableStateOf<UserModel?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    var selectedUserToView by remember { mutableStateOf<UserModel?>(null) }
    var selectedLinkedIdentityToView by remember { mutableStateOf("") }
    var showViewDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadUsers()
    }

    val roleList = listOf(
        "All Roles",
        "Admin",
        "Teacher",
        "Student"
    )

    val filteredUsers = users.filter { user ->
        val linkedId = when (user.role) {
            "Teacher" -> user.teacherId
            "Student" -> user.studentId
            "Admin" -> "System Admin"
            else -> "-"
        }

        val matchesSearch =
            user.fullName.contains(searchText, ignoreCase = true) ||
                    user.email.contains(searchText, ignoreCase = true) ||
                    user.role.contains(searchText, ignoreCase = true) ||
                    linkedId.contains(searchText, ignoreCase = true)

        val matchesRole =
            selectedRole == "All Roles" || user.role == selectedRole

        matchesSearch && matchesRole
    }

    val adminCount = users.count { it.role == "Admin" }
    val teacherCount = users.count { it.role == "Teacher" }
    val studentCount = users.count { it.role == "Student" }

    if (showDeleteDialog && selectedUserToDelete != null) {
        SgrmsDeleteDialog(
            title = "Delete User",
            message = "Are you sure you want to delete this account? The login account, profile and related records will be permanently removed.",
            onConfirmClick = {
                val userToDelete = selectedUserToDelete

                if (userToDelete != null) {
                    viewModel.deleteAccount(userToDelete)
                }

                showDeleteDialog = false
                selectedUserToDelete = null
            },
            onDismissClick = {
                showDeleteDialog = false
                selectedUserToDelete = null
            }
        )
    }

    if (showViewDialog && selectedUserToView != null) {
        UserDetailDialog(
            user = selectedUserToView!!,
            linkedIdentity = selectedLinkedIdentityToView,
            onDismiss = {
                showViewDialog = false
                selectedUserToView = null
                selectedLinkedIdentityToView = ""
            }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopBar(
                title = "User Management",
                showBack = true,
                onBackClick = onBackClick,
                showIcon = false
            )
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundGrey)
                .padding(paddingValues),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = 8.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SgrmsDashboardSummaryCard(
                        title = "Users",
                        value = users.size.toString(),
                        icon = Icons.Default.ManageAccounts,
                        modifier = Modifier.weight(1f),
                        style = SgrmsSummaryCardStyle.Primary,
                        height = 108.dp
                    )

                    SgrmsDashboardSummaryCard(
                        title = "Teachers",
                        value = teacherCount.toString(),
                        icon = Icons.Default.SupervisorAccount,
                        modifier = Modifier.weight(1f),
                        style = SgrmsSummaryCardStyle.Primary,
                        height = 108.dp
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SgrmsDashboardSummaryCard(
                        title = "Students",
                        value = studentCount.toString(),
                        icon = Icons.Default.Group,
                        modifier = Modifier.weight(1f),
                        style = SgrmsSummaryCardStyle.Primary,
                        height = 108.dp
                    )

                    SgrmsDashboardSummaryCard(
                        title = "Admins",
                        value = adminCount.toString(),
                        icon = Icons.Default.AdminPanelSettings,
                        modifier = Modifier.weight(1f),
                        style = SgrmsSummaryCardStyle.Primary,
                        height = 108.dp
                    )
                }
            }

            item {
                SgrmsSearchBar(
                    value = searchText,
                    onValueChange = {
                        searchText = it
                    },
                    label = "Search User",
                    placeholder = "Search by name, email, role, or linked ID"
                )
            }

            item {
                SgrmsFilterButton(
                    selectedValue = selectedRole,
                    expanded = roleExpanded,
                    onExpandedChange = {
                        roleExpanded = it
                    },
                    items = roleList,
                    onItemSelected = {
                        selectedRole = it
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (errorMessage.isNotBlank()) {
                item {
                    Text(
                        text = errorMessage,
                        color = Color.Red,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            item {
                Text(
                    text = "User List (${filteredUsers.size})",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = PrimaryBlue
                        )
                    }
                }
            } else if (filteredUsers.isEmpty()) {
                item {
                    SgrmsEmptyStateCard(
                        icon = Icons.Default.ManageAccounts,
                        title = "No user found",
                        message = "Try changing the search keyword or role filter"
                    )
                }
            } else {
                items(
                    items = filteredUsers,
                    key = { user ->
                        user.userId
                    }
                ) { user ->
                    val linkedIdentity = when (user.role) {
                        "Student" -> user.studentId.ifBlank { "Not linked" }
                        "Teacher" -> user.teacherId.ifBlank { "Not linked" }
                        "Admin" -> "System Admin"
                        else -> "-"
                    }

                    UserAccountCard(
                        user = user,
                        linkedIdentity = linkedIdentity,
                        onViewClick = {
                            selectedUserToView = user
                            selectedLinkedIdentityToView = linkedIdentity
                            showViewDialog = true
                        },
                        onEditClick = {
                            if (user.role == "Teacher" && user.teacherId.isNotBlank()) {
                                onEditUserClick(user.teacherId)
                            }
                        },
                        onDeleteClick = {
                            selectedUserToDelete = user
                            showDeleteDialog = true
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun UserAccountCard(
    user: UserModel,
    linkedIdentity: String,
    onViewClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val roleIcon = when (user.role) {
        "Admin" -> Icons.Default.AdminPanelSettings
        "Teacher" -> Icons.Default.SupervisorAccount
        "Student" -> Icons.Default.Group
        else -> Icons.Default.Person
    }

    val roleColor = when (user.role) {
        "Admin" -> WarningOrange
        "Teacher" -> PrimaryBlue
        "Student" -> SuccessGreen
        else -> TextGrey
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onViewClick()
            },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(roleColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = roleIcon,
                        contentDescription = user.role,
                        tint = roleColor,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.size(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = user.fullName.ifBlank {
                            "Unknown User"
                        },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "${user.email.ifBlank { "-" }} • ${user.role.ifBlank { "-" }}",
                        fontSize = 13.sp,
                        color = TextGrey
                    )
                }

                Text(
                    text = user.role.ifBlank {
                        "-"
                    },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = roleColor
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(color = BackgroundGrey)

            Spacer(modifier = Modifier.height(12.dp))

            SgrmsInfoRow(
                icon = Icons.Default.Email,
                label = "Email",
                value = user.email.ifBlank {
                    "Not specified"
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            SgrmsInfoRow(
                icon = Icons.Default.Badge,
                label = "Linked ID",
                value = linkedIdentity
            )

            Spacer(modifier = Modifier.height(14.dp))

            SgrmsActionButtonRow(
                onViewClick = onViewClick,
                onEditClick = onEditClick,
                onDeleteClick = onDeleteClick,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun UserDetailDialog(
    user: UserModel,
    linkedIdentity: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "User Details",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                SgrmsInfoRow(
                    icon = Icons.Default.Person,
                    label = "Full Name",
                    value = user.fullName.ifBlank {
                        "Unknown User"
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                SgrmsInfoRow(
                    icon = Icons.Default.Badge,
                    label = "User ID",
                    value = user.userId.ifBlank {
                        "-"
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                SgrmsInfoRow(
                    icon = Icons.Default.Email,
                    label = "Email",
                    value = user.email.ifBlank {
                        "Not specified"
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                SgrmsInfoRow(
                    icon = Icons.Default.ManageAccounts,
                    label = "Role",
                    value = user.role.ifBlank {
                        "-"
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                SgrmsInfoRow(
                    icon = Icons.Default.Badge,
                    label = "Linked ID",
                    value = linkedIdentity
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(
                    text = "Close",
                    color = PrimaryBlue
                )
            }
        }
    )
}