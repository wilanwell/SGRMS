package com.example.sgrms.ui.profile

import com.example.sgrms.ui.components.OnReturnToScreen
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sgrms.ui.theme.ColorPrimary
import com.example.sgrms.ui.theme.ColorOnSurfaceVariant
import com.example.sgrms.utils.GradeCalculator
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import com.example.sgrms.viewmodel.StudentProfileViewModel
import com.example.sgrms.ui.components.SgrmsAvatar
import com.example.sgrms.ui.components.SgrmsStatusChip
import com.example.sgrms.ui.components.ChipTone
import com.example.sgrms.ui.components.toneForGrade
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Cake
import com.example.sgrms.ui.components.SgrmsDetailScreen
import com.example.sgrms.ui.components.SgrmsDetailHero
import com.example.sgrms.ui.components.SgrmsInfoSection
import com.example.sgrms.ui.components.SgrmsInfoRow
import com.example.sgrms.ui.components.SgrmsRowDivider
import com.example.sgrms.ui.components.SgrmsSettingsRow
import com.example.sgrms.ui.components.SgrmsGroupedCard

@Composable
fun StudentProfileScreen(
    onBackClick: () -> Unit = {},
    onEditProfileClick: () -> Unit = {},
    onChangePasswordClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    val viewModel: StudentProfileViewModel = hiltViewModel()
    OnReturnToScreen { viewModel.refresh() }
    val uiState by viewModel.profileUiState.collectAsState()
    val extras by viewModel.extras.collectAsState()

    val student = uiState.profile
    val grades = extras.grades
    val account = extras.account

    val userFullName = account?.fullName?.ifBlank { null } ?: student?.studentName.orEmpty()
    val userEmail = account?.email?.ifBlank { null } ?: student?.email.orEmpty()
    val username = account?.username?.ifBlank { null }
        ?: userEmail.substringBefore("@", missingDelimiterValue = "-")

    val completedGrades = grades.filter { grade ->
        grade.status.equals("Complete", ignoreCase = true)
    }

    val totalSubjects = completedGrades.size

    val averageMarks = if (completedGrades.isNotEmpty()) {
        completedGrades.map { grade ->
            grade.marks
        }.average()
    } else {
        0.0
    }

    val overallGrade = if (completedGrades.isNotEmpty()) {
        GradeCalculator.gradeLetter(averageMarks)
    } else {
        "-"
    }

    SgrmsDetailScreen(
        title = "Profile",
        onBack = null,
        isLoading = uiState.isLoading && student == null,
        errorMessage = uiState.errorMessage
    ) {
        val name = student?.studentName?.ifBlank { null } ?: userFullName.ifBlank { "Student" }
        SgrmsDetailHero(
            title = name,
            subtitle = listOfNotNull(student?.studentCode, student?.className).filter { it.isNotBlank() }.joinToString(" · "),
            leading = {
                SgrmsAvatar(name = name, size = 88.dp, imageUrl = student?.profileImageUrl.orEmpty(), legacyImageBase64 = student?.profileImageBase64.orEmpty())
            }
        ) {
            SgrmsStatusChip(
                if (totalSubjects == 0) "No grades" else "Overall $overallGrade",
                if (totalSubjects == 0) ChipTone.Neutral else toneForGrade(overallGrade)
            )
            SgrmsStatusChip("$totalSubjects subjects", ChipTone.Primary)
        }

        SgrmsInfoSection("Contact") {
            SgrmsInfoRow(icon = Icons.Default.Email, label = "Email", value = userEmail)
            SgrmsRowDivider()
            SgrmsInfoRow(icon = Icons.Default.Phone, label = "Phone", value = student?.phoneNumber.orEmpty())
            SgrmsRowDivider()
            SgrmsInfoRow(icon = Icons.Default.Home, label = "Address", value = student?.address.orEmpty())
        }

        SgrmsInfoSection("Family") {
            SgrmsInfoRow(icon = Icons.Default.Cake, label = "Date of birth", value = student?.dateOfBirth.orEmpty())
            SgrmsRowDivider()
            SgrmsInfoRow(
                icon = Icons.Default.FamilyRestroom,
                label = "Guardian",
                value = listOfNotNull(student?.guardianName, student?.guardianPhone).filter { it.isNotBlank() }.joinToString(" · ")
            )
            SgrmsRowDivider()
            SgrmsInfoRow(icon = Icons.Default.Phone, label = "Emergency contact", value = student?.emergencyContact.orEmpty())
        }

        SgrmsGroupedCard {
            SgrmsSettingsRow("Edit profile", Icons.Default.Edit, onEditProfileClick)
            SgrmsRowDivider()
            SgrmsSettingsRow("Change password", Icons.Default.Lock, onChangePasswordClick)
        }

        SgrmsGroupedCard {
            SgrmsSettingsRow("Log out", Icons.AutoMirrored.Filled.Logout, onLogoutClick, destructive = true)
        }
    }
}

@Composable
fun StudentProfileInfoRow(
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
            tint = ColorPrimary,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.size(12.dp))

        Column {
            Text(
                text = title,
                fontSize = 13.sp,
                color = ColorOnSurfaceVariant
            )

            Text(
                text = value.ifBlank { "-" },
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}