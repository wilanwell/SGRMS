package com.wilanwell.sgrms.ui.profile

import com.wilanwell.sgrms.R
import androidx.compose.ui.res.stringResource
import com.wilanwell.sgrms.ui.components.OnReturnToScreen
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
import com.wilanwell.sgrms.ui.theme.ColorPrimary
import com.wilanwell.sgrms.ui.theme.ColorOnSurfaceVariant
import com.wilanwell.sgrms.utils.GradeCalculator
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import com.wilanwell.sgrms.viewmodel.StudentProfileViewModel
import com.wilanwell.sgrms.ui.components.SgrmsAvatar
import com.wilanwell.sgrms.ui.components.SgrmsStatusChip
import com.wilanwell.sgrms.ui.components.ChipTone
import com.wilanwell.sgrms.ui.components.toneForGrade
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Cake
import com.wilanwell.sgrms.ui.components.SgrmsDetailScreen
import com.wilanwell.sgrms.ui.components.SgrmsDetailHero
import com.wilanwell.sgrms.ui.components.SgrmsInfoSection
import com.wilanwell.sgrms.ui.components.SgrmsInfoRow
import com.wilanwell.sgrms.ui.components.SgrmsRowDivider
import com.wilanwell.sgrms.ui.components.SgrmsSettingsRow
import com.wilanwell.sgrms.ui.components.SgrmsGroupedCard

@Composable
fun StudentProfileScreen(
    onBackClick: () -> Unit = {},
    onEditProfileClick: () -> Unit = {},
    onChangePasswordClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    val noGradesText = stringResource(R.string.no_grades)
    val overallText = stringResource(R.string.overall)
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
        title = stringResource(R.string.profile),
        onBack = null,
        isLoading = uiState.isLoading && student == null,
        errorMessage = uiState.errorMessage
    ) {
        val name = student?.studentName?.ifBlank { null } ?: userFullName.ifBlank { stringResource(R.string.student) }
        SgrmsDetailHero(
            title = name,
            subtitle = listOfNotNull(student?.studentCode, student?.className).filter { it.isNotBlank() }.joinToString(" · "),
            leading = {
                SgrmsAvatar(name = name, size = 88.dp, imageUrl = student?.profileImageUrl.orEmpty(), legacyImageBase64 = student?.profileImageBase64.orEmpty())
            }
        ) {
            SgrmsStatusChip(
                if (totalSubjects == 0) noGradesText else overallText.format(overallGrade),
                if (totalSubjects == 0) ChipTone.Neutral else toneForGrade(overallGrade)
            )
            SgrmsStatusChip(stringResource(R.string.subjects_count, totalSubjects), ChipTone.Primary)
        }

        SgrmsInfoSection(stringResource(R.string.contact)) {
            SgrmsInfoRow(icon = Icons.Default.Email, label = stringResource(R.string.email), value = userEmail)
            SgrmsRowDivider()
            SgrmsInfoRow(icon = Icons.Default.Phone, label = stringResource(R.string.phone), value = student?.phoneNumber.orEmpty())
            SgrmsRowDivider()
            SgrmsInfoRow(icon = Icons.Default.Home, label = stringResource(R.string.address), value = student?.address.orEmpty())
        }

        SgrmsInfoSection(stringResource(R.string.family)) {
            SgrmsInfoRow(icon = Icons.Default.Cake, label = stringResource(R.string.date_of_birth), value = student?.dateOfBirth.orEmpty())
            SgrmsRowDivider()
            SgrmsInfoRow(
                icon = Icons.Default.FamilyRestroom,
                label = stringResource(R.string.guardian),
                value = listOfNotNull(student?.guardianName, student?.guardianPhone).filter { it.isNotBlank() }.joinToString(" · ")
            )
            SgrmsRowDivider()
            SgrmsInfoRow(icon = Icons.Default.Phone, label = stringResource(R.string.emergency_contact), value = student?.emergencyContact.orEmpty())
        }

        SgrmsGroupedCard {
            SgrmsSettingsRow(stringResource(R.string.edit_profile), Icons.Default.Edit, onEditProfileClick)
            SgrmsRowDivider()
            SgrmsSettingsRow(stringResource(R.string.change_password), Icons.Default.Lock, onChangePasswordClick)
        }

        SgrmsGroupedCard {
            SgrmsSettingsRow(stringResource(R.string.log_out), Icons.AutoMirrored.Filled.Logout, onLogoutClick, destructive = true)
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