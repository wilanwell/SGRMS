package com.wilanwell.sgrms.ui.profile

import com.wilanwell.sgrms.ui.components.OnReturnToScreen
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material.icons.filled.Work
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import com.wilanwell.sgrms.viewmodel.TeacherProfileViewModel
import com.wilanwell.sgrms.ui.theme.SgrmsColors
import com.wilanwell.sgrms.ui.components.SgrmsAvatar
import com.wilanwell.sgrms.ui.components.SgrmsStatusChip
import com.wilanwell.sgrms.ui.components.ChipTone
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Place
import com.wilanwell.sgrms.ui.components.SgrmsDetailScreen
import com.wilanwell.sgrms.ui.components.SgrmsDetailHero
import com.wilanwell.sgrms.ui.components.SgrmsInfoSection
import com.wilanwell.sgrms.ui.components.SgrmsInfoRow
import com.wilanwell.sgrms.ui.components.SgrmsRowDivider
import com.wilanwell.sgrms.ui.components.SgrmsSettingsRow
import com.wilanwell.sgrms.ui.components.SgrmsStatCard
import com.wilanwell.sgrms.ui.components.SgrmsGroupedCard

@Composable
fun TeacherProfileScreen(
    onBackClick: () -> Unit = {},
    onEditProfileClick: () -> Unit = {},
    onChangePasswordClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    val viewModel: TeacherProfileViewModel = hiltViewModel()
    OnReturnToScreen { viewModel.refresh() }
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

    SgrmsDetailScreen(
        title = "Profile",
        onBack = onBackClick,
        isLoading = isLoading && teacher == null,
        errorMessage = errorMessage
    ) {
        val name = teacher?.teacherName?.ifBlank { null } ?: "Teacher"
        SgrmsDetailHero(
            title = name,
            subtitle = teacher?.email,
            leading = {
                SgrmsAvatar(name = name, size = 88.dp, imageUrl = teacher?.profileImageUrl.orEmpty(), legacyImageBase64 = teacher?.profileImageBase64.orEmpty())
            }
        ) {
            SgrmsStatusChip(teacher?.position?.ifBlank { null } ?: "Teacher", ChipTone.Success)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SgrmsStatCard(
                value = studentCount.toString(), label = "Students", icon = Icons.Default.Group,
                tint = MaterialTheme.colorScheme.primary, container = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.weight(1f)
            )
            SgrmsStatCard(
                value = subjectCount.toString(), label = "Subjects", icon = Icons.AutoMirrored.Filled.MenuBook,
                tint = com.wilanwell.sgrms.ui.theme.SgrmsColors.Orange, container = com.wilanwell.sgrms.ui.theme.SgrmsColors.OrangeContainer,
                modifier = Modifier.weight(1f)
            )
        }

        SgrmsInfoSection("Teaching") {
            SgrmsInfoRow(icon = Icons.Default.School, label = "Classes ($classCount)", value = assignedClassNames.ifBlank { "No class assigned" })
            SgrmsRowDivider()
            SgrmsInfoRow(icon = Icons.AutoMirrored.Filled.MenuBook, label = "Subjects", value = assignedSubjectNames.ifBlank { "No subject assigned" })
            SgrmsRowDivider()
            SgrmsInfoRow(icon = Icons.Default.Summarize, label = "Reports", value = reportCount.toString())
        }

        SgrmsInfoSection("Work") {
            SgrmsInfoRow(icon = Icons.Default.Badge, label = "Staff ID", value = teacher?.staffId.orEmpty())
            SgrmsRowDivider()
            SgrmsInfoRow(icon = Icons.Default.Business, label = "Department", value = teacher?.department.orEmpty())
            SgrmsRowDivider()
            SgrmsInfoRow(icon = Icons.Default.Phone, label = "Phone", value = teacher?.phoneNumber.orEmpty())
            SgrmsRowDivider()
            SgrmsInfoRow(icon = Icons.Default.Place, label = "Office", value = teacher?.officeLocation.orEmpty())
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
