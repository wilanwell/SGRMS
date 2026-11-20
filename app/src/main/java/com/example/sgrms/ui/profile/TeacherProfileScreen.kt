package com.example.sgrms.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material.icons.filled.Work
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sgrms.ui.components.ProfileImageView
import com.example.sgrms.ui.components.SgrmsEmptyStateCard
import com.example.sgrms.ui.components.TopBar
import com.example.sgrms.ui.theme.BackgroundGrey
import com.example.sgrms.ui.theme.CardWhite
import com.example.sgrms.ui.theme.PrimaryBlue
import com.example.sgrms.ui.theme.TextGrey
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import com.example.sgrms.viewmodel.TeacherProfileViewModel

@Composable
fun TeacherProfileScreen(
    onBackClick: () -> Unit = {},
    onEditProfileClick: () -> Unit = {},
    onChangePasswordClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    val viewModel: TeacherProfileViewModel = hiltViewModel()
    val uiState by viewModel.profileUiState.collectAsState()
    val assignments by viewModel.assignments.collectAsState()

    val teacher = uiState.profile
    val isLoading = uiState.isLoading
    val errorMessage = uiState.errorMessage

    val classCount = assignments.classes.size
    val studentCount = assignments.studentCount
    val subjectCount = assignments.subjects.size
    val reportCount = assignments.reportCount

    val assignedClassNames = assignments.classes.joinToString { it.className }
    val assignedSubjectNames = assignments.subjects.joinToString { it.subjectName }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGrey)
    ) {
        TopBar(
            title = "Teacher Profile",
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
                    color = PrimaryBlue
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Loading teacher profile...",
                    fontSize = 14.sp,
                    color = TextGrey
                )
            } else if (teacher == null) {
                SgrmsEmptyStateCard(
                    icon = Icons.Default.Person,
                    title = "Teacher profile not found",
                    message = errorMessage.ifBlank {
                        "The teacher account is not linked to a teacher profile."
                    }
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = onLogoutClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlue
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
                    imageUrl = teacher?.profileImageUrl.orEmpty(),
                    legacyImageBase64 = teacher?.profileImageBase64.orEmpty(),
                    size = 90
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = teacher?.teacherName ?: "Teacher Account",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = teacher?.position?.ifBlank { "Teacher" } ?: "Teacher",
                    fontSize = 14.sp,
                    color = TextGrey
                )

                if (errorMessage.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = errorMessage,
                        fontSize = 14.sp,
                        color = Color.Red,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

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
                            text = "Account Information",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        TeacherProfileInfoRow(
                            icon = Icons.Default.Person,
                            title = "Teacher Name",
                            value = teacher?.teacherName ?: "Not linked"
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = BackgroundGrey
                        )

                        TeacherProfileInfoRow(
                            icon = Icons.Default.Badge,
                            title = "Staff ID",
                            value = teacher?.staffId ?: "Not linked"
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = BackgroundGrey
                        )

                        TeacherProfileInfoRow(
                            icon = Icons.Default.Email,
                            title = "Email",
                            value = teacher?.email ?: "-"
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = BackgroundGrey
                        )

                        TeacherProfileInfoRow(
                            icon = Icons.Default.Work,
                            title = "Department",
                            value = teacher?.department?.ifBlank { "Not specified" }
                                ?: "Not specified"
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = BackgroundGrey
                        )

                        TeacherProfileInfoRow(
                            icon = Icons.Default.Badge,
                            title = "Position",
                            value = teacher?.position?.ifBlank { "Teacher" } ?: "Teacher"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

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
                            text = "Additional Information",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        TeacherProfileInfoRow(
                            icon = Icons.Default.Badge,
                            title = "Phone Number",
                            value = teacher?.phoneNumber ?: "-"
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = BackgroundGrey
                        )

                        TeacherProfileInfoRow(
                            icon = Icons.Default.School,
                            title = "Office Location",
                            value = teacher?.officeLocation ?: "-"
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = BackgroundGrey
                        )

                        TeacherProfileInfoRow(
                            icon = Icons.Default.Work,
                            title = "Qualification",
                            value = teacher?.qualification ?: "-"
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = BackgroundGrey
                        )

                        TeacherProfileInfoRow(
                            icon = Icons.Default.MenuBook,
                            title = "Specialization",
                            value = teacher?.specialization ?: "-"
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = BackgroundGrey
                        )

                        TeacherProfileInfoRow(
                            icon = Icons.Default.Home,
                            title = "Address",
                            value = teacher?.address ?: "-"
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = BackgroundGrey
                        )

                        TeacherProfileInfoRow(
                            icon = Icons.Default.Note,
                            title = "Profile Note",
                            value = teacher?.profileNote ?: "-"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

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
                            text = "Teaching Summary",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            TeacherProfileSummaryBox(
                                title = "Classes",
                                value = classCount.toString(),
                                icon = Icons.Default.School,
                                modifier = Modifier.weight(1f)
                            )

                            TeacherProfileSummaryBox(
                                title = "Students",
                                value = studentCount.toString(),
                                icon = Icons.Default.Group,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            TeacherProfileSummaryBox(
                                title = "Subjects",
                                value = subjectCount.toString(),
                                icon = Icons.Default.MenuBook,
                                modifier = Modifier.weight(1f)
                            )

                            TeacherProfileSummaryBox(
                                title = "Reports",
                                value = reportCount.toString(),
                                icon = Icons.Default.Summarize,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        if (assignedClassNames.isNotBlank()) {
                            Spacer(modifier = Modifier.height(16.dp))

                            TeacherProfileInfoRow(
                                icon = Icons.Default.School,
                                title = "Assigned Classes",
                                value = assignedClassNames
                            )
                        }

                        if (assignedSubjectNames.isNotBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))

                            TeacherProfileInfoRow(
                                icon = Icons.Default.MenuBook,
                                title = "Assigned Subjects",
                                value = assignedSubjectNames
                            )
                        }
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
                        contentColor = PrimaryBlue
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Edit Profile",
                        tint = PrimaryBlue
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
                        contentColor = PrimaryBlue
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Change Password",
                        tint = PrimaryBlue
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
                        containerColor = PrimaryBlue
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

@Composable
fun TeacherProfileSummaryBox(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(90.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = PrimaryBlue.copy(alpha = 0.10f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = PrimaryBlue.copy(alpha = 0.12f)
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.size(12.dp))

            Column {
                Text(
                    text = value,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue
                )

                Text(
                    text = title,
                    fontSize = 13.sp,
                    color = TextGrey
                )
            }
        }
    }
}

@Composable
fun TeacherProfileInfoRow(
    icon: ImageVector,
    title: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = PrimaryBlue,
            modifier = Modifier.size(26.dp)
        )

        Spacer(modifier = Modifier.size(14.dp))

        Column {
            Text(
                text = title,
                fontSize = 14.sp,
                color = TextGrey
            )

            Text(
                text = value.ifBlank { "-" },
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}