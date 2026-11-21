package com.example.sgrms.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sgrms.ui.components.ProfileImageView
import com.example.sgrms.ui.components.SgrmsEmptyStateCard
import com.example.sgrms.ui.components.TopBar
import com.example.sgrms.ui.theme.ColorBackground
import com.example.sgrms.ui.theme.ColorSurface
import com.example.sgrms.ui.theme.ColorPrimary
import com.example.sgrms.ui.theme.ColorOnSurfaceVariant
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import com.example.sgrms.viewmodel.AdminProfileViewModel
import com.example.sgrms.data.model.UserRole
import com.example.sgrms.ui.theme.SgrmsColors

@Composable
fun AdminProfileScreen(
    onBackClick: () -> Unit = {},
    onEditProfileClick: () -> Unit = {},
    onChangePasswordClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    val viewModel: AdminProfileViewModel = hiltViewModel()
    val uiState by viewModel.profileUiState.collectAsState()
    val summary by viewModel.summary.collectAsState()

    val user = uiState.profile
    val profileFound = user != null
    val isLoading = uiState.isLoading
    val errorMessage = uiState.errorMessage

    val fullName = user?.fullName?.ifBlank { null } ?: "Admin Account"
    val username = user?.username?.ifBlank { null } ?: user?.email.orEmpty()
    val email = user?.email.orEmpty()
    val phoneNumber = user?.phoneNumber.orEmpty()
    val address = user?.address.orEmpty()
    val profileNote = user?.profileNote.orEmpty()
    val role = user?.role?.ifBlank { null } ?: UserRole.ADMIN.value
    val status = user?.status?.ifBlank { null } ?: "Active"

    val userCount = summary.totalAccounts
    val teacherCount = summary.teachers
    val studentCount = summary.students
    val classCount = summary.classes
    val subjectCount = summary.subjects
    val adminCount = summary.adminAccounts
    val teacherAccountCount = summary.teacherAccounts
    val studentAccountCount = summary.studentAccounts

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorBackground)
    ) {
        TopBar(
            title = "Admin Profile",
            showBack = false
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = 8.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isLoading) {
                Spacer(modifier = Modifier.height(24.dp))

                CircularProgressIndicator(
                    color = ColorPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Loading admin profile...",
                    fontSize = 14.sp,
                    color = ColorOnSurfaceVariant
                )
            } else if (!profileFound && errorMessage.isNotBlank()) {
                SgrmsEmptyStateCard(
                    icon = Icons.Default.AdminPanelSettings,
                    title = "Admin profile not found",
                    message = errorMessage
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = onLogoutClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ColorPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Logout",
                        tint = Color.White
                    )

                    Spacer(modifier = Modifier.size(8.dp))

                    Text(
                        text = "Logout",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                ProfileImageView(
                    imageUrl = user?.profileImageUrl.orEmpty(),
                    legacyImageBase64 = user?.profileImageBase64.orEmpty(),
                    size = 90
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = fullName.ifBlank { "Admin Account" },
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "${username.ifBlank { "-" }} • ${role.ifBlank { "Admin" }}",
                    fontSize = 14.sp,
                    color = ColorOnSurfaceVariant
                )

                if (errorMessage.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = errorMessage,
                        fontSize = 14.sp,
                        color = SgrmsColors.Error,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = ColorSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp)
                    ) {
                        Text(
                            text = "Account Information",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        AdminProfileInfoRow(
                            icon = Icons.Default.Person,
                            title = "Full Name",
                            value = fullName.ifBlank { "-" }
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = ColorBackground
                        )

                        AdminProfileInfoRow(
                            icon = Icons.Default.Badge,
                            title = "Username",
                            value = username.ifBlank { "-" }
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = ColorBackground
                        )

                        AdminProfileInfoRow(
                            icon = Icons.Default.Email,
                            title = "Email",
                            value = email.ifBlank { "-" }
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = ColorBackground
                        )

                        AdminProfileInfoRow(
                            icon = Icons.Default.AdminPanelSettings,
                            title = "Role",
                            value = role.ifBlank { "Admin" }
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = ColorBackground
                        )

                        AdminProfileInfoRow(
                            icon = Icons.Default.Badge,
                            title = "Status",
                            value = status.ifBlank { "Active" }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = ColorSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp)
                    ) {
                        Text(
                            text = "Additional Information",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        AdminProfileInfoRow(
                            icon = Icons.Default.Phone,
                            title = "Phone Number",
                            value = phoneNumber.ifBlank { "-" }
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = ColorBackground
                        )

                        AdminProfileInfoRow(
                            icon = Icons.Default.Home,
                            title = "Address",
                            value = address.ifBlank { "-" }
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = ColorBackground
                        )

                        AdminProfileInfoRow(
                            icon = Icons.Default.Note,
                            title = "Profile Note",
                            value = profileNote.ifBlank { "-" }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = ColorSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp)
                    ) {
                        Text(
                            text = "System Summary",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            AdminProfileSummaryBox(
                                title = "Users",
                                value = userCount.toString(),
                                icon = Icons.Default.ManageAccounts,
                                modifier = Modifier.weight(1f)
                            )

                            AdminProfileSummaryBox(
                                title = "Teachers",
                                value = teacherCount.toString(),
                                icon = Icons.Default.SupervisorAccount,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            AdminProfileSummaryBox(
                                title = "Students",
                                value = studentCount.toString(),
                                icon = Icons.Default.Group,
                                modifier = Modifier.weight(1f)
                            )

                            AdminProfileSummaryBox(
                                title = "Classes",
                                value = classCount.toString(),
                                icon = Icons.Default.School,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        AdminProfileSummaryBox(
                            title = "Subjects",
                            value = subjectCount.toString(),
                            icon = Icons.Default.MenuBook,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        AdminProfileInfoRow(
                            icon = Icons.Default.ManageAccounts,
                            title = "Account Breakdown",
                            value = "$adminCount Admin • $teacherAccountCount Teacher • $studentAccountCount Student"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                OutlinedButton(
                    onClick = onEditProfileClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = ColorPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Edit Profile",
                        tint = ColorPrimary
                    )

                    Spacer(modifier = Modifier.size(8.dp))

                    Text(
                        text = "Edit Profile",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onChangePasswordClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = ColorPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Change Password",
                        tint = ColorPrimary
                    )

                    Spacer(modifier = Modifier.size(8.dp))

                    Text(
                        text = "Change Password",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onLogoutClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ColorPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Logout",
                        tint = Color.White
                    )

                    Spacer(modifier = Modifier.size(8.dp))

                    Text(
                        text = "Logout",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
